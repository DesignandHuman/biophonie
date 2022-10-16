package com.example.biophonie.domain

import android.graphics.drawable.Drawable
import com.example.biophonie.util.LocationConverter
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.*

data class GeoPoint(
    var id: Int,
    var coordinates: Coordinates,
    var title: String?,
    var date: Instant?,
    var amplitudes: List<Float>,
    var picture: Resource,
    var sound: Resource)

data class Resource(
    val remote: String? = null,
    val local: String? = null
)

data class Coordinates(val latitude: Double, val longitude: Double) {
    override fun toString() = LocationConverter.latitudeAsDMS(latitude, 4) +
            LocationConverter.longitudeAsDMS(longitude, 4)
}

data class Landscape(var image: Drawable,
                     var titre: String)

data class DialogAdapterItem(var text: String, var icon: Int){
    override fun toString(): String = text
}
