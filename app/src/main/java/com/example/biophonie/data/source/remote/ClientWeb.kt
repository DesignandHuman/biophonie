package com.example.biophonie.data.source.remote

import com.example.biophonie.BASE_URL
import com.example.biophonie.data.source.ResultCallAdapterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object ClientWeb {
    private val client: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
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
        .addCallAdapterFactory(ResultCallAdapterFactory())
        .client(client)
        .build()

    val webService: WebService by lazy { retrofit.create(WebService::class.java) }
}