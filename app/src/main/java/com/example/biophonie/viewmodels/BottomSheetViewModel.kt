package com.example.biophonie.viewmodels

import android.content.ContentValues.TAG
import android.media.MediaPlayer
import android.util.Log
import androidx.lifecycle.*
import com.example.biophonie.domain.GeoPoint
import com.example.biophonie.domain.Sound
import com.example.biophonie.domain.dateAsCalendar
import com.example.biophonie.repositories.GeoPointRepository
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.mapbox.mapboxsdk.geometry.LatLng
import kotlinx.coroutines.*
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class BottomSheetViewModel(private val repository: GeoPointRepository) : ViewModel() {

    private lateinit var soundsIterator: ListIterator<Sound>
    var sound: Sound? = null

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

    val date: MutableLiveData<String> = MutableLiveData()
    val datePicker: MutableLiveData<String> = MutableLiveData()
    private val viewModelJob = SupervisorJob()
    private val viewModelScope = CoroutineScope(viewModelJob + Dispatchers.Main)
    val geoPoint: LiveData<GeoPoint> = repository.geoPoint


    fun onNetworkErrorShown() {
        _isNetworkErrorShown.value = true
    }

    fun onLeftClick(){
        soundsIterator.previous()
        sound = soundsIterator.previous()
        displaySound(sound!!)
    }

    fun onRightClick(){
        sound = soundsIterator.next()
        displaySound(sound!!)
    }

    private fun checkClickability(sounds: List<Sound>){
        // A bit of a hack due to ListIterators' behavior.
        // The index is between two elements.
        try {
            Log.d(TAG, "checkClickability: previous URL " + sounds[soundsIterator.previousIndex()-1].urlAudio)
            _leftClickable.value = true
        } catch (e: IndexOutOfBoundsException){
            _leftClickable.value = false
        }
        try {
            Log.d(TAG, "checkClickability: next URL "+ sounds[soundsIterator.nextIndex()].urlAudio)
            _rightClickable.value = true
        } catch (e: IndexOutOfBoundsException){
            _rightClickable.value = false
        }
    }

    private fun displaySound(sound: Sound) {
        checkClickability(geoPoint.value?.sounds!!)
        val calendar: Calendar = sound.dateAsCalendar()
        date.value = SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE).format(calendar.time)
        datePicker.value = SimpleDateFormat("MMM yyyy", Locale.FRANCE).format(calendar.time)
        _visibility.value = true
    }

    fun getGeoPoint(id: String, name: String, coordinates: LatLng){
        _bottomSheetState.value = BottomSheetBehavior.STATE_HALF_EXPANDED
        if (geoPoint.value?.id == id)
            return
        _visibility.value = false
        viewModelScope.launch {
            try {
                repository.fetchGeoPoint(id, name, coordinates)
                soundsIterator = geoPoint.value!!.sounds!!.listIterator()
                sound = soundsIterator.next()
                displaySound(sound!!)
                _eventNetworkError.value = false
                _isNetworkErrorShown.value = false
            } catch (networkError: IOException) {
                // Show a Toast error message and hide the progress bar.
                if(geoPoint.value == null)
                    _eventNetworkError.value = true
            }
        }
    }

    class ViewModelFactory : ViewModelProvider.Factory {

        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(BottomSheetViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return BottomSheetViewModel(GeoPointRepository()) as T
            }
            throw IllegalArgumentException("Unknown class name")
        }

    }
}