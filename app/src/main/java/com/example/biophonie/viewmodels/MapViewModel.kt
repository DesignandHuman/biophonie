package com.example.biophonie.viewmodels

import android.content.Context
import android.os.Bundle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.biophonie.database.DatabaseNewSound
import com.example.biophonie.database.NewSoundDatabase.Companion.getInstance
import com.example.biophonie.repositories.GeoJsonRepository
import com.example.biophonie.util.getRandomString
import kotlinx.coroutines.launch
import java.io.IOException

const val PROPERTY_NAME: String = "name"
const val PROPERTY_ID: String = "id"
private const val TAG = "MapViewModel"
class MapViewModel(private val repository: GeoJsonRepository): ViewModel() {

    val features = repository.geoFeatures
    val newSounds = repository.newSounds

    init {
        refreshDataFromRepository()
    }

    private fun refreshDataFromRepository() {
        viewModelScope.launch {
            try {
                repository.refreshFeatures()
            } catch (networkError: IOException) {
                // Show a Toast error message and hide the progress bar.
            }
        }
    }

    fun requestAddSound(extras: Bundle?) {
        extras?.let {
            val date = extras.getString("date")
            val soundPath = extras.getString("soundPath")
            val landscapePath = extras.getString("landscapePath")
            val amplitudes = extras.getIntegerArrayList("amplitudes")
            val latitude = extras.getDouble("latitude")
            val longitude = extras.getDouble("longitude")
            val title = extras.getString("title")
            viewModelScope.launch {
                repository.insertNewSound(DatabaseNewSound(getRandomString(16), title!!, date.toString(), amplitudes as List<Int>, latitude, longitude, landscapePath!!, soundPath!!))
            }
        }
    }

    class ViewModelFactory(private val context: Context) : ViewModelProvider.Factory {

        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MapViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return MapViewModel(GeoJsonRepository(getInstance(context))) as T
            }
            throw IllegalArgumentException("Unknown class name")
        }
    }
}