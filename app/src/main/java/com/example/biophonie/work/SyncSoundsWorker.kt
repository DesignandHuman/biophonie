package com.example.biophonie.work

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.biophonie.BuildConfig
import com.example.biophonie.database.NewGeoPointDatabase
import com.example.biophonie.repositories.GeoJsonRepository
import retrofit2.HttpException

private const val TAG = "SyncSoundsWorker"

class SyncSoundsWorker(appContext: Context, params: WorkerParameters) :
    CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        Log.d(TAG, "doWork: ")
        val database = NewGeoPointDatabase.getInstance(applicationContext)
        val repository = GeoJsonRepository(database)
        var finalResult = true
        try {
            val newGeoPoints = database.geoPointDao.getNewGeoPoints()
            for (geoPoint in newGeoPoints){
                val success = repository.sendNewGeoPoint(geoPoint)
                Log.d(TAG, "doWork: geopoint ${geoPoint.title} success? $success")
                if (!success) finalResult = false && continue
                else if (!BuildConfig.DEBUG) repository.deleteNewGeoPoint(geoPoint)
            }
        } catch (e: HttpException) {
            Log.d(TAG, "doWork: failure")
            return Result.failure()
        }
        return if(finalResult) Result.success() else Result.failure()
    }

    companion object {
        const val WORK_NAME = "com.example.biophonie.mapviewmodel.SyncSoundsWorker"
    }
}