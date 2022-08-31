package com.example.biophonie.repositories

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.biophonie.BuildConfig
import com.example.biophonie.database.DatabaseGeoPoint
import com.example.biophonie.database.NewGeoPointDatabase
import com.example.biophonie.network.ClientWeb
import com.example.biophonie.network.asNetworkModel
import com.mapbox.geojson.Feature
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

private const val TAG = "GeoJsonRepository"
class GeoJsonRepository(private val database: NewGeoPointDatabase) {

    suspend fun insertNewGeoPoint(geoPoint: DatabaseGeoPoint){
        withContext(Dispatchers.IO) {
            database.geoPointDao.insert(geoPoint)
        }
    }

    suspend fun deleteNewGeoPoint(geoPoint: DatabaseGeoPoint){
        withContext(Dispatchers.IO) {
            database.geoPointDao.delete(geoPoint)
        }
    }

    suspend fun sendNewGeoPoint(geoPoint: DatabaseGeoPoint): Boolean{
        return withContext(Dispatchers.IO) {
            val soundFile = File(geoPoint.soundPath)
            Log.d(TAG, "sendNewSound: soundPath ${soundFile.path}")
            val pictureFile = File(geoPoint.landscapePath)
            if (BuildConfig.DEBUG)
                delay(5000)
            val request = ClientWeb.webService.postNewGeoPoint(
                geoPoint.asNetworkModel(),
                MultipartBody.Part.create(soundFile.asRequestBody("audio".toMediaTypeOrNull())),
                MultipartBody.Part.create(pictureFile.asRequestBody("image".toMediaTypeOrNull()))
            )
            return@withContext request.isSuccessful
        }
    }

    var geoFeatures: MutableLiveData<List<Feature>> = MutableLiveData()
    var newSounds: LiveData<List<DatabaseGeoPoint>> = database.geoPointDao.getNewGeoPointsAsLiveData()
}
