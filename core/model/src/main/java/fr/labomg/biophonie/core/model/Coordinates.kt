package fr.labomg.biophonie.core.model

import fr.labomg.biophonie.core.utils.LocationConverter

data class Coordinates(val latitude: Double, val longitude: Double) {
    constructor(latitude: Float, longitude: Float) : this(latitude.toDouble(), longitude.toDouble())

    override fun toString() =
        LocationConverter.latitudeAsDMS(latitude, 2) +
            LocationConverter.longitudeAsDMS(longitude, 2)
}
