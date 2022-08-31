package com.example.biophonie.repositories

import androidx.lifecycle.MutableLiveData
import com.example.biophonie.database.NewGeoPointDatabase
import com.example.biophonie.database.asDomainModel
import com.example.biophonie.domain.GeoPoint
import com.example.biophonie.network.ClientWeb
import com.example.biophonie.network.NetworkGeoPoint
import com.example.biophonie.network.asDomainModel
import com.example.biophonie.util.coordinatesToString
import com.mapbox.geojson.Point
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

class GeoPointRepository(private val database: NewGeoPointDatabase) {
    suspend fun fetchGeoPoint(id: Int, name: String, coordinates: Point){
        withContext(Dispatchers.IO){
            val cachedNewGeoPoint = database.geoPointDao.getNewGeoPoint(id)
            if (cachedNewGeoPoint == null){
                val response: Response<NetworkGeoPoint> = ClientWeb.webService.getGeoPoint(id)
                if (response.isSuccessful && response.body() != null)
                    withContext(Dispatchers.Main){
                        geoPoint.value = response.body()?.asDomainModel(name, coordinates)
                    }
            } else {
                withContext(Dispatchers.Main){
                    geoPoint.value = cachedNewGeoPoint.asDomainModel()
                }
            }
        }
    }
    var geoPoint: MutableLiveData<GeoPoint> = MutableLiveData()
}