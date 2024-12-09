package fr.labomg.biophonie.core.data

import com.mapbox.common.location.Location
import fr.labomg.biophonie.core.location.LocationDataSource
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class DefaultLocationRepository
@Inject
constructor(private val locationDataSource: LocationDataSource) : LocationRepository {
    override fun getLocationStream(): Flow<Location> = locationDataSource.location

    override fun getIsGpsEnabledStream(): Flow<Boolean> = locationDataSource.isGPSEnabled
}
