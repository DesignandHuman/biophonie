package com.example.biophonie.viewmodels

import android.content.Context
import android.os.Bundle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.example.biophonie.PROPERTY_ID
import com.example.biophonie.database.DatabaseGeoPoint
import com.example.biophonie.database.GeoPointDatabase.Companion.getInstance
import com.example.biophonie.repositories.GeoPointRepository
import com.mapbox.maps.QueriedFeature
import kotlinx.coroutines.launch

private const val TAG = "MapViewModel"
class MapViewModel(private val repository: GeoPointRepository): ViewModel() {

    val newGeoPoints = liveData {
        emit(repository.getNewGeoPoints())
    }

    fun requestAddGeoPoint(extras: Bundle?) {
        extras?.let {
            val date = extras.getString("date")
            val soundPath = extras.getString("soundPath")
            val templatePath = extras.getString("templatePath")?.apply { removePrefix("/drawable/") }
            val landscapePath = extras.getString("landscapePath")
            val amplitudes = extras.getFloatArray("amplitudes")
            val latitude = extras.getDouble("latitude")
            val longitude = extras.getDouble("longitude")
            val title = extras.getString("title")
            viewModelScope.launch {
                repository.insertNewGeoPoint(DatabaseGeoPoint(
                    title = title!!,
                    date = date.toString(),
                    amplitudes = amplitudes!!.toList(),
                    latitude = latitude,
                    longitude = longitude,
                    picture = if (!templatePath.isNullOrEmpty()) templatePath else landscapePath,
                    sound = soundPath!!,
                    available = false
                ))
            }
        }
    }

    fun checkNewGeoPoints(features: MutableList<QueriedFeature>?) {
        if (features != null && newGeoPoints.value != null) {
            viewModelScope.launch {
                val remoteFeatureIds = features.map { feature -> feature.feature.getNumberProperty(
                    PROPERTY_ID).toInt() }
                for (geoPoint in newGeoPoints.value!!) {
                    if (remoteFeatureIds.contains(geoPoint.remoteId))
                        repository.setGeoPointAvailable(geoPoint.id)
                }
            }
        }
    }

    class ViewModelFactory(private val context: Context) : ViewModelProvider.Factory {

        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MapViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return MapViewModel(GeoPointRepository(getInstance(context))) as T
            }
            throw IllegalArgumentException("Unknown class name")
        }
    }
}