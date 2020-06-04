package com.example.biophonie

import com.google.gson.annotations.SerializedName




class SoundResponse {
    @SerializedName("name")
    private val name: String? = null

    @SerializedName("urlAudio")
    private val urlAudio: String? = null

    @SerializedName("amplitudes")
    private val amplitudes: List<Int>? = null
}
