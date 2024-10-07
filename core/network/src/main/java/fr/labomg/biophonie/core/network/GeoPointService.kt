package fr.labomg.biophonie.core.network

import fr.labomg.biophonie.core.network.model.Message
import fr.labomg.biophonie.core.network.model.NetworkGeoId
import fr.labomg.biophonie.core.network.model.NetworkGeoPoint
import fr.labomg.biophonie.core.network.model.NewNetworkGeoPoint
import okhttp3.MultipartBody
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface GeoPointService {

    @GET("/api/v1/geopoint/{id}")
    suspend fun getGeoPoint(
        @Path("id") id: Int,
    ): Result<NetworkGeoPoint>

    @GET("/api/v1/restricted/ping") suspend fun pingRestricted(): Result<Message>

    @GET("/api/v1/geopoint/closest/to/{latitude}/{longitude}")
    suspend fun getClosestGeoPoint(
        @Path("latitude") latitude: Double,
        @Path("longitude") longitude: Double,
        @Query("not[]") not: Array<Int>,
    ): Result<NetworkGeoId>

    @Multipart
    @POST("/api/v1/restricted/geopoint")
    suspend fun postNewGeoPoint(
        @Part("geopoint") geoPoint: NewNetworkGeoPoint,
        @Part sound: MultipartBody.Part,
        @Part picture: MultipartBody.Part?,
    ): Result<NetworkGeoPoint>
}
