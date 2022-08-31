package com.example.biophonie.network

import com.example.biophonie.database.DatabaseGeoPoint
import com.example.biophonie.domain.GeoPoint
import com.example.biophonie.util.coordinatesToString
import com.example.biophonie.util.dateAsCalendar
import com.example.biophonie.viewmodels.TutorialViewModel
import com.mapbox.geojson.Point
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.text.SimpleDateFormat
import java.util.*

@JsonClass(generateAdapter = true)
data class NetworkGeoPoint(
    val id: Int,
    val userId: Int?,
    val title: String?,
    val location: Coordinates,
    @Json(name="createdOn")
    val date: String?,
    val amplitudes: List<Int>?,
    val sound: String?,
    val picture: String?
)

data class Coordinates(
    @Json(name="X")
    val x: Double,
    @Json(name="Y")
    val y: Double
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

fun NetworkGeoPoint.asDomainModel(name: String, coordinates: Point): GeoPoint{
    return GeoPoint(
        id = id,
        name = name,
        coordinates = coordinatesToString(coordinates),
        title = title,
        date = dateAsCalendar(date),
        amplitudes = amplitudes ?: listOf(1),
        landscapePath = picture ?: "", //TODO
        soundPath = sound ?: "" //TODO
    )
}

fun DatabaseGeoPoint.asNetworkModel(): NetworkGeoPoint {
    return NetworkGeoPoint(
        id = id,
        userId = null,
        title = title,
        location = Coordinates(longitude,latitude),
        date = date,
        amplitudes = amplitudes,
        picture = landscapePath,
        sound = soundPath,
    )
}