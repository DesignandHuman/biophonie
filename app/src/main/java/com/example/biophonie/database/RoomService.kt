package com.example.biophonie.database

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface GeoPointDao {
    @Query("select * from databasegeopoint")
    fun getNewGeoPointsAsLiveData(): LiveData<List<DatabaseGeoPoint>>

    @Query("select * from databasegeopoint")
    fun getNewGeoPoints(): List<DatabaseGeoPoint>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(geoPoint: DatabaseGeoPoint)

    @Delete
    fun delete(geoPoint: DatabaseGeoPoint)

    @Query("select * from databasegeopoint where id like :id")
    fun getNewGeoPoint(id: Int): DatabaseGeoPoint?
}

@Database(entities = [DatabaseGeoPoint::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class NewGeoPointDatabase : RoomDatabase() {

    abstract val geoPointDao: GeoPointDao

    companion object {

        @Volatile
        private var INSTANCE: NewGeoPointDatabase? = null

        fun getInstance(context: Context): NewGeoPointDatabase {
            synchronized(this) {
                var instance = INSTANCE

                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        NewGeoPointDatabase::class.java,
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