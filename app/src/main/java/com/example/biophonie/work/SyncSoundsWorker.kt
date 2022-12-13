package com.example.biophonie.work

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.biophonie.database.GeoPointDatabase
import com.example.biophonie.network.ClientWeb
import com.example.biophonie.repositories.GeoPointRepository
import com.example.biophonie.util.AppPrefs

private const val TAG = "SyncSoundsWorker"

class SyncSoundsWorker(appContext: Context, params: WorkerParameters) :
    CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        initPrefs(applicationContext)
        val database = GeoPointDatabase.getInstance(applicationContext)
        val repository = GeoPointRepository(database)
        val toSendGeoPoints = database.geoPointDao.getGeoPointsToSend()

        var finalResult = ClientWeb.webService.pingRestricted().isSuccess
        if (finalResult)
            for (geoPoint in toSendGeoPoints){
                repository.postNewGeoPoint(geoPoint)
                    .onSuccess { Log.d(TAG, "doWork: geopoint ${geoPoint.title} posted") }
                    .onFailure {
                        finalResult = false
                        Log.d(TAG, "doWork: post geopoint ${geoPoint.title} failed with ${it.message}")
                    }
            }
        val unavailableGeoPoints = database.geoPointDao.getUnavailableNewGeoPoints()
        for (geoPoint in unavailableGeoPoints){
            ClientWeb.webService.getGeoPoint(geoPoint.remoteId)
                .onSuccess { repository.setGeoPointAvailable(geoPoint.id) }
                .onFailure { Log.d(TAG, "doWork: geopoint ${geoPoint.title} was not enabled yet") }
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