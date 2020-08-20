package com.example.biophonie.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class DatabaseNewSound (
    @PrimaryKey(autoGenerate = true)
    val pointId: Int = 0,

    @ColumnInfo(name = "sound_path")
    val soundPath: String,

    @ColumnInfo(name = "landscape_path")
    val landscapePath: String,

    val amplitudes: ArrayList<Int>,
    val title: String)