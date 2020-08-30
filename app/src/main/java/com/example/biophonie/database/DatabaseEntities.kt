package com.example.biophonie.database

import android.util.Log
import androidx.core.util.Pair
import androidx.room.*
import com.example.biophonie.domain.Sound
import com.example.biophonie.network.NetworkSound
import com.example.biophonie.util.LocationConverter
import java.util.*

@Entity
data class DatabaseNewSound (
    @PrimaryKey
    val id: String,
    val title: String,
    val date: String,
    @TypeConverters(Converters::class)
    val amplitudes: List<Int>,
    val latitude: Double,
    val longitude: Double,

    @ColumnInfo(name = "landscape_path")
    val landscapePath: String,
    @ColumnInfo(name = "sound_path")
    val soundPath: String)

fun List<DatabaseNewSound>.asDomainModel(): List<Sound> {
    return map {
        Sound(
            title = it.title,
            date = it.date,
            amplitudes = it.amplitudes,
            coordinates = LocationConverter.latitudeAsDMS(it.latitude,4)+LocationConverter.longitudeAsDMS(it.longitude, 4),
            landscapePath = it.landscapePath,
            soundPath = it.soundPath
        )
    }
}

fun DatabaseNewSound.asNetworkModel(): NetworkSound {
    return NetworkSound(
        title = title,
        coordinates = listOf(latitude, longitude),
        date = date,
        amplitudes = amplitudes,
        urlAudio = soundPath,
        urlPhoto = landscapePath
    )
}

private const val TAG = "DatabaseEntities"
class Converters {
    @TypeConverter
    fun stringToList(value: String): List<Int> {
        val list = value.split(",")
        return list.map{ if(it.isEmpty()) 0 else it.toInt()}
    }

    @TypeConverter
    fun listToString(list: List<Int>): String {
        var value = ""
        for (i in list) value += "$i,"
        return value
    }
}