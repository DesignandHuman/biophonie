package com.example.biophonie.util

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import com.mapbox.android.core.location.LocationEngineCallback
import com.mapbox.android.core.location.LocationEngineProvider
import com.mapbox.android.core.location.LocationEngineRequest
import com.mapbox.android.core.location.LocationEngineResult
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.geojson.Point
import com.mapbox.maps.plugin.locationcomponent.LocationComponentConstants
import com.mapbox.maps.plugin.locationcomponent.LocationConsumer
import com.mapbox.maps.plugin.locationcomponent.LocationProvider
import kotlinx.coroutines.*
import timber.log.Timber
import java.lang.ref.WeakReference
import java.util.concurrent.CopyOnWriteArraySet

/**
 * Custom Location Provider implementation adapted from mapbox DefaultLocationProvider
 */
class CustomLocationProvider(
    context: Context
) : LocationProvider {

    private val applicationContext = context.applicationContext
    private val locationEngine = LocationEngineProvider.getBestLocationEngine(applicationContext)
    private val locationEngineRequest =
        LocationEngineRequest.Builder(LocationComponentConstants.DEFAULT_INTERVAL_MILLIS)
            .setFastestInterval(LocationComponentConstants.DEFAULT_FASTEST_INTERVAL_MILLIS)
            .setPriority(LocationEngineRequest.PRIORITY_BALANCED_POWER_ACCURACY)
            .build()

    private val locationConsumers = CopyOnWriteArraySet<LocationConsumer>()
    private var updateDelay = INIT_UPDATE_DELAY
    private val job = CoroutineScope(Dispatchers.IO).launch (start = CoroutineStart.LAZY) {
        delay(updateDelay)
        requestLocationUpdates()
    }

    private val locationEngineCallback: LocationEngineCallback<LocationEngineResult> =
        CurrentLocationEngineCallback(this)

    @SuppressLint("MissingPermission")
    private fun requestLocationUpdates() {
        if (PermissionsManager.areLocationPermissionsGranted(applicationContext)) {
            locationEngine.requestLocationUpdates(
                locationEngineRequest, locationEngineCallback, Looper.getMainLooper()
            )
        } else {
            if (updateDelay * 2 < MAX_UPDATE_DELAY) {
                updateDelay *= 2
            } else {
                updateDelay = MAX_UPDATE_DELAY
            }
            job.start()
            Timber.w("Missing location permission, location component will not take effect before location permission is granted.")
        }
    }

    private fun notifyLocationUpdates(location: Location) {
        locationConsumers.forEach { consumer ->
            consumer.onLocationUpdated(Point.fromLngLat(location.longitude, location.latitude))
        }
    }

    fun addSingleRequestLocationConsumer(callback: (Point.() -> Unit)) {
        this.registerLocationConsumer(object : LocationConsumer {
            private var updates = 0
            override fun onBearingUpdated(
                vararg bearing: Double,
                options: (ValueAnimator.() -> Unit)?
            ) {}

            override fun onLocationUpdated(
                vararg location: Point,
                options: (ValueAnimator.() -> Unit)?
            ) {
                updates++
                if (updates >= UPDATES_NEEDED) {
                    callback(location[0])
                    this@CustomLocationProvider.unRegisterLocationConsumer(this)
                }
            }

            override fun onPuckBearingAnimatorDefaultOptionsUpdated(options: ValueAnimator.() -> Unit) {
            }

            override fun onPuckLocationAnimatorDefaultOptionsUpdated(options: ValueAnimator.() -> Unit) {
            }
        })
    }

    @SuppressLint("MissingPermission")
    override fun registerLocationConsumer(locationConsumer: LocationConsumer) {
        if (locationConsumers.isEmpty()) {
            requestLocationUpdates()
        }
        locationConsumers.add(locationConsumer)
        if (PermissionsManager.areLocationPermissionsGranted(applicationContext)) {
            locationEngine.getLastLocation(locationEngineCallback)
        } else {
            Timber.w("Missing location permission, location component will not take effect before location permission is granted.")
        }
    }

    override fun unRegisterLocationConsumer(locationConsumer: LocationConsumer) {
        locationConsumers.remove(locationConsumer)
        if (locationConsumers.isEmpty()) {
            locationEngine.removeLocationUpdates(locationEngineCallback)
            GlobalScope.launch { job.cancel() }
        }
    }

    // Callbacks may leak after GoogleLocationEngineImpl.removeLocationUpdates,
    // see https://github.com/mapbox/mapbox-events-android/issues/562 for more details
    private class CurrentLocationEngineCallback(locationProvider: CustomLocationProvider) :
        LocationEngineCallback<LocationEngineResult> {
        private val locationProviderWeakReference: WeakReference<CustomLocationProvider> =
            WeakReference(locationProvider)

        override fun onSuccess(result: LocationEngineResult) {
            result.lastLocation?.let { location ->
                locationProviderWeakReference.get()?.notifyLocationUpdates(location)
            }
        }

        override fun onFailure(exception: Exception) {
            Timber.e("Failed to obtain location update: $exception")
        }
    }

    private companion object {
        private const val INIT_UPDATE_DELAY = 1000L
        private const val MAX_UPDATE_DELAY = 5000L
        private const val UPDATES_NEEDED = 3
    }
}