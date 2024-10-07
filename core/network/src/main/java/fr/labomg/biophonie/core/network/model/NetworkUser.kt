package fr.labomg.biophonie.core.network.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import fr.labomg.biophonie.core.model.User

@JsonClass(generateAdapter = true)
data class NetworkUser(
    @Json(name = "userId") val id: Int,
    val name: String,
    val password: String,
    @Json(ignore = true) val token: String = ""
)

fun NetworkUser.toExternal() = User(id = id, name = name, password = password, token = token)

fun User.toNetwork() = NetworkUser(id = id, name = name, password = password, token = token)
