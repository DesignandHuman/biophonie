package com.example.biophonie.data.source

import com.example.biophonie.data.Coordinates
import com.example.biophonie.data.GeoPoint
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import timber.log.Timber

class DefaultGeoPointRepository(
    private val geoPointRemoteDataSource: GeoPointDataSource,
    private val geoPointLocalDataSource: GeoPointDataSource,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
    ) : GeoPointRepository {
    
    override suspend fun fetchGeoPoint(id: Int): Result<GeoPoint> {
        return with(geoPointLocalDataSource.getGeoPoint(id)) {
            if (isSuccess)
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
                .onSuccess { Timber.i("${geoPoint.title} posted") }
                .onFailure {
                    Timber.e("could not post ${geoPoint.title}: $it")
                    success = false
                }
        }
        return success
    }

    override suspend fun refreshUnavailableGeoPoints() {
        getUnavailableGeoPoints().forEach { geoPoint ->
            if (geoPoint.remoteId != 0)
                geoPointRemoteDataSource.getGeoPoint(geoPoint.remoteId)
                    .onSuccess {
                        Timber.i("refreshUnavailableGeoPoints: ${geoPoint.title} enabled")
                        it.remoteId = it.id
                        it.id = geoPoint.id
                        geoPointLocalDataSource.refreshGeoPoint(it)
                    }
                    .onFailure {
                        Timber.w("refreshUnavailableGeoPoints: ${geoPoint.title} was not enabled yet")
                    }
            else
                Timber.w("refreshUnavailableGeoPoints: ${geoPoint.title} not posted yet")
        }
    }
}
