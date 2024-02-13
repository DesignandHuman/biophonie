package fr.labomg.biophonie.data

import androidx.annotation.DrawableRes
import fr.labomg.biophonie.data.source.DatabaseGeoPoint
import fr.labomg.biophonie.util.LocationConverter
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

data class Resource(var remote: String? = null, var local: String? = null)

data class Coordinates(val latitude: Double, val longitude: Double) {
    override fun toString() =
        LocationConverter.latitudeAsDMS(latitude, 2) +
            LocationConverter.longitudeAsDMS(longitude, 2)
}

data class Landscape(@DrawableRes var image: Int, var titre: String)

data class DialogAdapterItem(var text: String, var icon: Int) {
    override fun toString(): String = text
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
