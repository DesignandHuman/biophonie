package fr.labomg.biophonie.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import fr.labomg.biophonie.core.database.dao.GeoPointDao
import fr.labomg.biophonie.core.database.model.Converters
import fr.labomg.biophonie.core.database.model.GeoPointEntity
import fr.labomg.biophonie.core.utils.BuildConfig

@Database(
    entities = [GeoPointEntity::class],
    version = 7,
    exportSchema = BuildConfig.BUILD_TYPE != "debug"
)
@TypeConverters(Converters::class)
abstract class GeoPointDatabase : RoomDatabase() {

    abstract fun geoPointDao(): GeoPointDao
}
