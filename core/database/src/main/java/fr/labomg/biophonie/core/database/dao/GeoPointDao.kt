package fr.labomg.biophonie.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import fr.labomg.biophonie.core.database.model.GeoPointEntity

@Dao
interface GeoPointDao {
    @Query("select * from geopointentity where remote_id == 0")
    fun getNewGeoPoints(): List<GeoPointEntity>

    @Query("select * from geopointentity where not available")
    fun getUnavailableGeoPoints(): List<GeoPointEntity>

    @Upsert fun upsert(geoPoint: GeoPointEntity)

    @Query("select * from geopointentity where id == :id")
    fun getNewGeoPoint(id: Int): GeoPointEntity?

    @Query("select * from geopointentity where remote_id == :remoteId")
    fun getGeoPoint(remoteId: Int): GeoPointEntity?
}
