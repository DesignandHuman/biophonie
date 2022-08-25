package com.example.biophonie.util

import android.content.Context
import android.util.Log
import com.mapbox.common.location.compat.*
import com.mapbox.geojson.Point
import com.mapbox.maps.plugin.locationcomponent.LocationConsumer
import com.mapbox.maps.plugin.locationcomponent.LocationProvider
import java.lang.ref.WeakReference

class CustomLocationProvider(
    context: Context,
    locationUpdatesInterval: Long = DEFAULT_LOCATION_UPDATES_INTERVAL,
    locationUpdatesMaxWaitTime: Long = DEFAULT_LOCATION_UPDATES_MAX_WAIT_TIME,
    private val locationEngine: LocationEngine = LocationEngineProvider.getBestLocationEngine(context),
) : LocationProvider {

    companion object {
        private const val TAG = "CustomLocationProvider"
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
                provider.get()!!.updateLocation(Point.fromLngLat(it.longitude, it.latitude, it.altitude))
                provider.get()!!.updateBearing(it.bearing.toDouble())
            }
        }

        override fun onFailure(exception: Exception) {
            Log.i(TAG, "onFailure: ${exception.localizedMessage}")
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
            throw RuntimeException(e)
        }
    }

    fun onDestroy() {
        locationEngine.removeLocationUpdates(onLocationUpdated)
        consumers.clear()
    }

    private fun updateLocation(point: Point) {
        println("CustomLocationProvider.updateLocation - ($point)")
        consumers.forEach { it.onLocationUpdated(point) }
    }

    private fun updateBearing(bearing: Double) {
        println("CustomLocationProvider.updateBearing - ($bearing)")
        consumers.forEach { it.onBearingUpdated(bearing) }
    }

    override fun registerLocationConsumer(locationConsumer: LocationConsumer) {
        consumers.add(locationConsumer)
    }

    override fun unRegisterLocationConsumer(locationConsumer: LocationConsumer) {
        consumers.remove(locationConsumer)
    }
}