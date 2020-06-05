package com.example.biophonie.api

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiInterface {
    @GET("/sound")
    fun getSound(@Query("id") id: String): Call<SoundResponse>
}