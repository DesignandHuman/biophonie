package fr.labomg.biophonie.data.geopoint.source

import fr.labomg.biophonie.data.geopoint.Coordinates
import fr.labomg.biophonie.data.geopoint.GeoPoint

interface GeoPointRepository {
    suspend fun fetchGeoPoint(id: Int): Result<GeoPoint>

    suspend fun getClosestGeoPointId(coord: Coordinates, not: Array<Int>): Result<Int>

    suspend fun getUnavailableGeoPoints(): List<GeoPoint>

    suspend fun addNewGeoPoints(): Boolean

    suspend fun refreshUnavailableGeoPoints()

    suspend fun saveNewGeoPoint(geoPoint: GeoPoint, dataPath: String): Result<GeoPoint>

    suspend fun cancelNetworkRequest()

    suspend fun saveAssetsInStorage(geoPoint: GeoPoint, dataPath: String)
}
