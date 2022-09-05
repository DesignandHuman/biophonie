package com.example.biophonie.viewmodels

import android.content.Context
import android.net.Uri
import androidx.lifecycle.*
import com.example.biophonie.database.NewGeoPointDatabase
import com.example.biophonie.domain.GeoPoint
import com.example.biophonie.network.BASE_URL
import com.example.biophonie.repositories.GeoPointRepository
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.mapbox.geojson.Point
import fr.haran.soundwave.controller.DefaultPlayerController
import fr.haran.soundwave.ui.PlayerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
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

    val geoPoint: LiveData<GeoPoint> = geoPointId.switchMap { id -> liveData {
        emit(repository.fetchGeoPoint(id))
        displayGeoPoint()
    } }

    fun setPlayerController(view: PlayerView){
        playerController = DefaultPlayerController(view).apply { setPlayerListener() }
    }

    fun onNetworkErrorShown() {
        _isNetworkErrorShown.value = true
    }

    fun onLeftClick(){
        currentIndex--
        /*geoPoint.value?.sounds?.get(currentIndex--)?.let {
            sound = it
            displaySound(it)
        }*/ //TODO
    }

    fun onRightClick(){
        currentIndex++
        /*geoPoint.value?.sounds?.get(currentIndex++)?.let {
            sound = it
            displaySound(it)
        }*/ //TODO
    }

    private fun checkClickability(){
        _leftClickable.value = currentIndex - 1 >= 0
        // TODO /*_rightClickable.value = currentIndex < geoPoint.value?.sounds?.size!! - 1*/
    }

    private fun displayGeoPoint() {
        val url = "$BASE_URL/api/v1/assets/sound/${geoPoint.value?.soundPath}"
        try {
            geoPoint.value?.let {
                playerController.addAudioUrl(url, it.amplitudes)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        checkClickability()
        currentIndex = 0
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
                return BottomPlayerViewModel(GeoPointRepository(NewGeoPointDatabase.getInstance(context))) as T
            }
            throw IllegalArgumentException("Unknown class name")
        }

    }
}
