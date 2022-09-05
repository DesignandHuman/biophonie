package com.example.biophonie.repositories

import com.example.biophonie.database.NewGeoPointDatabase
import com.example.biophonie.database.asDomainModel
import com.example.biophonie.domain.GeoPoint
import com.example.biophonie.network.ClientWeb
import com.example.biophonie.network.NetworkGeoPoint
import com.example.biophonie.network.asDomainModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

class GeoPointRepository(private val database: NewGeoPointDatabase) {
    suspend fun fetchGeoPoint(id: Int): GeoPoint{
        return withContext(Dispatchers.IO){
            val cachedNewGeoPoint = database.geoPointDao.getNewGeoPoint(id)
            if (cachedNewGeoPoint == null){
                val response: Response<NetworkGeoPoint> = ClientWeb.webService.getGeoPoint(id)
                if (response.isSuccessful && response.body() != null)
                    return@withContext response.body()!!.asDomainModel()
                else
                    throw Exception("NetworkError")
            } else {
                return@withContext cachedNewGeoPoint.asDomainModel()
            }
        }
    }
}