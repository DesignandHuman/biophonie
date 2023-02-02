package com.example.biophonie.network

import com.example.biophonie.data.Coordinates
import com.example.biophonie.data.GeoPoint
import com.example.biophonie.data.Resource
import com.example.biophonie.data.source.DatabaseGeoPoint
import com.example.biophonie.templates
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.time.Instant

@JsonClass(generateAdapter = true)
data class NetworkGeoPoint(
    val id: Int,
    val userId: Int,
    val title: String,
    val latitude: Double,
    val longitude: Double,
    @Json(name="createdOn")
    val date: String,
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
    @Json(name="picture_template")
    val pictureTemplate: String?
)

@JsonClass(generateAdapter = true)
data class NetworkGeoId(@Json(name="id") val id: Int)

@JsonClass(generateAdapter = true)
data class NetworkAddUser(val name: String)

@JsonClass(generateAdapter = true)
data class NetworkUser(
    val userId: Int,
    val admin: Boolean,
    val createdOn: String,
    val name: String,
    val password: String,
)

@JsonClass(generateAdapter = true)
data class Message(val message: String)

@JsonClass(generateAdapter = true)
data class NetworkAuthUser(
    val name: String,
    val password: String,
)

@JsonClass(generateAdapter = true)
data class AccessToken(val token: String)

fun NetworkGeoPoint.asDomainModel(): GeoPoint {
    return GeoPoint(
        id = id,
        title = title,
        coordinates = Coordinates(latitude,longitude),
        date = Instant.parse(date),
        amplitudes = amplitudes,
        picture = if (templates.contains(picture)) Resource(local = picture) else Resource(remote = picture),
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
        picture = if(isTemplate) picture.removeSuffix(".webp") else null,
        remotePicture = if(!isTemplate) picture else null,
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
