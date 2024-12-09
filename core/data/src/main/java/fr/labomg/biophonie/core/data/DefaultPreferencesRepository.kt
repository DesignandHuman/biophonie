package fr.labomg.biophonie.core.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import fr.labomg.biophonie.core.model.CameraConfiguration
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class DefaultPreferencesRepository
@Inject
constructor(
    private val dataStore: DataStore<Preferences>,
    private val externalScope: CoroutineScope
) : PreferencesRepository {

    override fun getCameraConfigurationStream(): Flow<CameraConfiguration> =
        dataStore.data.map { prefs ->
            CameraConfiguration(
                longitude = (prefs[LONGITUDE] ?: 0).toDouble(),
                latitude = (prefs[LATITUDE] ?: 0).toDouble(),
                zoomLevel = (prefs[ZOOM_LEVEL] ?: 0).toDouble(),
            )
        }

    override fun saveCameraConfiguration(options: CameraConfiguration) {
        externalScope.launch {
            dataStore.edit { prefs ->
                prefs[LATITUDE] = (options.latitude ?: 0).toDouble()
                prefs[LONGITUDE] = (options.longitude ?: 0).toDouble()
                prefs[ZOOM_LEVEL] = (options.zoomLevel ?: 0).toDouble()
            }
        }
    }

    private companion object {
        val LATITUDE = doublePreferencesKey("latitude")
        val LONGITUDE = doublePreferencesKey("longitude")
        val ZOOM_LEVEL = doublePreferencesKey("zoom_level")
    }
}
