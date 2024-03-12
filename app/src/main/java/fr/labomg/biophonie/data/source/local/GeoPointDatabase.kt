package fr.labomg.biophonie.data.source.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import fr.labomg.biophonie.BuildConfig
import fr.labomg.biophonie.data.source.Converters
import fr.labomg.biophonie.data.source.DatabaseGeoPoint

@Database(
    entities = [DatabaseGeoPoint::class],
    version = 6,
    exportSchema = BuildConfig.BUILD_TYPE != "debug"
)
@TypeConverters(Converters::class)
abstract class GeoPointDatabase : RoomDatabase() {

    abstract fun geoPointDao(): GeoPointDao
}
