package fr.labomg.biophonie.core.utils

import android.location.Location
import kotlin.math.absoluteValue

object LocationConverter {

    fun latitudeAsDMS(latitude: Double, decimalPlace: Int): String {
        val direction = if (latitude > 0) "N" else "S"
        var strLatitude = Location.convert(latitude.absoluteValue, Location.FORMAT_SECONDS)
        strLatitude = replaceDelimiters(strLatitude, decimalPlace)
        strLatitude += "$direction "
        return strLatitude
    }

    fun longitudeAsDMS(longitude: Double, decimalPlace: Int): String {
        val direction = if (longitude > 0) "W" else "E"
        var strLongitude = Location.convert(longitude.absoluteValue, Location.FORMAT_SECONDS)
        strLongitude = replaceDelimiters(strLongitude, decimalPlace)
        strLongitude += "$direction "
        return strLongitude
    }

    private fun replaceDelimiters(str: String, decimalPlace: Int): String {
        var newStr = str
        newStr = newStr.replaceFirst(":".toRegex(), "°")
        newStr = newStr.replaceFirst(":".toRegex(), "'")
        val pointIndex = newStr.indexOf(".")
        val endIndex = pointIndex + 1 + decimalPlace
        if (endIndex < newStr.length) {
            newStr = newStr.substring(0, endIndex)
        }
        newStr += "\""
        return newStr
    }
}
