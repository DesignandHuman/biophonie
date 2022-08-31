package com.example.biophonie.database

import androidx.room.*
import com.example.biophonie.domain.GeoPoint
import com.example.biophonie.util.LocationConverter
import com.example.biophonie.util.dateAsCalendar

@Entity
data class DatabaseGeoPoint (
    val title: String,
    val date: String,
    @TypeConverters(Converters::class)
    val amplitudes: List<Int>,
    val latitude: Double,
    val longitude: Double,

    @ColumnInfo(name = "landscape_path")
    val landscapePath: String,
    @ColumnInfo(name = "sound_path")
    val soundPath: String) {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
}

fun DatabaseGeoPoint.asDomainModel(): GeoPoint {
    return GeoPoint(
        id = id,
        title = title,
        date = dateAsCalendar(date),
        amplitudes = amplitudes,
        coordinates = LocationConverter.latitudeAsDMS(latitude,4)+LocationConverter.longitudeAsDMS(longitude, 4), //coordinatesToString(Point.fromLngLat(latitude, longitude)),
        landscapePath = landscapePath,
        soundPath = soundPath
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