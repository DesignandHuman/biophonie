package fr.labomg.biophonie.core.data

import com.mapbox.common.location.Location
import kotlinx.coroutines.flow.Flow

interface LocationRepository {
    fun getLocationStream(): Flow<Location>

    fun getIsGpsEnabledStream(): Flow<Boolean>
}
