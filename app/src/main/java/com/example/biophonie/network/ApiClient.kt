package com.example.biophonie.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class ApiClient {

    private val client: OkHttpClient = OkHttpClient.Builder().addInterceptor(FakeInterceptor()).build()//.apply { interceptors().add(FakeInterceptor()) }

    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        //TODO(Use Moshi instead of GSON)
        .addConverterFactory(MoshiConverterFactory.create())
        .client(client)
        .build()

    fun <T> createService(type: Class<T>): T {
        return retrofit.create(type)
    }
}