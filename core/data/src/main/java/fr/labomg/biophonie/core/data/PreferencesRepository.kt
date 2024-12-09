package fr.labomg.biophonie.core.data

import fr.labomg.biophonie.core.model.CameraConfiguration
import kotlinx.coroutines.flow.Flow

interface PreferencesRepository {
    fun getCameraConfigurationStream(): Flow<CameraConfiguration>

    fun saveCameraConfiguration(options: CameraConfiguration)
}
