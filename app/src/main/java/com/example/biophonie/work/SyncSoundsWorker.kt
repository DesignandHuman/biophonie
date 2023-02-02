package com.example.biophonie.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.biophonie.data.source.DefaultGeoPointRepository
import com.example.biophonie.data.source.local.GeoPointDatabase
import com.example.biophonie.data.source.local.GeoPointLocalDataSource
import com.example.biophonie.data.source.remote.GeoPointRemoteDataSource
import com.example.biophonie.util.AppPrefs

private const val TAG = "SyncSoundsWorker"

class SyncSoundsWorker(appContext: Context, params: WorkerParameters) :
    CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        initPrefs(applicationContext)
        val database = GeoPointDatabase.getInstance(applicationContext)
        val repository = DefaultGeoPointRepository(
            GeoPointRemoteDataSource(),
            GeoPointLocalDataSource(database.geoPointDao)
        )
        repository.refreshUnavailableGeoPoints()
        return if (repository.addNewGeoPoints()) Result.success() else Result.failure()
    }

    companion object {
        const val WORK_NAME = "com.example.biophonie.mapviewmodel.SyncSoundsWorker"
    }

    private fun initPrefs(context: Context) {
        AppPrefs.setup(context)
    }
}