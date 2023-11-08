package fr.labomg.biophonie.viewmodels

import android.app.Application
import android.net.Uri
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.*
import fr.labomg.biophonie.BASE_URL
import fr.labomg.biophonie.BiophonieApplication
import fr.labomg.biophonie.data.Coordinates
import fr.labomg.biophonie.data.GeoPoint
import fr.labomg.biophonie.data.domain.InternalErrorThrowable
import fr.labomg.biophonie.data.domain.NoConnectionThrowable
import fr.labomg.biophonie.data.domain.NotFoundThrowable
import fr.labomg.biophonie.data.source.GeoPointRepository
import com.google.android.material.bottomsheet.BottomSheetBehavior
import fr.haran.soundwave.controller.DefaultPlayerController
import fr.haran.soundwave.ui.PlayerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException

class BottomPlayerViewModel(private val repository: GeoPointRepository, application: Application) : AndroidViewModel(
    application
) {
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

    private val _eventNetworkError = MutableLiveData<String>()
    val eventNetworkError: LiveData<String>
        get() = _eventNetworkError

    private val _leftClickable = MutableLiveData<Boolean>()
    val leftClickable: LiveData<Boolean>
        get() = _leftClickable

    private val _rightClickable = MutableLiveData<Boolean>()
    val rightClickable: LiveData<Boolean>
        get() = _rightClickable

    private val viewModelJob = SupervisorJob()
    private val viewModelScope = CoroutineScope(viewModelJob + Dispatchers.Main)
    val geoPoint: LiveData<GeoPoint?> = geoPointId.switchMap { id -> liveData {
        _event.value = Event.LOADING
        isFetchingClose = false
        lastLocation = null
        repository.fetchGeoPoint(id)
            .onSuccess {
                _event.value = Event.SUCCESS
                emit(it)
                displayGeoPoint()
                if (passedIds.isEmpty())
                    passedIds += id
                _eventNetworkError.value?.run { _eventNetworkError.value = null }
            }
            .onFailure {
                _event.value = Event.FAILURE
                _eventNetworkError.value = when(it){
                    is NotFoundThrowable -> "Ce son n’est plus disponible"
                    is InternalErrorThrowable -> "Oups, notre serveur a des soucis"
                    is NoConnectionThrowable -> "Connexion au serveur impossible"
                    else -> "Oups, une erreur s’est produite"
                }
                emit(null)
            }
    } }

    var passedIds: Array<Int> = arrayOf()

    fun setPlayerController(view: PlayerView){
        playerController = DefaultPlayerController(view, getApplication<Application>().cacheDir.absolutePath).apply { setPlayerListener(
            error = {
                _eventNetworkError.value = when(it) {
                    is RuntimeException -> "Le son est corrompu, désolé"
                    is IOException -> "Impossible de trouver le son"
                    else -> "Le cache est corrompu"
                }
                it.printStackTrace()
            }
        ) }
    }

    fun onRetry() {
        _eventNetworkError.value = null
        _event.value = Event.LOADING
        if (isFetchingClose && lastLocation != null)
            displayClosestGeoPoint(lastLocation!!)
        else {
            geoPointId.value?.let {
                geoPointId.value = it
            }
        }
    }

    fun onLeftClick(){
        currentIndex--
        geoPointId.value = passedIds[currentIndex]
        _rightClickable.value = true
    }

    fun onRightClick(){
        if (currentIndex <= passedIds.size-1)
            displayClosestGeoPoint(geoPoint.value!!.coordinates)
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
            repository.getClosestGeoPointId(coordinates, passedIds)
                .onSuccess {
                    currentIndex++
                    if (!passedIds.contains(it))
                        passedIds += it
                    isFetchingClose = false
                    setGeoPointQuery(it, false)
                }
                .onFailure {
                    _event.value = Event.FAILURE
                    when (it) {
                        is NotFoundThrowable -> {
                            _eventNetworkError.value = "Vous avez écouté tous les points disponibles"
                            _rightClickable.value = false
                        }
                        is InternalErrorThrowable -> _eventNetworkError.value = "Oups, notre serveur a des soucis"
                        is NoConnectionThrowable -> _eventNetworkError.value = "Connexion au serveur impossible"
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

    private fun checkClickability(){
        _leftClickable.value = currentIndex - 1 >= 0
    }

    private fun displayGeoPoint() {
        addSoundToPlayer()
        checkClickability()
    }

    private fun addSoundToPlayer() {
        if (geoPoint.value!!.sound.local != null) {
            try {
                playerController?.addAudioFileUri(getApplication(), Uri.fromFile(File(geoPoint.value!!.sound.local!!)))
                return
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        if (geoPoint.value!!.sound.remote != null) {
            val url = "$BASE_URL/api/v1/assets/sound/${geoPoint.value!!.sound.remote}"
            try {
                playerController?.addAudioUrl(url)
                return
            } catch (e: FileNotFoundException) {
                _eventNetworkError.value = "Nous n’avons pas pu trouver le son. Réessayez plus tard."
            }
        }
    }

    fun setGeoPointQuery(id: Int, resetPlaylist: Boolean){
        _bottomSheetState.value = BottomSheetBehavior.STATE_COLLAPSED
        pauseController()
        if (geoPoint.value?.remoteId == id && _event.value != Event.FAILURE)
            return
        geoPointId.value = id
        if (resetPlaylist)
            stopPlaylist()
    }

    fun pauseController() {
        playerController?.pause()
    }

    fun destroyController() {
        playerController?.destroyPlayer()
        playerController = null
    }

    class ViewModelFactory(private val application: BiophonieApplication) : ViewModelProvider.Factory {

        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(BottomPlayerViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return BottomPlayerViewModel(application.geoPointRepository, application) as T
            }
            throw IllegalArgumentException("Unknown class name")
        }

    }

    enum class Event {
        LOADING, FAILURE, SUCCESS
    }
}
