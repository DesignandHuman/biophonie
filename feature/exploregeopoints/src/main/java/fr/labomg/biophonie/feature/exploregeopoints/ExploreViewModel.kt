package fr.labomg.biophonie.feature.exploregeopoints

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import fr.haran.soundwave.controller.DefaultPlayerController
import fr.haran.soundwave.ui.PlayerView
import fr.labomg.biophonie.core.data.GeoPointRepository
import fr.labomg.biophonie.core.data.UserRepository
import fr.labomg.biophonie.core.model.Coordinates
import fr.labomg.biophonie.core.model.GeoPoint
import fr.labomg.biophonie.core.network.InternalErrorThrowable
import fr.labomg.biophonie.core.network.NoConnectionThrowable
import fr.labomg.biophonie.core.network.NotFoundThrowable
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.launch
import timber.log.Timber

@HiltViewModel
class ExploreViewModel
@Inject
constructor(
    private val geoPointRepository: GeoPointRepository,
    private val userRepository: UserRepository,
    @ApplicationContext appContext: Context
) : AndroidViewModel(appContext as Application) {
    private var currentIndex = 0
    private var lastLocation: Coordinates? = null
    private var isFetchingClose = false
    private var playerController: DefaultPlayerController? = null

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    val geoPointId = MutableLiveData<Int?>()

    private val _event = MutableLiveData<Event>()
    val event: LiveData<Event>
        get() = _event

    private val _eventNetworkError = MutableLiveData<String?>()
    val eventNetworkError: LiveData<String?>
        get() = _eventNetworkError

    private val _leftClickable = MutableLiveData<Boolean>()
    val leftClickable: LiveData<Boolean>
        get() = _leftClickable

    private val _rightClickable = MutableLiveData<Boolean>()
    val rightClickable: LiveData<Boolean>
        get() = _rightClickable

    private val _newGeoPoints = MutableLiveData<List<GeoPoint>>()
    val newGeoPoints = _newGeoPoints

    val geoPoint: LiveData<GeoPoint?> =
        geoPointId.switchMap { id ->
            liveData {
                if (id == null) {
                    emit(null)
                    return@liveData
                }
                _event.value = Event.LOADING
                isFetchingClose = false
                lastLocation = null
                geoPointRepository
                    .fetchGeoPoint(id)
                    .onSuccess {
                        _event.value = Event.SUCCESS
                        emit(it)
                        displayGeoPoint()
                        if (passedIds.isEmpty()) passedIds += id
                        _eventNetworkError.value?.run { _eventNetworkError.value = null }
                    }
                    .onFailure {
                        _event.value = Event.FAILURE
                        _eventNetworkError.value =
                            when (it) {
                                is NotFoundThrowable -> "Ce son n’est plus disponible"
                                is InternalErrorThrowable -> "Oups, notre serveur a des soucis"
                                is NoConnectionThrowable -> "Connexion au serveur impossible"
                                else -> "Oups, une erreur s’est produite"
                            }
                        emit(null)
                    }
            }
        }

    private var passedIds: Array<Int> = arrayOf()

    init {
        refreshUnavailableGeoPoints()
    }

    fun refreshUnavailableGeoPoints() {
        viewModelScope.launch { _newGeoPoints.value = geoPointRepository.getUnavailableGeoPoints() }
    }

    fun setPlayerController(view: PlayerView) {
        playerController =
            DefaultPlayerController(view, getApplication<Application>().cacheDir.absolutePath)
                .apply {
                    setPlayerListener(
                        error = {
                            _eventNetworkError.value =
                                when (it) {
                                    is RuntimeException -> "Le son est corrompu, désolé"
                                    is IOException -> "Impossible de trouver le son"
                                    else -> "Le cache est corrompu"
                                }
                            Timber.e("could not play audio: $it")
                        }
                    )
                }
    }

    fun retry() {
        _eventNetworkError.value = null
        _event.value = Event.LOADING
        if (isFetchingClose && lastLocation != null) displayClosestGeoPoint(lastLocation!!)
        else {
            geoPointId.value?.let { geoPointId.value = it }
        }
    }

    fun previousGeoPoint() {
        currentIndex--
        geoPointId.value = passedIds[currentIndex]
        _rightClickable.value = true
    }

    fun nextGeoPoint() {
        if (currentIndex >= passedIds.size - 1) displayClosestGeoPoint(geoPoint.value!!.coordinates)
        else {
            currentIndex++
            geoPointId.value = passedIds[currentIndex]
        }
    }

    fun displayClosestGeoPoint(coordinates: Coordinates) {
        _event.value = Event.LOADING
        isFetchingClose = true
        lastLocation = coordinates
        pauseController()
        viewModelScope.launch {
            geoPointRepository
                .getClosestGeoPointId(coordinates, passedIds)
                .onSuccess {
                    currentIndex++
                    if (!passedIds.contains(it)) passedIds += it
                    isFetchingClose = false
                    setGeoPointQuery(it, false)
                }
                .onFailure {
                    _event.value = Event.FAILURE
                    when (it) {
                        is NotFoundThrowable -> {
                            _eventNetworkError.value = "Vous avez écouté tous les sons en ligne"
                            _rightClickable.value = false
                        }
                        is InternalErrorThrowable ->
                            _eventNetworkError.value = "Oups, notre serveur a des soucis"
                        is NoConnectionThrowable ->
                            _eventNetworkError.value = "Connexion au serveur impossible"
                        else -> _eventNetworkError.value = "Oups, une erreur s’est produite"
                    }
                }
        }
    }

    private fun stopPlaylist() {
        passedIds = arrayOf()
        _rightClickable.value = true
        currentIndex = 0
        pauseController()
    }

    private fun checkClickability() {
        _leftClickable.value = currentIndex - 1 >= 0
    }

    private fun displayGeoPoint() {
        addSoundToPlayer()
        checkClickability()
    }

    private fun addSoundToPlayer() {
        try {
            playerController?.addAudioFileUri(
                getApplication(),
                Uri.fromFile(File(geoPoint.value!!.sound))
            )
            return
        } catch (e: IOException) {
            Timber.e("could not add local sound to player: $e")
        }

        val url = "${BuildConfig.BASE_URL}/api/v1/assets/sound/${geoPoint.value!!.sound}"
        try {
            playerController?.addAudioUrl(url)
            return
        } catch (e: FileNotFoundException) {
            _eventNetworkError.value = "Nous n’avons pas pu trouver le son. Réessayez plus tard."
            Timber.e("could not add remote sound to player: $e")
        }
    }

    fun setGeoPointQuery(id: Int, resetPlaylist: Boolean) {
        pauseController()
        if (geoPoint.value?.remoteId == id && _event.value != Event.FAILURE) return
        geoPointId.value = id
        if (resetPlaylist) stopPlaylist()
    }

    fun pauseController() {
        playerController?.pause()
    }

    fun destroyController() {
        playerController?.destroyPlayer()
        playerController = null
    }

    fun isUserConnected(): Boolean = userRepository.isUserConnected()

    fun unselect() {
        geoPointId.value = null
        stopPlaylist()
    }

    enum class Event {
        LOADING,
        FAILURE,
        SUCCESS
    }
}
