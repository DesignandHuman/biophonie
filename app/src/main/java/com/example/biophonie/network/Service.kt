package com.example.biophonie.network

import com.example.biophonie.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*

const val BASE_URL = "https://biophonie.fr"

interface WebService {
    @GET("/geopoint")
    suspend fun getGeoPoint(@Query("id") id: String): Response<NetworkGeoPoint>

    @Multipart
    @POST("/geopoint")
    suspend fun postNewSound(@Part("sound") sound: NetworkSound,
                             @Part soundfile: MultipartBody.Part,
                             @Part imagefile: MultipartBody.Part
    ): Response<Message>
}

data class Message(val message: String)

object GeoPointWeb {

    private val client: OkHttpClient = OkHttpClient.Builder().apply {
        if (BuildConfig.DEBUG)
            addInterceptor(FakeInterceptor())
    }.build()

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    // Configure retrofit to parse JSON and use coroutines
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .client(client)
        .build()

    val geopoints: WebService by lazy { retrofit.create(WebService::class.java) }
}