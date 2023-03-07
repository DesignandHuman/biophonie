package com.example.biophonie.data.source.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.biophonie.data.source.Converters
import com.example.biophonie.data.source.DatabaseGeoPoint

@Database(entities = [DatabaseGeoPoint::class], version = 6, exportSchema = false /* true in prod */)
@TypeConverters(Converters::class)
abstract class GeoPointDatabase : RoomDatabase() {

    abstract fun geoPointDao(): GeoPointDao
}