package com.example.biophonie.classes

import com.google.gson.annotations.SerializedName

class SoundResponse {
    @SerializedName("id")
    private val id: String? = null

    @SerializedName("location")
    private val location: String? = null

    @SerializedName("date")
    private val date: String? = null

    @SerializedName("amplitudes")
    private val amplitudes: List<Int>? = null

    @SerializedName("urlPhoto")
    private val urlPhoto: String? = null

    @SerializedName("urlAudio")
    private val urlAudio: String? = null

    fun toSound(): Sound{
        return Sound(
            this.id,
            this.location,
            this.date,
            this.amplitudes,
            this.urlPhoto,
            this.urlAudio
        )
    }
}
