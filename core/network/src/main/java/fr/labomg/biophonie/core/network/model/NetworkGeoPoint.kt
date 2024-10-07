package fr.labomg.biophonie.core.network.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import fr.labomg.biophonie.core.model.Coordinates
import fr.labomg.biophonie.core.model.GeoPoint
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

fun NetworkGeoPoint.toExternal(): GeoPoint {
    return GeoPoint(
        id = 0,
        remoteId = id,
        title = title,
        coordinates = Coordinates(latitude, longitude),
        date = Instant.parse(date),
        amplitudes = amplitudes,
        picture = picture,
        sound = sound
    )
}

fun GeoPoint.toNetwork(): NetworkGeoPoint {
    TODO("not implemented")
}
