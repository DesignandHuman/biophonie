package fr.labomg.biophonie.core.data

import fr.labomg.biophonie.core.model.Coordinates
import fr.labomg.biophonie.core.model.GeoPoint
import kotlinx.coroutines.flow.Flow

interface GeoPointRepository {
    suspend fun fetchGeoPoint(id: Int): Result<GeoPoint>

    suspend fun getClosestGeoPointId(coord: Coordinates, not: Array<Int>): Result<Int>

    fun getGeopointStream(id: Int): Flow<GeoPoint>

    suspend fun getUnavailableGeoPoints(): List<GeoPoint>

    fun getUnavailableGeoPointsStream(): Flow<List<GeoPoint>>

    suspend fun addNewGeoPoints(): Boolean

    suspend fun refreshUnavailableGeoPoints()

    suspend fun saveNewGeoPoint(geoPoint: GeoPoint)
}
