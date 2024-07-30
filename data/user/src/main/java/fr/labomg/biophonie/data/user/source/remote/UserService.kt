package fr.labomg.biophonie.data.user.source.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface UserService {
    @POST("/api/v1/user")
    suspend fun postUser(
        @Body user: NetworkAddUser,
    ): Result<User>

    @GET("/api/v1/restricted/ping") suspend fun pingRestricted(): Result<Message>

    @POST("/api/v1/user/authorize")
    suspend fun refreshToken(
        @Body user: NetworkAuthUser,
    ): Result<AccessToken>
}
