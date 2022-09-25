package com.example.biophonie.repositories

import com.example.biophonie.database.*
import com.example.biophonie.domain.Coordinates
import com.example.biophonie.domain.GeoPoint
import com.example.biophonie.network.ClientWeb
import com.example.biophonie.network.NetworkGeoPoint
import com.example.biophonie.network.asDatabaseModel
import com.example.biophonie.network.asDomainModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

private const val TAG = "GeoPointRepository"
class GeoPointRepository(private val database: GeoPointDatabase) {
    
    suspend fun fetchGeoPoint(id: Int): GeoPoint {
        return withContext(Dispatchers.IO){
            val cachedNewGeoPoint = database.geoPointDao.getGeoPoint(id)
            if (cachedNewGeoPoint == null){
                val response = ClientWeb.webService.getGeoPoint(id)
                if (response.isSuccessful && response.body() != null) {
                    launch { insertNewGeoPoint(response.body()!!.asDatabaseModel()) }
                    return@withContext response.body()!!.asDomainModel()
                }
                else throw Exception("NetworkError") //TODO
            } else {
                return@withContext cachedNewGeoPoint.asDomainModel()
            }
        }
    }

    suspend fun fetchClosestGeoPoint(coord: Coordinates, not: Array<Int>): Int {
        return withContext(Dispatchers.IO){
            val response = ClientWeb.webService.getGeoId(coord.latitude,coord.longitude,not)
            if (response.isSuccessful && response.body() != null) {
                return@withContext response.body()!!.id
            }
            else throw Exception("NetworkError") //TODO
        }
    }

    suspend fun insertNewGeoPoint(geoPoint: DatabaseGeoPoint){
        withContext(Dispatchers.IO) {
            database.geoPointDao.insert(geoPoint)
        }
    }

    suspend fun getNewGeoPoints(): List<GeoPoint>{
        return withContext(Dispatchers.IO) {
            database.geoPointDao.getNewGeoPoints().map { it.asDomainModel() }
        }
    }

    private suspend fun syncGeoPoint(localId: Int, remoteGeo: NetworkGeoPoint) {
        withContext(Dispatchers.IO) {
            database.geoPointDao.syncGeoPoint(GeoPointSync(
                id = localId,
                remoteId = remoteGeo.id,
                remoteSound = remoteGeo.sound,
                remotePicture = remoteGeo.picture
            ))
        }
    }

    suspend fun postNewGeoPoint(geoPoint: DatabaseGeoPoint): Boolean{
        return withContext(Dispatchers.IO) {
            val soundFile = File(geoPoint.sound!!)
            val response = if (!geoPoint.picture!!.endsWith(".jpg")) {
                ClientWeb.webService.postNewGeoPoint(
                    geoPoint.asNetworkModel(),
                    MultipartBody.Part.createFormData("sound","sound.wav",soundFile.asRequestBody("audio/x-wav".toMediaTypeOrNull())),
                    null
                )
            } else {
                val pictureFile = File(geoPoint.picture)
                ClientWeb.webService.postNewGeoPoint(
                    geoPoint.asNetworkModel(),
                    MultipartBody.Part.createFormData("sound",null,soundFile.asRequestBody("audio/x-wav".toMediaTypeOrNull())),
                    MultipartBody.Part.createFormData("picture",null,pictureFile.asRequestBody("image/jpeg".toMediaTypeOrNull()))
                )
            }
            if (response.isSuccessful && response.body() != null) {
                launch { syncGeoPoint(geoPoint.id, response.body()!!) }
                return@withContext true
            } else {
                //TODO(manage error here on in OkHttpInterceptor to prevent looping)
                return@withContext false
            }
        }
    }
}
