package com.example.biophonie.util

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.mapbox.common.location.compat.*
import com.mapbox.geojson.Point
import com.mapbox.maps.plugin.locationcomponent.LocationConsumer
import com.mapbox.maps.plugin.locationcomponent.LocationProvider
import timber.log.Timber
import java.lang.ref.WeakReference

class CustomLocationProvider(
    context: Context,
    locationUpdatesInterval: Long = DEFAULT_LOCATION_UPDATES_INTERVAL,
    locationUpdatesMaxWaitTime: Long = DEFAULT_LOCATION_UPDATES_MAX_WAIT_TIME,
    private val locationEngine: LocationEngine = LocationEngineProvider.getBestLocationEngine(context),
) : LocationProvider {

    companion object {
        private const val DEFAULT_LOCATION_UPDATES_INTERVAL = 200L

        private const val DEFAULT_LOCATION_UPDATES_MIN_WAIT_TIME = 50L
        private const val DEFAULT_LOCATION_UPDATES_MAX_WAIT_TIME = DEFAULT_LOCATION_UPDATES_INTERVAL * 5
    }

    class LocationUpdatedCallback(provider: CustomLocationProvider) :
        LocationEngineCallback<LocationEngineResult> {

        // weak reference to avoid memory leak
        private val provider = WeakReference(provider)

        override fun onSuccess(result: LocationEngineResult?) {
            result?.lastLocation?.let {
                provider.get()?.updateLocation(Point.fromLngLat(it.longitude, it.latitude, it.altitude))
                provider.get()?.updateBearing(it.bearing.toDouble())
            }
        }

        override fun onFailure(exception: Exception) {
            Timber.e("onFailure: ${exception.localizedMessage}")
        }
    }

    private val consumers: HashSet<LocationConsumer> = hashSetOf()
    private val onLocationUpdated = LocationUpdatedCallback(this)

    init {
        val request = LocationEngineRequest.Builder(locationUpdatesInterval)
            .setFastestInterval(DEFAULT_LOCATION_UPDATES_MIN_WAIT_TIME)
            .setMaxWaitTime(locationUpdatesMaxWaitTime)
            .setDisplacement(0f)
            .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY)
            .build()

        try {
            locationEngine.apply {
                requestLocationUpdates(request, onLocationUpdated, context.mainLooper)
                //getLastLocation(onLocationUpdated) // if this is commented out, callback is never called
            }
        } catch (e: SecurityException) {
            Timber.e(e)
        }
    }

    private fun updateLocation(point: Point) {
        Timber.i("updateLocation: $point")
        consumers.forEach { it.onLocationUpdated(point) }
    }

    private fun updateBearing(bearing: Double) {
        Timber.i("updateBearing: $bearing")
        consumers.forEach { it.onBearingUpdated(bearing) }
    }

    @SuppressLint("MissingPermission")
    fun addSingleRequestLocationConsumer(callback: (Location.() -> Unit)) {
        locationEngine.getLastLocation(object: LocationEngineCallback<LocationEngineResult> {
            override fun onFailure(exception: Exception) {
                Timber.e(exception)
            }

            override fun onSuccess(result: LocationEngineResult?) {
                result?.lastLocation?.let {
                    callback(it)
                }
            }
        })
    }

    override fun registerLocationConsumer(locationConsumer: LocationConsumer) {
        consumers.add(locationConsumer)
    }

    override fun unRegisterLocationConsumer(locationConsumer: LocationConsumer) {
        consumers.remove(locationConsumer)
    }
}