package com.example.biophonie.repositories

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.biophonie.BuildConfig
import com.example.biophonie.database.DatabaseNewSound
import com.example.biophonie.database.NewSoundDatabase
import com.example.biophonie.database.asNetworkModel
import com.example.biophonie.network.GeoPointWeb
import com.example.biophonie.viewmodels.PROPERTY_CACHE
import com.example.biophonie.viewmodels.PROPERTY_ID
import com.example.biophonie.viewmodels.PROPERTY_NAME
import com.mapbox.geojson.Feature
import com.mapbox.geojson.Point
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File

private const val TAG = "GeoJsonRepository"
class GeoJsonRepository(private val database: NewSoundDatabase) {

    suspend fun insertNewSound(newSound: DatabaseNewSound){
        withContext(Dispatchers.IO) {
            database.soundDao.insert(newSound)
        }
    }

    suspend fun deleteNewSound(newSound: DatabaseNewSound){
        withContext(Dispatchers.IO) {
            database.soundDao.delete(newSound)
        }
    }

    suspend fun sendNewSound(newSound: DatabaseNewSound): Boolean{
        return withContext(Dispatchers.IO) {
            val soundFile = File(newSound.soundPath)
            Log.d(TAG, "sendNewSound: soundPath ${soundFile.path}")
            val pictureFile = File(newSound.landscapePath)
            if (BuildConfig.DEBUG)
                delay(5000)
            val request = GeoPointWeb.geopoints.postNewSound(
                newSound.asNetworkModel(),
                MultipartBody.Part.create(RequestBody.create(MediaType.parse("audio"), soundFile)),
                MultipartBody.Part.create(RequestBody.create(MediaType.parse("image"), pictureFile))
            )
            return@withContext request.isSuccessful
        }
    }

    var geoFeatures: MutableLiveData<List<Feature>> = MutableLiveData()
    var newSounds: LiveData<List<DatabaseNewSound>> = database.soundDao.getNewSoundsAsLiveData()
}
