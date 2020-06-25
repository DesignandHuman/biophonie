package com.example.biophonie.domain

class SoundResponse {

    private val date: String? = null

    private val amplitudes: List<Int>? = null

    private val urlPhoto: String? = null

    private val urlAudio: String? = null

    fun toSound(): Sound{
        return Sound(
            this.date,
            this.amplitudes,
            this.urlPhoto,
            this.urlAudio
        )
    }
}
