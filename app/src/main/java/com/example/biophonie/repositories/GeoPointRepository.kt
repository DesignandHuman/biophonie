package com.example.biophonie.repositories

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.biophonie.BuildConfig
import com.example.biophonie.database.*
import com.example.biophonie.domain.GeoPoint
import com.example.biophonie.network.ClientWeb
import com.example.biophonie.network.NetworkGeoPoint
import com.example.biophonie.network.asDatabaseModel
import com.example.biophonie.network.asDomainModel
import com.mapbox.geojson.Feature
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Response
import java.io.File

private const val TAG = "GeoPointRepository"
class GeoPointRepository(private val database: NewGeoPointDatabase) {
    
    suspend fun fetchGeoPoint(id: Int): GeoPoint {
        return withContext(Dispatchers.IO){
            val cachedNewGeoPoint = database.geoPointDao.getGeoPoint(id)
            if (cachedNewGeoPoint == null){
                val response = ClientWeb.webService.getGeoPoint(id)
                if (response.isSuccessful && response.body() != null) {
                    launch { insertNewGeoPoint(response.body()!!.asDatabaseModel()) }
                    return@withContext response.body()!!.asDomainModel()
                }
                else throw Exception("NetworkError")
            } else {
                return@withContext cachedNewGeoPoint.asDomainModel()
            }
        }
    }

    suspend fun insertNewGeoPoint(geoPoint: DatabaseGeoPoint){
        withContext(Dispatchers.IO) {
            database.geoPointDao.insert(geoPoint)
        }
    }

    private suspend fun syncGeoPoint(localId: Int, remoteId: Int) {
        withContext(Dispatchers.IO) {
            database.geoPointDao.syncGeoPoint(GeoPointSync(localId, remoteId))
        }
    }

    suspend fun postNewGeoPoint(geoPoint: DatabaseGeoPoint): Boolean{
        return withContext(Dispatchers.IO) {
            val soundFile = File(geoPoint.soundPath)
            Log.d(TAG, "sendNewSound: soundPath ${soundFile.path}")
            val pictureFile = File(geoPoint.landscapePath)
            val response = ClientWeb.webService.postNewGeoPoint(
                geoPoint.asNetworkModel(),
                MultipartBody.Part.create(soundFile.asRequestBody("audio".toMediaTypeOrNull())),
                MultipartBody.Part.create(pictureFile.asRequestBody("image".toMediaTypeOrNull()))
            )
            if (response.isSuccessful && response.body() != null) {
                launch { syncGeoPoint(geoPoint.id, response.body()!!.id) }
                return@withContext true
            } else {
                //TODO(manage error here on in OkHttpInterceptor to prevent looping)
                return@withContext false
            }
        }
    }

    var newGeoPoints: LiveData<List<DatabaseGeoPoint>> = database.geoPointDao.getNewGeoPointsAsLiveData() //TODO(refactor)
}
