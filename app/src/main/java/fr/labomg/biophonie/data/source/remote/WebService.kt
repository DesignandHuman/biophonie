package fr.labomg.biophonie.data.source.remote

import okhttp3.MultipartBody
import retrofit2.http.*

interface WebService {
    @POST("/api/v1/user")
    suspend fun postUser(@Body user: NetworkAddUser): Result<NetworkUser>

    @GET("/api/v1/geopoint/{id}")
    suspend fun getGeoPoint(@Path("id") id: Int): Result<NetworkGeoPoint>

    @GET("/api/v1/restricted/ping")
    suspend fun pingRestricted(): Result<Message>

    @POST("/api/v1/user/authorize")
    suspend fun refreshToken(@Body user: NetworkAuthUser): Result<AccessToken>

    @GET("/api/v1/geopoint/closest/to/{latitude}/{longitude}")
    suspend fun getClosestGeoPoint(@Path("latitude") latitude: Double,
                         @Path("longitude") longitude: Double,
                         @Query("not[]") not: Array<Int>): Result<NetworkGeoId>

    @Multipart
    @POST("/api/v1/restricted/geopoint")
    suspend fun postNewGeoPoint(@Part("geopoint") geoPoint: NewNetworkGeoPoint,
                                @Part sound: MultipartBody.Part,
                                @Part picture: MultipartBody.Part?
    ): Result<NetworkGeoPoint>
}
