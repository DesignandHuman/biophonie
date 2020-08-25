package com.example.biophonie.domain

import android.content.ContentValues.TAG
import android.content.res.AssetFileDescriptor
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.annotation.RawRes
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
data class Sound(
    var title: String?,
    var date: String?,
    var amplitudes: List<Int>,
    var coordinates: String?,
    var landscapePath: String,
    var soundPath: String)

data class GeoPoint(var id: String?,
                    var name: String = "",
                    var coordinates: String = "",
                    var sounds: List<Sound>?)

data class Landscape(var image: Drawable,
                     var titre: String)

data class DialogAdapterItem(var text: String, var icon: Int){
    override fun toString(): String = text
}

fun Sound.dateAsCalendar(): Calendar{
    return if (date != null){
        try {
            Calendar.getInstance().apply {
                @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
                time = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.FRANCE).parse(date)
            }
        } catch (e: ParseException) {
            e.printStackTrace()
            Calendar.getInstance().apply { time = Date(0) }
        }
    } else{
        Calendar.getInstance().apply { time = Date(0) }
    }
}