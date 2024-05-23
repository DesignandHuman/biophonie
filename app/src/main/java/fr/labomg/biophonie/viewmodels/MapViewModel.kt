package fr.labomg.biophonie.viewmodels

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.labomg.biophonie.data.Coordinates
import fr.labomg.biophonie.data.GeoPoint
import fr.labomg.biophonie.data.Resource
import fr.labomg.biophonie.data.source.GeoPointRepository
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class MapViewModel @Inject constructor(private val repository: GeoPointRepository) : ViewModel() {

    private val _newGeoPoints = MutableLiveData<List<GeoPoint>>()
    val newGeoPoints = _newGeoPoints

    init {
        viewModelScope.launch { _newGeoPoints.value = repository.getUnavailableGeoPoints() }
    }

    // solved by lib desugaring
    @SuppressLint("NewApi")
    fun requestAddGeoPoint(extras: Bundle?, dataPath: String) {
        extras?.let {
            val date = extras.getString("date")
            val soundPath = extras.getString("soundPath")
            val templatePath =
                extras.getString("templatePath")?.apply { removePrefix("/drawable/") }
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
                        picture =
                            Resource(
                                local =
                                    if (!templatePath.isNullOrEmpty()) templatePath
                                    else landscapePath
                            ),
                        sound = Resource(local = soundPath!!),
                        remoteId = 0,
                        id = 0
                    ),
                    dataPath
                )
                _newGeoPoints.postValue(repository.getUnavailableGeoPoints())
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
}
