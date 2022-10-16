package com.example.biophonie.work

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.biophonie.database.GeoPointDatabase
import com.example.biophonie.repositories.GeoPointRepository
import com.example.biophonie.util.AppPrefs
import retrofit2.HttpException

private const val TAG = "SyncSoundsWorker"

class SyncSoundsWorker(appContext: Context, params: WorkerParameters) :
    CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        initPrefs(applicationContext)
        val database = GeoPointDatabase.getInstance(applicationContext)
        val repository = GeoPointRepository(database)
        val newGeoPoints = database.geoPointDao.getNewGeoPoints()

        var finalResult = true

        for (geoPoint in newGeoPoints){
            repository.postNewGeoPoint(geoPoint)
                .onSuccess { Log.d(TAG, "doWork: geopoint ${geoPoint.title} posted") }
                .onFailure {
                    finalResult = false
                    Log.d(TAG, "doWork: post geopoint ${geoPoint.title} failed with ${it.message}")
                }
        }
        return if (finalResult) Result.success() else Result.failure()
    }

    companion object {
        const val WORK_NAME = "com.example.biophonie.mapviewmodel.SyncSoundsWorker"
    }

    private fun initPrefs(context: Context) {
        AppPrefs.setup(context)
    }
}