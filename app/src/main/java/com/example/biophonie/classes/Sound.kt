@file:Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")

package com.example.biophonie.classes

import com.example.biophonie.classes.LocationConverter.latitudeAsDMS
import com.example.biophonie.classes.LocationConverter.longitudeAsDMS
import com.example.biophonie.classes.LocationConverter.replaceDelimiters
import com.mapbox.mapboxsdk.geometry.LatLng
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class Sound {
    var id: String
    var location: String
    var date: Calendar
    var amplitudes: List<Int>
    var urlPhoto: String
    var urlAudio: String
    constructor(
        id: String?,
        location: String?,
        date: String?,
        amplitudes: List<Int>?,
        urlPhoto: String?,
        urlAudio: String?
    ) {
        this.id = id ?: "0"
        this.location = location ?: "??"

        if (date != null){
            try {
                this.date = Calendar.getInstance().apply { time = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.FRANCE).parse(date)}
            } catch (e: ParseException) {
                e.printStackTrace()
                this.date = Calendar.getInstance().apply { time = Date(0) }
            }
        }
        else{
            this.date = Calendar.getInstance().apply { time = Date(0) }
        }

        this.amplitudes = amplitudes ?: listOf(1)
        this.urlPhoto = urlPhoto ?: "https//biophonie.fr/photos/1"
        this.urlAudio = urlAudio ?: ""
    }

    var name: String = ""
    var coordinates: LatLng = LatLng()

    fun coordinatesToString(): String{
        return latitudeAsDMS(coordinates.latitude, 4) + longitudeAsDMS(coordinates.longitude, 4)
    }
}