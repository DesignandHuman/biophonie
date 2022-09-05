package com.example.biophonie.domain

import android.graphics.drawable.Drawable
import com.example.biophonie.util.LocationConverter
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

data class GeoPoint(
    var id: Int,
    var coordinates: Coordinates,
    var title: String?,
    var date: Calendar?,
    var amplitudes: List<Float>,
    var landscapePath: String,
    var soundPath: String)

data class Coordinates(val latitude: Double, val longitude: Double) {
    override fun toString() = LocationConverter.latitudeAsDMS(latitude, 4) +
            LocationConverter.longitudeAsDMS(longitude, 4)
}

data class Landscape(var image: Drawable,
                     var titre: String)

data class DialogAdapterItem(var text: String, var icon: Int){
    override fun toString(): String = text
}

/*
fun GeoPoint.dateAsCalendar(): Calendar{
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
}*/
