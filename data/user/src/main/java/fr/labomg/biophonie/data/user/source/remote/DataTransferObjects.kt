package fr.labomg.biophonie.data.user.source.remote

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true) data class NetworkAddUser(val name: String)

@JsonClass(generateAdapter = true) data class Message(val message: String)

@JsonClass(generateAdapter = true)
data class NetworkAuthUser(
    val name: String,
    val password: String,
)

@JsonClass(generateAdapter = true) data class AccessToken(val token: String)

@JsonClass(generateAdapter = true)
data class User(
    val id: Int? = null,
    val admin: Boolean? = null,
    val createdOn: String? = null,
    val name: String,
    val password: String? = null,
    var token: String? = null
)

fun User.toNetworkAddUser(): NetworkAddUser {
    return NetworkAddUser(name)
}
