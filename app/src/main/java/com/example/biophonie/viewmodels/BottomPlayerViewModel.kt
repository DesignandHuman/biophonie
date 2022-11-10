package com.example.biophonie.viewmodels

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.*
import com.example.biophonie.database.GeoPointDatabase
import com.example.biophonie.domain.*
import com.example.biophonie.network.BASE_URL
import com.example.biophonie.repositories.GeoPointRepository
import com.google.android.material.bottomsheet.BottomSheetBehavior
import fr.haran.soundwave.controller.DefaultPlayerController
import fr.haran.soundwave.ui.PlayerView
import kotlinx.coroutines.*
import java.io.IOException

class BottomPlayerViewModel(private val repository: GeoPointRepository, application: Application) : AndroidViewModel(
    application
) {

    private var currentIndex = 0
    lateinit var playerController: DefaultPlayerController

    private val geoPointId = MutableLiveData<Int>()

    private val _bottomSheetState = MutableLiveData<Int>()
    val bottomSheetState: LiveData<Int>
        get() = _bottomSheetState

    private val _visibility = MutableLiveData<Boolean>()
    val visibility: LiveData<Boolean>
        get() = _visibility

    private val _isNetworkErrorShown = MutableLiveData<Boolean>()
    val isNetworkErrorShown: LiveData<Boolean>
        get() = _isNetworkErrorShown

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
    val geoPoint: LiveData<GeoPoint> = geoPointId.switchMap { id -> liveData {
        repository.fetchGeoPoint(id)
            .onSuccess {
                emit(it)
                displayGeoPoint()
                if (!passedIds.contains(id))
                    passedIds += id
            }
            .onFailure {
                when (it) {
                    is NotFoundThrowable -> _eventNetworkError.value = "Ce son n’est plus disponible"
                    is InternalErrorThrowable -> _eventNetworkError.value = "Oups, notre serveur a des soucis"
                    is NoConnectionThrowable -> _eventNetworkError.value = "Connexion au serveur impossible"
                    else -> _eventNetworkError.value = "Oups, une erreur s’est produite"
                }
            }
    } }

    var passedIds: Array<Int> = arrayOf()

    fun setPlayerController(view: PlayerView){
        playerController = DefaultPlayerController(view).apply { setPlayerListener() }
    }

    fun onNetworkErrorShown() {
        _isNetworkErrorShown.value = true
    }

    fun onLeftClick(){
        currentIndex--
        geoPointId.value = passedIds[currentIndex]
        _rightClickable.value = true
    }

    fun onRightClick(){
        currentIndex++
        if (currentIndex == passedIds.size)
            displayClosestGeoPoint(geoPoint.value!!.coordinates)
        else
            geoPointId.value = passedIds[currentIndex]
    }

    fun displayClosestGeoPoint(coordinates: Coordinates) {
        _bottomSheetState.value = BottomSheetBehavior.STATE_COLLAPSED
        viewModelScope.launch {
            repository.fetchClosestGeoPoint(coordinates, passedIds)
                .onSuccess { geoPointId.value = it }
                .onFailure {
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

    private fun checkClickability(){
        _leftClickable.value = currentIndex - 1 >= 0
    }

    private fun displayGeoPoint() {
        addSoundToPlayer()
        checkClickability()
        _visibility.value = true
    }

    private fun addSoundToPlayer() {
        if (geoPoint.value!!.sound.local != null) {
            try {
                playerController.addAudioFileUri(getApplication(), Uri.parse(geoPoint.value!!.sound.local), geoPoint.value!!.amplitudes)
                return
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        geoPoint.value!!.sound.remote?.let {
            val url = "$BASE_URL/api/v1/assets/sound/$it"
            playerController.addAudioUrl(url, geoPoint.value!!.amplitudes)
        }
    }

    fun setGeoPointQuery(id: Int){
        _bottomSheetState.value = BottomSheetBehavior.STATE_COLLAPSED
        if (geoPoint.value?.id == id)
            return
        geoPointId.value = id
    }

    class ViewModelFactory(private val context: Context) : ViewModelProvider.Factory {

        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(BottomPlayerViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return BottomPlayerViewModel(GeoPointRepository(GeoPointDatabase.getInstance(context)),
                    context as Application
                ) as T
            }
            throw IllegalArgumentException("Unknown class name")
        }

    }
}
