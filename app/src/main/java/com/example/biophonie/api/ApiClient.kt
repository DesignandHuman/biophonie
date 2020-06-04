package com.example.biophonie.api

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

const val BASE_URL = "stuff"

class ApiClient {

    val retrofit: Retrofit = Retrofit.Builder()
    .baseUrl(BASE_URL)
    .addConverterFactory(GsonConverterFactory.create())
    .build();

    fun <T> createService(type: Class<T>): T {
        return retrofit.create(type)
    }
}