package com.example.biophonie.database

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface GeoPointDao {
    @Query("select * from databasegeopoint where remote_id == 0")
    fun getNewGeoPointsAsLiveData(): LiveData<List<DatabaseGeoPoint>> //TODO(delete)

    @Query("select * from databasegeopoint where remote_id == 0")
    fun getNewGeoPoints(): List<DatabaseGeoPoint>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(geoPoint: DatabaseGeoPoint)

    @Query("select * from databasegeopoint where id == :id")
    fun getNewGeoPoint(id: Int): DatabaseGeoPoint?

    @Query("select * from databasegeopoint where remote_id == :remoteId")
    fun getGeoPoint(remoteId: Int): DatabaseGeoPoint?

    @Update(entity = DatabaseGeoPoint::class)
    fun syncGeoPoint(sync: GeoPointSync)
}

@Database(entities = [DatabaseGeoPoint::class], version = 2, exportSchema = false)
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