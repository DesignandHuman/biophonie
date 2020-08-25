package com.example.biophonie.repositories

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.example.biophonie.database.NewSoundDatabase
import com.example.biophonie.domain.Sound
import com.example.biophonie.viewmodels.PROPERTY_ID
import com.example.biophonie.viewmodels.PROPERTY_NAME
import com.mapbox.geojson.Feature
import com.mapbox.geojson.Point
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GeoJsonRepository(private val database: NewSoundDatabase) {

    fun refreshFeatures(){
        // TODO fetch from network (use GeoJsonSource)
        geoFeatures.value = createTestLayers()
    }

    suspend fun insertNewSound(newSound: Sound, title: String){
        withContext(Dispatchers.IO) {
            database.soundDao.insert(newSound)
        }
    }

    private fun createTestLayers(): MutableList<Feature> {
        val symbolLayerIconFeatureList: MutableList<Feature> = ArrayList()
        symbolLayerIconFeatureList.add(
            Feature.fromGeometry(
                Point.fromLngLat(-1.803165, 47.516218)
            ).apply {
                addStringProperty(PROPERTY_NAME, "Point 1")
                addStringProperty(PROPERTY_ID, "1")
            }
        )
        symbolLayerIconFeatureList.add(
            Feature.fromGeometry(
                Point.fromLngLat(-1.814496, 47.534516)
            ).apply {
                addStringProperty(PROPERTY_NAME, "Point 2")
                addStringProperty(PROPERTY_ID, "2")
            }
        )
        symbolLayerIconFeatureList.add(
            Feature.fromGeometry(
                Point.fromLngLat(-1.778381, 47.512238)
            ).apply {
                addStringProperty(PROPERTY_NAME, "Point 3")
                addStringProperty(PROPERTY_ID, "3")
            }
        )
        return symbolLayerIconFeatureList
    }

    var geoFeatures: MutableLiveData<List<Feature>> = MutableLiveData()
    var newSounds: LiveData<List<Sound>> = database.soundDao.getNewSounds()
}
