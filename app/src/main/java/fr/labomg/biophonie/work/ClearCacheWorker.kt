package fr.labomg.biophonie.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.mapbox.maps.MapboxMap
import com.mapbox.maps.ResourceOptions
import com.mapbox.maps.applyDefaultParams
import timber.log.Timber

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
        const val WORK_NAME = "fr.labomg.biophonie.mapviewmodel.ClearCacheWorker"
    }
}