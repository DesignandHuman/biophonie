package com.example.biophonie.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.biophonie.repositories.GeoJsonRepository
import com.example.biophonie.repositories.GeoPointRepository
import com.mapbox.geojson.Feature
import com.mapbox.geojson.Point
import kotlinx.coroutines.launch
import java.io.IOException

const val PROPERTY_NAME: String = "name"
const val PROPERTY_ID: String = "id"

class MapViewModel(private val repository: GeoJsonRepository): ViewModel() {

    val features = repository.geoFeatures

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

    class ViewModelFactory : ViewModelProvider.Factory {

        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MapViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return MapViewModel(GeoJsonRepository()) as T
            }
            throw IllegalArgumentException("Unknown class name")
        }

    }
}