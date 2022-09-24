package com.example.biophonie.database

import androidx.room.*
import com.example.biophonie.domain.Coordinates
import com.example.biophonie.domain.GeoPoint
import com.example.biophonie.network.NetworkAddGeoPoint
import com.example.biophonie.network.NetworkGeoPoint
import com.example.biophonie.util.LocationConverter
import com.example.biophonie.util.dateAsCalendar

@Entity
data class DatabaseGeoPoint (
    val title: String,
    val date: String,
    @TypeConverters(Converters::class)
    val amplitudes: List<Float>,
    val latitude: Double,
    val longitude: Double,
    @ColumnInfo(name = "landscape_path")
    var landscapePath: String,
    @ColumnInfo(name = "sound_path")
    val soundPath: String,
    @ColumnInfo(name = "user_id")
    val userId: Int = 0,
    @ColumnInfo(name = "remote_id")
    val remoteId: Int = 0)
{
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
}

@Entity
data class GeoPointSync (
    val id: Int,
    @ColumnInfo(name = "remote_id")
    val remoteId: Int
)

fun DatabaseGeoPoint.asDomainModel(): GeoPoint {
    return GeoPoint(
        id = remoteId,
        title = title,
        date = dateAsCalendar(date),
        amplitudes = amplitudes,
        coordinates = Coordinates(latitude,longitude), //coordinatesToString(Point.fromLngLat(latitude, longitude)),
        landscapePath = landscapePath,
        soundPath = soundPath
    )
}

fun DatabaseGeoPoint.asNetworkModel(): NetworkAddGeoPoint {
    return NetworkAddGeoPoint(
        title = title,
        longitude = longitude,
        latitude = latitude,
        date = date,
        amplitudes = amplitudes,
        pictureTemplate = if (landscapePath != "") landscapePath else null
    )
}

private const val TAG = "DatabaseEntities"
class Converters {
    @TypeConverter
    fun fromListOfFloats(list: List<Float>?): String {
        return list?.joinToString(separator = ";") { it.toString() } ?: ""
    }

    @TypeConverter
    fun toListOfFloats(string: String?): List<Float> {
        return ArrayList(string?.split(";")?.mapNotNull { it.toFloatOrNull() } ?: emptyList())
    }
}