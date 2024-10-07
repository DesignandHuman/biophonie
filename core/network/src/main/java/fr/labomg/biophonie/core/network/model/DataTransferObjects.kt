package fr.labomg.biophonie.core.network.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import fr.labomg.biophonie.core.assets.templates
import fr.labomg.biophonie.core.model.GeoPoint
import fr.labomg.biophonie.core.model.User

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

fun GeoPoint.asNewNetworkGeoPoint(): NewNetworkGeoPoint {
    val baseName = picture.substringBefore(".")
    return NewNetworkGeoPoint(
        title = title,
        longitude = coordinates.longitude,
        latitude = coordinates.latitude,
        date = date.toString(),
        amplitudes = amplitudes,
        pictureTemplate = if (templates.contains(baseName)) baseName else null
    )
}

@JsonClass(generateAdapter = true) data class NetworkAddUser(val name: String)

@JsonClass(generateAdapter = true)
data class NetworkAuthUser(
    val name: String,
    val password: String,
)

@JsonClass(generateAdapter = true) data class AccessToken(val token: String)

fun User.toNetworkAddUser(): NetworkAddUser {
    return NetworkAddUser(name)
}
