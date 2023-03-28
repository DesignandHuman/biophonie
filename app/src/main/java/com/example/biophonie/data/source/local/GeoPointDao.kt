package com.example.biophonie.data.source.local

import androidx.room.*
import com.example.biophonie.data.source.DatabaseGeoPoint
import com.example.biophonie.data.source.GeoPointAvailable
import com.example.biophonie.data.source.GeoPointSync

@Dao
interface GeoPointDao {
    @Query("select * from databasegeopoint where remote_id == 0")
    fun getNewGeoPoints(): List<DatabaseGeoPoint>

    @Query("select * from databasegeopoint where not available")
    fun getUnavailableGeoPoints(): List<DatabaseGeoPoint>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(geoPoint: DatabaseGeoPoint)

    @Query("select * from databasegeopoint where id == :id")
    fun getNewGeoPoint(id: Int): DatabaseGeoPoint?

    @Query("select * from databasegeopoint where remote_id == :remoteId")
    fun getGeoPoint(remoteId: Int): DatabaseGeoPoint?

    /*@Query("select * from databasegeopoint where id != (:excludeIds) order by ABS(latitude - :latitude) + ABS(longitude - :longitude) ASC limit 1")
    fun getClosestGeoPoint(latitude: Double, longitude: Double, excludeIds: Array<Int>): DatabaseGeoPoint?*/

    @Update(entity = DatabaseGeoPoint::class)
    fun syncGeoPoint(sync: GeoPointSync)

    @Update(entity = DatabaseGeoPoint::class)
    fun setGeoPointAvailable(geoPoint: GeoPointAvailable)
}