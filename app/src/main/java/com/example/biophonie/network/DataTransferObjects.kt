package com.example.biophonie.network

import com.example.biophonie.domain.GeoPoint
import com.example.biophonie.domain.Sound
import com.example.biophonie.ui.activities.TutorialActivity
import com.example.biophonie.util.coordinatesToString
import com.example.biophonie.viewmodels.TutorialViewModel
import com.mapbox.geojson.Point
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.util.*

@JsonClass(generateAdapter = true)
data class NetworkSound(val title: String?,
                        val coordinates: List<Double>?,
                        val date: String?,
                        var amplitudes: List<Int>?,
                        @Json(name = "url_photo") var urlPhoto: String?,
                        @Json(name = "url_audio")var urlAudio: String?)

@JsonClass(generateAdapter = true)
data class NetworkGeoPoint(var id: String,
                    var sounds: List<NetworkSound>)

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
    return GeoPoint(id, name,
        coordinatesToString(coordinates),
        sounds.map {
            Sound(
                title = it.title,
                coordinates = null,
                date = it.date,
                amplitudes = it.amplitudes ?: listOf(1),
                landscapePath = it.urlPhoto ?: "https//biophonie.fr/photos/1",
                soundPath = it.urlAudio ?: "")})
}