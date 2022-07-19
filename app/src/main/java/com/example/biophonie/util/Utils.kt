@file:Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")

package com.example.biophonie.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import com.mapbox.geojson.Point
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

fun coordinatesToString(coordinates: Point): String{
    return LocationConverter.latitudeAsDMS(
        coordinates.latitude(),
        4
    ) + LocationConverter.longitudeAsDMS(coordinates.longitude(), 4)
}

fun getRandomString(length: Int) : String {
    val allowedChars = ('A'..'Z') + ('a'..'z') + ('1'..'9')
    return (1..length)
        .map { allowedChars.random() }
        .joinToString("")
}

fun dateAsCalendar(date: String?): Calendar{
    return if (date != null){
        try {
            Calendar.getInstance().apply {
                @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
                time = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.FRANCE).parse(date)
            }
        } catch (e: ParseException) {
            e.printStackTrace()
            Calendar.getInstance().apply { time = Date(0) }
        }
    } else{
        Calendar.getInstance().apply { time = Date(0) }
    }
}

class GPSCheck(private val locationCallBack: LocationCallBack) :
    BroadcastReceiver() {
    interface LocationCallBack {
        fun turnedOn()
        fun turnedOff()
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (isGPSEnabled(context))
            locationCallBack.turnedOn()
        else locationCallBack.turnedOff()
    }
}

fun isGPSEnabled(context: Context): Boolean{
    val locationManager =
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)||
            locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
}

fun dpToPx(context: Context, dp: Int): Int {
    return dp*(context.resources.displayMetrics.density).toInt()
}