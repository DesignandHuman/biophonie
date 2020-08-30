package com.example.biophonie.network

import com.example.biophonie.domain.GeoPoint
import com.example.biophonie.domain.Sound
import com.example.biophonie.util.coordinatesToString
import com.mapbox.mapboxsdk.geometry.LatLng
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

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

fun NetworkGeoPoint.asDomainModel(name: String, coordinates: LatLng): GeoPoint{
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