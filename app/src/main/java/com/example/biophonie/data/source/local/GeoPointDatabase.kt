package com.example.biophonie.data.source.local

import android.content.Context
import androidx.room.*
import com.example.biophonie.data.source.Converters
import com.example.biophonie.data.source.DatabaseGeoPoint

@Database(entities = [DatabaseGeoPoint::class], version = 6, exportSchema = false /* true in prod */)
@TypeConverters(Converters::class)
abstract class GeoPointDatabase : RoomDatabase() {

    abstract val geoPointDao: GeoPointDao

    companion object {

        @Volatile
        private var INSTANCE: GeoPointDatabase? = null

        fun getInstance(context: Context): GeoPointDatabase {
            synchronized(this) {
                var instance = INSTANCE

                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        GeoPointDatabase::class.java,
                        "new_geopoint_database"
                    )
                        .fallbackToDestructiveMigration()
                        .build()
                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}