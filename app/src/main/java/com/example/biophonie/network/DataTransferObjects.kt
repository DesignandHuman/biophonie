package com.example.biophonie.network

import com.example.biophonie.database.DatabaseGeoPoint
import com.example.biophonie.domain.Coordinates
import com.example.biophonie.domain.GeoPoint
import com.example.biophonie.util.dateAsCalendar
import com.example.biophonie.viewmodels.TutorialViewModel
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class NetworkGeoPoint(
    val id: Int,
    val userId: Int?,
    val title: String?,
    val latitude: Double,
    val longitude: Double,
    @Json(name="createdOn")
    val date: String?,
    val amplitudes: List<Float>?,
    val sound: String?,
    val picture: String?
)

@JsonClass(generateAdapter = true)
data class NetworkAddUser(val name: String)

@JsonClass(generateAdapter = true)
data class NetworkUser(val userId: Int,
                       val admin: Boolean,
                       val createdOn: String,
                       val name: String,
                       val password: String,
)

fun NetworkUser.asDomainModel(): TutorialViewModel.User {
    return TutorialViewModel.User(name, userId, password)
}

fun NetworkGeoPoint.asDomainModel(): GeoPoint{
    return GeoPoint(
        id = id,
        title = title,
        coordinates = Coordinates(latitude,longitude),
        date = dateAsCalendar(date),
        amplitudes = amplitudes ?: listOf(1f),
        landscapePath = picture ?: "", //TODO
        soundPath = sound ?: "" //TODO
    )
}

fun DatabaseGeoPoint.asNetworkModel(): NetworkGeoPoint {
    return NetworkGeoPoint(
        id = id,
        userId = null,
        title = title,
        longitude = longitude,
        latitude = latitude,
        date = date,
        amplitudes = amplitudes,
        picture = landscapePath,
        sound = soundPath,
    )
}