package fr.labomg.biophonie.core.testing.repository

import com.mapbox.common.location.Location
import fr.labomg.biophonie.core.data.LocationRepository
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow

class TestLocationRepository : LocationRepository {
    val locationStream =
        MutableSharedFlow<Location>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val isGpsEnabledStream = MutableStateFlow(true)

    override fun getLocationStream(): Flow<Location> = locationStream

    override fun getIsGpsEnabledStream(): Flow<Boolean> = isGpsEnabledStream

    fun setGpsEnabled(enabled: Boolean) {
        isGpsEnabledStream.value = enabled
    }

    fun sendLocationUpdate(location: Location) {
        locationStream.tryEmit(location)
    }
}
