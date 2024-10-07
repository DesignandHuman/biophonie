package fr.labomg.biophonie.core.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import fr.labomg.biophonie.core.data.GeoPointRepository

@HiltWorker
class SyncSoundsWorker
@AssistedInject
constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val repository: GeoPointRepository
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        with(repository) {
            refreshUnavailableGeoPoints()
            return if (addNewGeoPoints()) Result.success() else Result.failure()
        }
    }

    companion object {
        const val WORK_NAME = "fr.labomg.biophonie.mapviewmodel.SyncSoundsWorker"
    }
}
