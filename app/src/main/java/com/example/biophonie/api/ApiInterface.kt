package com.example.biophonie.api

import com.example.biophonie.SoundResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface ApiInterface {
    @GET("/sound.json")
    fun getSound(@Path("name") name: String): Call<SoundResponse>
}