package fr.labomg.biophonie.core.location

import com.mapbox.common.location.AccuracyAuthorization
import com.mapbox.common.location.AccuracyLevel
import com.mapbox.common.location.DeviceLocationProvider
import com.mapbox.common.location.Location
import com.mapbox.common.location.LocationObserver
import com.mapbox.common.location.LocationProviderRequest
import com.mapbox.common.location.LocationService
import com.mapbox.common.location.LocationServiceFactory
import com.mapbox.common.location.LocationServiceObserver
import com.mapbox.common.location.PermissionStatus
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import timber.log.Timber

@Singleton
class LocationDataSource @Inject constructor() {

    private val locationService: LocationService = LocationServiceFactory.getOrCreate()
    private var locationProvider: DeviceLocationProvider? = null
    private var locationRequest: LocationProviderRequest? = null

    init {
        locationRequest = LocationProviderRequest.Builder().accuracy(AccuracyLevel.MEDIUM).build()

        val result = locationService.getDeviceLocationProvider(locationRequest)
        if (result.isValue) {
            locationProvider = result.value!!
        } else {
            Timber.e("Failed to get device location provider: ${result.error}")
        }
    }

    @Suppress("MissingPermission")
    private val _location =
        callbackFlow<Location> {
            val locationObserver = LocationObserver { locations -> trySend(locations.last()) }

            locationProvider?.addLocationObserver(locationObserver)

            awaitClose { locationProvider?.removeLocationObserver(locationObserver) }
        }
    val location: Flow<Location>
        get() = _location

    private val _isGPSEnabled = callbackFlow {
        val observer =
            object : LocationServiceObserver {
                override fun onAvailabilityChanged(isAvailable: Boolean) {
                    trySend(isAvailable)
                }

                override fun onPermissionStatusChanged(permission: PermissionStatus) = Unit

                override fun onAccuracyAuthorizationChanged(authorization: AccuracyAuthorization) =
                    Unit
            }
        locationService.registerObserver(observer)
        trySend(locationService.isAvailable())
        awaitClose { locationService.unregisterObserver(observer) }
    }
    val isGPSEnabled: Flow<Boolean>
        get() = _isGPSEnabled
}
