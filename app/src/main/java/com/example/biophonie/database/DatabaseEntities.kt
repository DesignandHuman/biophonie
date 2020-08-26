package com.example.biophonie.database

import androidx.room.*
import com.example.biophonie.domain.Sound
import java.util.*

@Entity
data class DatabaseNewSound (
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String?,
    val date: String?,
    @TypeConverters(Converters::class)
    val amplitudes: List<Int>,
    val coordinates: String?,

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
            coordinates = it.coordinates,
            landscapePath = it.landscapePath,
            soundPath = it.soundPath
        )
    }
}

fun Sound.asDatabaseModel(): DatabaseNewSound {
    return DatabaseNewSound(
        id = 0,
        title = this.title,
        date = this.date,
        amplitudes = this.amplitudes,
        coordinates = this.coordinates,
        landscapePath = this.landscapePath,
        soundPath = this.soundPath)
}

class Converters {
    @TypeConverter
    fun stringToList(value: String): List<Int> {
        val list = value.split(",")
        return list.map{ it.toInt() }
    }

    @TypeConverter
    fun listToString(list: List<Int>): String {
        var value = ""
        for (i in list) value += "$i,"
        return value
    }
}