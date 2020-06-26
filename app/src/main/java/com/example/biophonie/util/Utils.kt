@file:Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")

package com.example.biophonie.util

import com.mapbox.mapboxsdk.geometry.LatLng
import java.text.SimpleDateFormat
import java.util.*

fun coordinatesToString(coordinates: LatLng): String{
    return LocationConverter.latitudeAsDMS(
        coordinates.latitude,
        4
    ) + LocationConverter.longitudeAsDMS(coordinates.longitude, 4)
}

fun dateToCalendar(date: String): Calendar{
    return Calendar.getInstance().apply { time = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.FRANCE).parse(date)}
}

enum class Status {
    SUCCESS,
    ERROR,
    LOADING
}

data class Resource<out T>(val status: Status, val data: T?, val message: String?) {
    companion object {
        fun <T> success(data: T): Resource<T> = Resource(status = Status.SUCCESS, data = data, message = null)

        fun <T> error(data: T?, message: String): Resource<T> =
            Resource(status = Status.ERROR, data = data, message = message)

        fun <T> loading(data: T?): Resource<T> = Resource(status = Status.LOADING, data = data, message = null)
    }
}