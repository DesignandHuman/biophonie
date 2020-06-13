package com.example.biophonie.api

import com.example.biophonie.classes.GeoPointResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiInterface {
    @GET("/geopoint")
    fun getGeoPoint(@Query("id") id: String): Call<GeoPointResponse>
}