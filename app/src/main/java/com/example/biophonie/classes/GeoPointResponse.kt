package com.example.biophonie.classes

import android.content.ContentValues.TAG
import android.util.Log
import com.google.gson.annotations.SerializedName

class GeoPointResponse {
    @SerializedName("id")
    private val id: String? = null

    @SerializedName("sounds")
    private val sounds: List<SoundResponse>? = null

    fun toGeoPoint(): GeoPoint{
        return GeoPoint(id, sounds?.map { it.toSound() })
    }
}