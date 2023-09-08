package fr.labomg.biophonie.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import fr.labomg.biophonie.BiophonieApplication

class SyncSoundsWorker(appContext: Context, params: WorkerParameters) :
    CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        with((applicationContext as BiophonieApplication).geoPointRepository) {
            refreshUnavailableGeoPoints()
            return if (addNewGeoPoints()) Result.success() else Result.failure()
        }
    }

    companion object {
        const val WORK_NAME = "fr.labomg.biophonie.mapviewmodel.SyncSoundsWorker"
    }
}