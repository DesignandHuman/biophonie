package fr.labomg.biophonie.data.geopoint

import fr.labomg.biophonie.core.utils.LocationConverter
import fr.labomg.biophonie.data.geopoint.source.local.DatabaseGeoPoint
import java.time.Instant

data class GeoPoint(
    var id: Int,
    var remoteId: Int = 0,
    var coordinates: Coordinates,
    var title: String,
    var date: Instant?,
    var amplitudes: List<Float>,
    var picture: Resource,
    var sound: Resource
)

data class NewGeoPoint(
    val title: String,
    val date: String,
    val amplitudes: List<Int>,
    var coordinates: Coordinates?,
    val soundPath: String,
    val landscapePath: String,
    val templatePath: String
)

data class Resource(var remote: String? = null, var local: String? = null)

data class Coordinates(val latitude: Double, val longitude: Double) {
    constructor(latitude: Float, longitude: Float) : this(latitude.toDouble(), longitude.toDouble())

    override fun toString() =
        LocationConverter.latitudeAsDMS(latitude, 2) +
            LocationConverter.longitudeAsDMS(longitude, 2)
}

fun GeoPoint.asDatabaseModel(fromUser: Boolean = true): DatabaseGeoPoint {
    return DatabaseGeoPoint(
        remoteId = if (!fromUser) remoteId else 0,
        title = title,
        date = date.toString(),
        amplitudes = amplitudes,
        latitude = coordinates.latitude,
        longitude = coordinates.longitude,
        picture = picture.local,
        sound = sound.local,
        remotePicture = picture.remote,
        remoteSound = sound.remote,
        available = !fromUser
    )
}
