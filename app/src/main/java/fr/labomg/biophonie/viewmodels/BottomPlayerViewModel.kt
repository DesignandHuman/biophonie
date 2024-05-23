package fr.labomg.biophonie.viewmodels

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.switchMap
import com.google.android.material.bottomsheet.BottomSheetBehavior
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import fr.haran.soundwave.controller.DefaultPlayerController
import fr.haran.soundwave.ui.PlayerView
import fr.labomg.biophonie.BuildConfig
import fr.labomg.biophonie.data.Coordinates
import fr.labomg.biophonie.data.GeoPoint
import fr.labomg.biophonie.data.InternalErrorThrowable
import fr.labomg.biophonie.data.NoConnectionThrowable
import fr.labomg.biophonie.data.NotFoundThrowable
import fr.labomg.biophonie.data.source.GeoPointRepository
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber

@HiltViewModel
class BottomPlayerViewModel
@Inject
constructor(private val repository: GeoPointRepository, @ApplicationContext appContext: Context) :
    AndroidViewModel(appContext as Application) {
    private var currentIndex = 0
    private var lastLocation: Coordinates? = null
    private var isFetchingClose = false
    private var playerController: DefaultPlayerController? = null

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    val geoPointId = MutableLiveData<Int>()

    private val _bottomSheetState = MutableLiveData<Int>()
    val bottomSheetState: LiveData<Int>
        get() = _bottomSheetState

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

    private val viewModelJob = SupervisorJob()
    private val viewModelScope = CoroutineScope(viewModelJob + Dispatchers.Main)
    val geoPoint: LiveData<GeoPoint?> =
        geoPointId.switchMap { id ->
            liveData {
                _event.value = Event.LOADING
                isFetchingClose = false
                lastLocation = null
                repository
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

    fun onRetry() {
        _eventNetworkError.value = null
        _event.value = Event.LOADING
        if (isFetchingClose && lastLocation != null) displayClosestGeoPoint(lastLocation!!)
        else {
            geoPointId.value?.let { geoPointId.value = it }
        }
    }

    fun onLeftClick() {
        currentIndex--
        geoPointId.value = passedIds[currentIndex]
        _rightClickable.value = true
    }

    fun onRightClick() {
        if (currentIndex <= passedIds.size - 1) displayClosestGeoPoint(geoPoint.value!!.coordinates)
        else {
            currentIndex++
            geoPointId.value = passedIds[currentIndex]
        }
    }

    fun displayClosestGeoPoint(coordinates: Coordinates) {
        _event.value = Event.LOADING
        _bottomSheetState.value = BottomSheetBehavior.STATE_COLLAPSED
        isFetchingClose = true
        lastLocation = coordinates
        pauseController()
        viewModelScope.launch {
            repository
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
                            _eventNetworkError.value =
                                "Vous avez écouté tous les points disponibles"
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

    fun stopPlaylist() {
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
        if (geoPoint.value!!.sound.local != null) {
            try {
                playerController?.addAudioFileUri(
                    getApplication(),
                    Uri.fromFile(File(geoPoint.value!!.sound.local!!))
                )
                return
            } catch (e: IOException) {
                Timber.e("could not add local sound to player: $e")
            }
        }

        if (geoPoint.value!!.sound.remote != null) {
            val url = "${BuildConfig.BASE_URL}/api/v1/assets/sound/${geoPoint.value!!.sound.remote}"
            try {
                playerController?.addAudioUrl(url)
                return
            } catch (e: FileNotFoundException) {
                _eventNetworkError.value =
                    "Nous n’avons pas pu trouver le son. Réessayez plus tard."
                Timber.e("could not add remote sound to player: $e")
            }
        }
    }

    fun setGeoPointQuery(id: Int, resetPlaylist: Boolean) {
        _bottomSheetState.value = BottomSheetBehavior.STATE_COLLAPSED
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

    enum class Event {
        LOADING,
        FAILURE,
        SUCCESS
    }
}
