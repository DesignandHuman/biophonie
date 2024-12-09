package fr.labomg.biophonie.core.testing.repository

import fr.labomg.biophonie.core.data.PreferencesRepository
import fr.labomg.biophonie.core.model.CameraConfiguration
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

class TestPreferencesRepository : PreferencesRepository {

    var cameraConfiguration =
        MutableSharedFlow<CameraConfiguration>(
            replay = 1,
            onBufferOverflow = BufferOverflow.DROP_OLDEST
        )

    override fun getCameraConfigurationStream(): Flow<CameraConfiguration> = cameraConfiguration

    override fun saveCameraConfiguration(options: CameraConfiguration) {
        cameraConfiguration.tryEmit(options)
    }
}
