package fr.labomg.biophonie.core.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.location.LocationManager

class GPSCheck(private val locationCallBack: LocationCallBack) : BroadcastReceiver() {
    interface LocationCallBack {
        fun turnedOn()

        fun turnedOff()
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (isGPSEnabled(context)) locationCallBack.turnedOn() else locationCallBack.turnedOff()
    }
}

fun isGPSEnabled(context: Context): Boolean {
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
        locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
}
