package fr.labomg.biophonie.core.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.mapbox.maps.MapboxMap

class ClearCacheWorker(appContext: Context, params: WorkerParameters) :
    CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        var result = Result.success()
        MapboxMap.clearData { it.onError { result = Result.failure() } }
        return if (applicationContext.cacheDir.deleteRecursively()) result else Result.failure()
    }

    companion object {
        const val WORK_NAME = "fr.labomg.biophonie.core.work.ClearCacheWorker"
    }
}
