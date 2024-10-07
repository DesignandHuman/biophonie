package fr.labomg.biophonie.core.network

import fr.labomg.biophonie.core.network.model.AccessToken
import fr.labomg.biophonie.core.network.model.Message
import fr.labomg.biophonie.core.network.model.NetworkAddUser
import fr.labomg.biophonie.core.network.model.NetworkAuthUser
import fr.labomg.biophonie.core.network.model.NetworkUser
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface UserService {
    @POST("/api/v1/user")
    suspend fun postUser(
        @Body user: NetworkAddUser,
    ): Result<NetworkUser>

    @GET("/api/v1/restricted/ping") suspend fun pingRestricted(): Result<Message>

    @POST("/api/v1/user/authorize")
    suspend fun refreshToken(
        @Body user: NetworkAuthUser,
    ): Result<AccessToken>
}
