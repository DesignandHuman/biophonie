package fr.labomg.biophonie.data.geopoint.source.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import fr.labomg.biophonie.core.utils.BuildConfig

@Database(
    entities = [DatabaseGeoPoint::class],
    version = 6,
    exportSchema = BuildConfig.BUILD_TYPE != "debug"
)
@TypeConverters(Converters::class)
abstract class GeoPointDatabase : RoomDatabase() {

    abstract fun geoPointDao(): GeoPointDao
}
