package com.example.biophonie.data.source

import android.util.Log
import com.example.biophonie.data.Coordinates
import com.example.biophonie.data.GeoPoint
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

private const val TAG = "DefaultGeoPointRepository"
class DefaultGeoPointRepository(
    private val geoPointRemoteDataSource: GeoPointDataSource,
    private val geoPointLocalDataSource: GeoPointDataSource,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
    ) : GeoPointRepository {
    
    override suspend fun fetchGeoPoint(id: Int): Result<GeoPoint> {
        return with(geoPointLocalDataSource.getGeoPoint(id)) {
            if (!isFailure)
                return@with this
            else
                return@with geoPointRemoteDataSource.getGeoPoint(id).onSuccess {
                    geoPointLocalDataSource.addGeoPoint(it)
                }
        }
    }

    override suspend fun getClosestGeoPointId(coord: Coordinates, not: Array<Int>): Result<Int> {
        return geoPointRemoteDataSource.getClosestGeoPointId(coord, not)
    }

    override suspend fun getUnavailableGeoPoints(): List<GeoPoint> =
        geoPointLocalDataSource.getUnavailableGeoPoints()

    override suspend fun saveNewGeoPoint(geoPoint: GeoPoint) =
        geoPointLocalDataSource.addGeoPoint(geoPoint, true)

    override suspend fun addNewGeoPoints(): Boolean {
        var success = true
        geoPointLocalDataSource.getNewGeoPoints().forEach { geoPoint ->
            geoPointRemoteDataSource.addGeoPoint(geoPoint)
                .onSuccess { Log.d(TAG, "saveNewGeoPoints: ${geoPoint.title} posted") }
                .onFailure {
                    Log.d(TAG, "saveNewGeoPoints: could not post ${geoPoint.title}")
                    success = false
                }
        }
        return success
    }

    override suspend fun refreshUnavailableGeoPoints() {
        getUnavailableGeoPoints().forEach { geoPoint ->
            geoPointRemoteDataSource.getGeoPoint(geoPoint.remoteId)
                .onSuccess {
                    Log.d(TAG, "refreshUnavailableGeoPoints: ${geoPoint.title} posted")
                    it.remoteId = it.id
                    it.id = geoPoint.id
                    geoPointLocalDataSource.refreshGeoPoint(it)
                }
                .onFailure {
                    Log.d(TAG, "refreshUnavailableGeoPoints: ${geoPoint.title} was not enabled yet")
                }
        }
    }
}
