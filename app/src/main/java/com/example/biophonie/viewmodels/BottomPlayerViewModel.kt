package com.example.biophonie.viewmodels

import android.content.Context
import androidx.lifecycle.*
import com.example.biophonie.database.GeoPointDatabase
import com.example.biophonie.domain.Coordinates
import com.example.biophonie.domain.GeoPoint
import com.example.biophonie.network.BASE_URL
import com.example.biophonie.repositories.GeoPointRepository
import com.google.android.material.bottomsheet.BottomSheetBehavior
import fr.haran.soundwave.controller.DefaultPlayerController
import fr.haran.soundwave.ui.PlayerView
import kotlinx.coroutines.*
import java.io.IOException

class BottomPlayerViewModel(private val repository: GeoPointRepository) : ViewModel() {

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

    private val _eventNetworkError = MutableLiveData<Boolean>()
    val eventNetworkError: LiveData<Boolean>
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
        emit(repository.fetchGeoPoint(id))
        displayGeoPoint()
        if (!passedIds.contains(id))
            passedIds += id
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
    }

    fun onRightClick(){
        currentIndex++
        if (currentIndex == passedIds.size)
            viewModelScope.launch {
                geoPointId.value = repository.fetchClosestGeoPoint(geoPoint.value!!.coordinates, passedIds)
            }
        else
            geoPointId.value = passedIds[currentIndex]
    }

    fun displayClosestGeoPoint(coordinates: Coordinates) {
        _bottomSheetState.value = BottomSheetBehavior.STATE_COLLAPSED
        viewModelScope.launch {
            geoPointId.value = repository.fetchClosestGeoPoint(coordinates, passedIds)
        }
    }

    private fun checkClickability(){
        _leftClickable.value = currentIndex - 1 >= 0
    }

    private fun displayGeoPoint() {
        val url = "$BASE_URL/api/v1/assets/sound/${geoPoint.value!!.soundPath}"
        try {
            playerController.addAudioUrl(url, geoPoint.value!!.amplitudes)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        checkClickability()
        _visibility.value = true
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
                return BottomPlayerViewModel(GeoPointRepository(GeoPointDatabase.getInstance(context))) as T
            }
            throw IllegalArgumentException("Unknown class name")
        }

    }
}
