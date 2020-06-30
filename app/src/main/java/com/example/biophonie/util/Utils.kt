@file:Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")

package com.example.biophonie.util

import android.content.Context
import com.mapbox.mapboxsdk.geometry.LatLng
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

fun coordinatesToString(coordinates: LatLng): String{
    return LocationConverter.latitudeAsDMS(
        coordinates.latitude,
        4
    ) + LocationConverter.longitudeAsDMS(coordinates.longitude, 4)
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

private fun dpToPx(context: Context, dp: Int): Int {
    return dp*(context.resources.displayMetrics.density).toInt()
}