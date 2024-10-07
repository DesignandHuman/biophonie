package fr.labomg.biophonie.core.database.model

import android.annotation.SuppressLint
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import fr.labomg.biophonie.core.model.Coordinates
import fr.labomg.biophonie.core.model.GeoPoint
import java.time.Instant

@Entity
data class GeoPointEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val date: String,
    @TypeConverters(Converters::class) val amplitudes: List<Float>,
    val latitude: Double,
    val longitude: Double,
    @ColumnInfo(name = "picture") val picture: String? = null,
    @ColumnInfo(name = "sound") val sound: String? = null,
    @ColumnInfo(name = "remote_picture") val remotePicture: String? = null,
    @ColumnInfo(name = "remote_sound") val remoteSound: String? = null,
    @ColumnInfo(name = "user_id") val userId: Int = 0,
    @ColumnInfo(name = "remote_id") val remoteId: Int = 0,
    @ColumnInfo(name = "available") val available: Boolean = true
)

// solved by lib desugaring
@SuppressLint("NewApi")
fun GeoPointEntity.toExternal(): GeoPoint {
    return GeoPoint(
        id = id,
        remoteId = remoteId,
        title = title,
        date = Instant.parse(date),
        amplitudes = amplitudes,
        coordinates = Coordinates(latitude, longitude),
        picture = picture ?: remotePicture!!,
        sound = sound ?: remoteSound!!
    )
}

fun GeoPoint.toEntity(fromUser: Boolean = false): GeoPointEntity {
    return GeoPointEntity(
        id = id,
        remoteId = if (!fromUser) remoteId else 0,
        title = title,
        date = date.toString(),
        amplitudes = amplitudes,
        latitude = coordinates.latitude,
        longitude = coordinates.longitude,
        picture = if (fromUser) picture else null,
        sound = if (fromUser) sound else null,
        remotePicture = if (!fromUser) picture else null,
        remoteSound = if (!fromUser) sound else null,
        available = !fromUser
    )
}

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
