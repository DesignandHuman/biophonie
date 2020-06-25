package com.example.biophonie.network

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

const val BASE_URL = "https://stuff.com"

class ApiClient {

    private val client: OkHttpClient = OkHttpClient.Builder().addInterceptor(FakeInterceptor()).build()//.apply { interceptors().add(FakeInterceptor()) }

    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .client(client)
        .build()

    fun <T> createService(type: Class<T>): T {
        return retrofit.create(type)
    }
}