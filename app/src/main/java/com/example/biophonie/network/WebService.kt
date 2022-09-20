package com.example.biophonie.network

import android.content.SharedPreferences
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*

const val BASE_URL = "http://10.0.2.2:8080"

interface WebService {
    @POST("/api/v1/user")
    suspend fun postUser(@Body user: NetworkAddUser): Response<NetworkUser>

    @GET("/api/v1/geopoint/{id}")
    suspend fun getGeoPoint(@Path("id") id: Int): Response<NetworkGeoPoint>

    @GET("/api/v1/restricted/ping")
    suspend fun pingRestricted(): Response<Message>

    @POST("/api/v1/user/authorize")
    suspend fun refreshToken(@Body user: NetworkAuthUser): Response<AccessToken>

    @GET("/api/v1/geopoint/closest/to/{latitude}/{longitude}")
    suspend fun getGeoId(@Path("latitude") latitude: Double,
                         @Path("longitude") longitude: Double,
                         @Query("not[]") not: Array<Int>): Response<NetworkGeoId>

    @Multipart
    @POST("/api/v1/restricted/geopoint")
    suspend fun postNewGeoPoint(@Part geoPoint: NetworkAddGeoPoint,
                                @Part sound: MultipartBody.Part,
                                @Part image: MultipartBody.Part
    ): Response<NetworkGeoPoint>
}

object ClientWeb {

    private val client: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .addInterceptor(AuthenticationInterceptor())
        .authenticator(AccessTokenAuthenticator())
        .build()

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(MoshiConverterFactory.create(moshi).asLenient())
        .client(client)
        .build()

    val webService: WebService by lazy { retrofit.create(WebService::class.java) }
}