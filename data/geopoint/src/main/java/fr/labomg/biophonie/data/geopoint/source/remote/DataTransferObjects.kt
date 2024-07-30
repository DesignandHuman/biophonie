package fr.labomg.biophonie.data.geopoint.source.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import fr.labomg.biophonie.core.assets.templates
import fr.labomg.biophonie.data.geopoint.Coordinates
import fr.labomg.biophonie.data.geopoint.GeoPoint
import fr.labomg.biophonie.data.geopoint.Resource
import fr.labomg.biophonie.data.geopoint.source.local.DatabaseGeoPoint
import java.time.Instant

@JsonClass(generateAdapter = true)
data class NetworkGeoPoint(
    val id: Int,
    val userId: Int,
    val title: String,
    val latitude: Double,
    val longitude: Double,
    @Json(name = "createdOn") val date: String,
    val amplitudes: List<Float>,
    val sound: String,
    val picture: String
)

@JsonClass(generateAdapter = true)
data class NewNetworkGeoPoint(
    val title: String,
    val latitude: Double,
    val longitude: Double,
    val date: String,
    val amplitudes: List<Float>,
    @Json(name = "picture_template") val pictureTemplate: String?
)

@JsonClass(generateAdapter = true) data class NetworkGeoId(@Json(name = "id") val id: Int)

@JsonClass(generateAdapter = true) data class Message(val message: String)

fun NetworkGeoPoint.asDomainModel(): GeoPoint {
    return GeoPoint(
        id = 0,
        remoteId = id,
        title = title,
        coordinates = Coordinates(latitude, longitude),
        date = Instant.parse(date),
        amplitudes = amplitudes,
        picture =
            if (templates.contains(picture)) Resource(local = picture)
            else Resource(remote = picture),
        sound = Resource(remote = sound)
    )
}

fun NetworkGeoPoint.asDatabaseModel(): DatabaseGeoPoint {
    val isTemplate = templates.contains(picture.removeSuffix(".webp"))
    return DatabaseGeoPoint(
        title = title,
        latitude = latitude,
        longitude = longitude,
        date = date,
        amplitudes = amplitudes,
        picture = if (isTemplate) picture.removeSuffix(".webp") else null,
        remotePicture = if (!isTemplate) picture else null,
        remoteSound = sound,
        remoteId = id,
    )
}

fun GeoPoint.asNewNetworkGeoPoint(): NewNetworkGeoPoint {
    return NewNetworkGeoPoint(
        title = title,
        longitude = coordinates.longitude,
        latitude = coordinates.latitude,
        date = date.toString(),
        amplitudes = amplitudes,
        pictureTemplate = if (templates.contains(picture.local)) picture.local else null
    )
}
