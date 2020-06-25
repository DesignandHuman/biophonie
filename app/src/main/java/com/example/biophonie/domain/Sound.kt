@file:Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")

package com.example.biophonie.domain

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class Sound(date: String?, amplitudes: List<Int>?, urlPhoto: String?, urlAudio: String?) {
    var date: Calendar
    var amplitudes: List<Int>
    var urlPhoto: String
    var urlAudio: String

    init {
        if (date != null){
            try {
                this.date = Calendar.getInstance().apply { time = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.FRANCE).parse(date)}
            } catch (e: ParseException) {
                e.printStackTrace()
                this.date = Calendar.getInstance().apply { time = Date(0) }
            }
        }
        else{
            this.date = Calendar.getInstance().apply { time = Date(0) }
        }
        this.amplitudes = amplitudes ?: listOf(1)
        this.urlPhoto = urlPhoto ?: "https//biophonie.fr/photos/1"
        this.urlAudio = urlAudio ?: ""
    }
}