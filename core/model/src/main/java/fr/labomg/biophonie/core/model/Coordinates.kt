package fr.labomg.biophonie.core.model

import kotlin.math.abs

data class Coordinates(val latitude: Double, val longitude: Double) {
    constructor(latitude: Float, longitude: Float) : this(latitude.toDouble(), longitude.toDouble())

    override fun toString(): String {
        try {
            var latSeconds = Math.round(latitude * 3600).toInt()
            val latDegrees = latSeconds / 3600
            latSeconds = abs((latSeconds % 3600).toDouble()).toInt()
            val latMinutes = latSeconds / 60
            latSeconds %= 60

            var longSeconds = Math.round(longitude * 3600).toInt()
            val longDegrees = longSeconds / 3600
            longSeconds = abs((longSeconds % 3600).toDouble()).toInt()
            val longMinutes = longSeconds / 60
            longSeconds %= 60
            val latDegree = if (latDegrees >= 0) "N" else "S"
            val lonDegrees = if (longDegrees >= 0) "E" else "W"

            return (abs(latDegrees.toDouble()).toString() +
                "°" +
                latMinutes +
                "'" +
                latSeconds +
                "\"" +
                latDegree +
                " " +
                abs(longDegrees.toDouble()) +
                "°" +
                longMinutes +
                "'" +
                longSeconds +
                "\"" +
                lonDegrees)
        } catch (e: Exception) {
            return ("" +
                String.format("%8.5f", latitude) +
                "  " +
                String.format("%8.5f", longitude))
        }
    }
}
