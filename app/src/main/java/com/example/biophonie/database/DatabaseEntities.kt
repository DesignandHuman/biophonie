package com.example.biophonie.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import com.example.biophonie.domain.GeoPoint
import com.example.biophonie.domain.Sound
import java.util.*

@Entity
data class DatabaseNewSound (
    val title: String,
    val date: String,
    val amplitudes: List<Int>,
    val coordinates: String,

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