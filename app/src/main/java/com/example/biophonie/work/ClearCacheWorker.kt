package com.example.biophonie.work

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.biophonie.BiophonieApplication
import com.example.biophonie.R
import com.mapbox.maps.MapView
import com.mapbox.maps.MapboxMap
import com.mapbox.maps.ResourceOptions
import com.mapbox.maps.applyDefaultParams
import timber.log.Timber
import java.io.File
import kotlin.io.path.walk

class ClearCacheWorker(appContext: Context, params: WorkerParameters) :
    CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        var result = Result.success()
        val builder = ResourceOptions.Builder().applyDefaultParams(applicationContext)
        MapboxMap.clearData(builder.build()) {
            it.onError { error ->
                Timber.e(error)
                result = Result.failure()
            }
        }
        return if (applicationContext.cacheDir.deleteRecursively())
            result
        else
            Result.failure()
    }

    companion object {
        const val WORK_NAME = "com.example.biophonie.mapviewmodel.ClearCacheWorker"
    }
}