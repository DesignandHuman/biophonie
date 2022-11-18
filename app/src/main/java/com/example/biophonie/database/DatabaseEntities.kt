package com.example.biophonie.database

import androidx.room.*
import com.example.biophonie.domain.Coordinates
import com.example.biophonie.domain.GeoPoint
import com.example.biophonie.domain.Resource
import com.example.biophonie.network.NetworkAddGeoPoint
import com.example.biophonie.templates
import java.time.Instant

@Entity
data class DatabaseGeoPoint (
    val title: String,
    val date: String,
    @TypeConverters(Converters::class)
    val amplitudes: List<Float>,
    val latitude: Double,
    val longitude: Double,
    @ColumnInfo(name = "picture")
    val picture: String? = null,
    @ColumnInfo(name = "sound")
    val sound: String? = null,
    @ColumnInfo(name = "remote_picture")
    val remotePicture: String? = null,
    @ColumnInfo(name = "remote_sound")
    val remoteSound: String? = null,
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
    val remoteId: Int,
    @ColumnInfo(name = "remote_picture")
    val remotePicture: String,
    @ColumnInfo(name = "remote_sound")
    val remoteSound: String,
)

fun DatabaseGeoPoint.asDomainModel(): GeoPoint {
    return GeoPoint(
        id = remoteId,
        title = title,
        date = Instant.parse(date),
        amplitudes = amplitudes,
        coordinates = Coordinates(latitude,longitude),
        picture = Resource(remote = remotePicture, local = picture),
        sound = Resource(remote = remoteSound, local = sound)
    )
}

fun DatabaseGeoPoint.asNetworkModel(): NetworkAddGeoPoint {
    return NetworkAddGeoPoint(
        title = title,
        longitude = longitude,
        latitude = latitude,
        date = date,
        amplitudes = amplitudes,
        pictureTemplate = if (templates.contains(picture)) picture else null
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