package com.example.biophonie.viewmodels

import android.content.Context
import android.os.Bundle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.example.biophonie.data.Coordinates
import com.example.biophonie.data.GeoPoint
import com.example.biophonie.data.Resource
import com.example.biophonie.data.source.DefaultGeoPointRepository
import com.example.biophonie.data.source.local.GeoPointDatabase
import com.example.biophonie.data.source.local.GeoPointLocalDataSource
import com.example.biophonie.data.source.remote.GeoPointRemoteDataSource
import kotlinx.coroutines.launch
import java.time.Instant

private const val TAG = "MapViewModel"
class MapViewModel(private val repository: DefaultGeoPointRepository): ViewModel() {

    val newGeoPoints = liveData {
        emit(repository.getUnavailableGeoPoints())
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
                repository.saveNewGeoPoint(
                    GeoPoint(
                        title = title!!,
                        date = Instant.parse(date),
                        amplitudes = amplitudes!!.toList(),
                        coordinates = Coordinates(latitude, longitude),
                        picture = Resource(local = if (!templatePath.isNullOrEmpty()) templatePath else landscapePath),
                        sound = Resource(local = soundPath!!),
                        remoteId = 0,
                        id = 0
                    )
                )
            }
        }
    }

    // checks from geojson, might be needed
    /*fun checkNewGeoPoints(features: MutableList<QueriedFeature>?) {
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
    }*/

    class ViewModelFactory(private val context: Context) : ViewModelProvider.Factory {

        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MapViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return MapViewModel(DefaultGeoPointRepository(
                    GeoPointRemoteDataSource(),
                    GeoPointLocalDataSource(GeoPointDatabase.getInstance(context).geoPointDao)
                )) as T
            }
            throw IllegalArgumentException("Unknown class name")
        }
    }
}