package com.example.biophonie.repositories

import android.content.ContentValues.TAG
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.example.biophonie.domain.GeoPoint
import com.example.biophonie.network.GeoPointWeb
import com.example.biophonie.network.NetworkGeoPoint
import com.example.biophonie.network.asDomainModel
import com.mapbox.mapboxsdk.geometry.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import retrofit2.await

class GeoPointRepository {
    suspend fun fetchGeoPoint(id: String, name: String, coordinates: LatLng){
        withContext(Dispatchers.IO){
            val response: Response<NetworkGeoPoint> = GeoPointWeb.geopoints.getGeoPoint(id)
            if (response.isSuccessful && response.body() != null)
                withContext(Dispatchers.Main){
                    geoPoint.value = response.body()?.asDomainModel(name, coordinates)
                }
        }
    }
    var geoPoint: MutableLiveData<GeoPoint> = MutableLiveData()
}