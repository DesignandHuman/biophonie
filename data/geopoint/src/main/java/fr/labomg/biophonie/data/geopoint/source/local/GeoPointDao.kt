package fr.labomg.biophonie.data.geopoint.source.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface GeoPointDao {
    @Query("select * from databasegeopoint where remote_id == 0")
    fun getNewGeoPoints(): List<DatabaseGeoPoint>

    @Query("select * from databasegeopoint where not available")
    fun getUnavailableGeoPoints(): List<DatabaseGeoPoint>

    @Insert(onConflict = OnConflictStrategy.IGNORE) fun insert(geoPoint: DatabaseGeoPoint)

    @Query("select * from databasegeopoint where id == :id")
    fun getNewGeoPoint(id: Int): DatabaseGeoPoint?

    @Query("select * from databasegeopoint where remote_id == :remoteId")
    fun getGeoPoint(remoteId: Int): DatabaseGeoPoint?

    @Update(entity = DatabaseGeoPoint::class) fun syncGeoPoint(sync: GeoPointSync)

    @Query("UPDATE databasegeopoint SET available = 1 WHERE remote_id == :remoteId")
    fun setGeoPointAvailable(remoteId: Int)
}
