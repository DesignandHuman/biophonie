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
    
    suspend fun fetchGeoPoint(id: Int): Result<GeoPoint> {
        return withContext(Dispatchers.IO){
            val cachedNewGeoPoint = database.geoPointDao.getGeoPoint(id)
            if (cachedNewGeoPoint == null){
                return@withContext ClientWeb.webService.getGeoPoint(id)
                    .onSuccess { launch { insertNewGeoPoint(it.asDatabaseModel()) } }
                    .map { it.asDomainModel() }
            } else {
                return@withContext Result.success(cachedNewGeoPoint.asDomainModel())
            }
        }
    }

    suspend fun fetchClosestGeoPoint(coord: Coordinates, not: Array<Int>): Result<Int> {
        return withContext(Dispatchers.IO){
            return@withContext ClientWeb.webService.getGeoId(coord.latitude,coord.longitude,not).map { it.id }
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

    suspend fun postNewGeoPoint(geoPoint: DatabaseGeoPoint): Result<NetworkGeoPoint> {
        return withContext(Dispatchers.IO) {
            val soundFile = File(geoPoint.sound!!)
            val result = if (!geoPoint.picture!!.endsWith(".jpg")) {
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
            launch { result.getOrNull()?.let { syncGeoPoint(geoPoint.id, it) } }
            return@withContext result
        }
    }
}
