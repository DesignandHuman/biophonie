package com.example.biophonie.viewmodels

import android.content.Context
import android.os.Bundle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.biophonie.database.DatabaseGeoPoint
import com.example.biophonie.database.GeoPointDatabase.Companion.getInstance
import com.example.biophonie.repositories.GeoPointRepository
import kotlinx.coroutines.launch

const val PROPERTY_CACHE: String = "fromCache?"
const val PROPERTY_NAME: String = "name"
const val PROPERTY_ID: String = "id"
private const val TAG = "MapViewModel"
class MapViewModel(private val repository: GeoPointRepository): ViewModel() {

    val newSounds = repository.newGeoPoints

    fun requestAddSound(extras: Bundle?) {
        extras?.let {
            val date = extras.getString("date")
            val soundPath = extras.getString("soundPath")
            val landscapePath = extras.getString("landscapePath")
            val amplitudes = extras.getFloatArray("amplitudes")
            val latitude = extras.getDouble("latitude")
            val longitude = extras.getDouble("longitude")
            val title = extras.getString("title")
            viewModelScope.launch {
                repository.insertNewGeoPoint(DatabaseGeoPoint(title!!, date.toString(), amplitudes!!.toList(), latitude, longitude, landscapePath!!, soundPath!!))
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