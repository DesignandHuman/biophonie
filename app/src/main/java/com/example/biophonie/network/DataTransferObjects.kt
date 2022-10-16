package com.example.biophonie.network

import com.example.biophonie.database.DatabaseGeoPoint
import com.example.biophonie.domain.Coordinates
import com.example.biophonie.domain.GeoPoint
import com.example.biophonie.domain.Resource
import com.example.biophonie.ui.templates
import com.example.biophonie.viewmodels.TutorialViewModel
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
data class NetworkAddGeoPoint(
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

fun NetworkGeoPoint.asDomainModel(): GeoPoint{
    return GeoPoint(
        id = id,
        title = title,
        coordinates = Coordinates(latitude,longitude),
        date = Instant.parse(date),
        amplitudes = amplitudes,
        picture = if (templates.containsKey(picture)) Resource(local = picture) else Resource(remote = picture),
        sound = Resource(remote = sound)
    )
}

fun NetworkGeoPoint.asDatabaseModel(): DatabaseGeoPoint{
    return DatabaseGeoPoint(
        title = title,
        latitude = latitude,
        longitude = longitude,
        date = date,
        amplitudes = amplitudes,
        remotePicture = picture,
        remoteSound = sound,
        remoteId = id,
    )
}
