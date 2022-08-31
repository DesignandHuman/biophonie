package com.example.biophonie.domain

import android.graphics.drawable.Drawable
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

data class GeoPoint(
    var id: Int,
    var name: String = "",
    var coordinates: String = "",
    var title: String?,
    var date: Calendar?,
    var amplitudes: List<Int>,
    var landscapePath: String,
    var soundPath: String)

data class Landscape(var image: Drawable,
                     var titre: String)

data class DialogAdapterItem(var text: String, var icon: Int){
    override fun toString(): String = text
}

/*
fun GeoPoint.dateAsCalendar(): Calendar{
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
}*/
