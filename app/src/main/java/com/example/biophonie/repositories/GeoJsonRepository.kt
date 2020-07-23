package com.example.biophonie.repositories

import androidx.lifecycle.MutableLiveData
import com.example.biophonie.domain.GeoPoint
import com.example.biophonie.network.GeoPointWeb
import com.example.biophonie.network.NetworkGeoPoint
import com.example.biophonie.network.asDomainModel
import com.example.biophonie.viewmodels.PROPERTY_ID
import com.example.biophonie.viewmodels.PROPERTY_NAME
import com.mapbox.geojson.Feature
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.geometry.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

class GeoJsonRepository {

    fun refreshFeatures(){
        // TODO fetch from network and cache
        geoFeatures.value = createTestLayers()
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
}
