package fr.labomg.biophonie.core.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import fr.labomg.biophonie.core.data.PreferencesRepository
import fr.labomg.biophonie.core.model.CameraConfiguration
import okio.IOException
import timber.log.Timber

@HiltWorker
class SaveCameraConfigurationWorker
@AssistedInject
constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val repository: PreferencesRepository
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        try {
            Timber.d("saving camera options")
            repository.saveCameraConfiguration(
                CameraConfiguration(
                    longitude = inputData.getDouble("longitude", 0.0),
                    latitude = inputData.getDouble("latitude", 0.0),
                    zoomLevel = inputData.getDouble("zoom_level", 0.0),
                )
            )
            Timber.d("camera options saved")
            return Result.success()
        } catch (e: IOException) {
            Timber.wtf(e, "could not save camera options")
            return Result.failure()
        }
    }
}
