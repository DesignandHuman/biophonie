package fr.labomg.biophonie.data.source.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import fr.labomg.biophonie.data.source.DatabaseGeoPoint
import fr.labomg.biophonie.data.source.GeoPointSync

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

    // Fetching closestGeoPoint is done remotely only. Keeping for reference.
    /*@Query("select * from databasegeopoint " +
            "where id != (:excludeIds) " +
            "order by ABS(latitude - :latitude) + ABS(longitude - :longitude) ASC " +
            "limit 1")
    fun getClosestGeoPoint(latitude: Double, longitude: Double, excludeIds: Array<Int>): DatabaseGeoPoint?*/

    @Update(entity = DatabaseGeoPoint::class) fun syncGeoPoint(sync: GeoPointSync)

    @Query("UPDATE databasegeopoint SET available = 1 WHERE remote_id == :remoteId")
    fun setGeoPointAvailable(remoteId: Int)
}
