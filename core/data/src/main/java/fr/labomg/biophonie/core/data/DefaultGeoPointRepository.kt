package fr.labomg.biophonie.core.data

import fr.labomg.biophonie.core.database.dao.GeoPointDao
import fr.labomg.biophonie.core.database.model.toEntity
import fr.labomg.biophonie.core.database.model.toExternal
import fr.labomg.biophonie.core.model.Coordinates
import fr.labomg.biophonie.core.model.GeoPoint
import fr.labomg.biophonie.core.network.GeoPointRemoteDataSource
import fr.labomg.biophonie.core.network.model.toExternal
import fr.labomg.biophonie.core.utils.di.IoDispatcher
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import timber.log.Timber

class DefaultGeoPointRepository
@Inject
constructor(
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val remoteDataSource: GeoPointRemoteDataSource,
    private val localDataSource: GeoPointDao
) : GeoPointRepository {

    override suspend fun fetchGeoPoint(id: Int): Result<GeoPoint> {
        return withContext(ioDispatcher) {
            val remoteGeoPoint = localDataSource.getGeoPoint(id)
            if (remoteGeoPoint != null)
                return@withContext Result.success(remoteGeoPoint.toExternal())
            val localGeoPoint = localDataSource.getNewGeoPoint(id)
            if (localGeoPoint != null) return@withContext Result.success(localGeoPoint.toExternal())
            else
                return@withContext remoteDataSource
                    .getGeoPoint(id)
                    .onSuccess { localDataSource.upsert(it.toExternal().toEntity()) }
                    .map { it.toExternal() }
        }
    }

    override suspend fun getClosestGeoPointId(coord: Coordinates, not: Array<Int>): Result<Int> {
        return remoteDataSource.getClosestGeoPointId(coord, not)
    }

    override fun getGeopointStream(id: Int): Flow<GeoPoint> {
        TODO("Not yet implemented")
    }

    override suspend fun getUnavailableGeoPoints(): List<GeoPoint> =
        withContext(ioDispatcher) {
            return@withContext localDataSource.getUnavailableGeoPoints().map { it.toExternal() }
        }

    override fun getUnavailableGeoPointsStream(): Flow<List<GeoPoint>> =
        localDataSource.observeAllUnavailable().map { it.toExternal() }

    override suspend fun saveNewGeoPoint(geoPoint: GeoPoint) =
        withContext(ioDispatcher) { localDataSource.upsert(geoPoint.toEntity(true)) }

    override suspend fun addNewGeoPoints(): Boolean =
        withContext(ioDispatcher) {
            var success = true
            val newGeoPoints = localDataSource.getNewGeoPoints()
            if (newGeoPoints.isNotEmpty()) {
                success = remoteDataSource.pingRestricted().isSuccess
                if (success) {
                    newGeoPoints.forEach { geoPoint ->
                        remoteDataSource
                            .addGeoPoint(geoPoint.toExternal())
                            .onSuccess {
                                val updatedGeoPoint =
                                    geoPoint.copy(
                                        remoteId = it.id,
                                        remotePicture = it.picture,
                                        remoteSound = it.sound
                                    )
                                localDataSource.upsert(updatedGeoPoint)
                                Timber.i("${geoPoint.title} posted")
                            }
                            .onFailure {
                                Timber.e("could not post ${geoPoint.title}: $it")
                                success = false
                            }
                    }
                }
            }
            return@withContext success
        }

    override suspend fun refreshUnavailableGeoPoints() {
        withContext(ioDispatcher) {
            getUnavailableGeoPoints().forEach { geoPoint ->
                if (geoPoint.remoteId != 0)
                    remoteDataSource
                        .getGeoPoint(geoPoint.remoteId)
                        .onSuccess {
                            localDataSource.upsert(geoPoint.toEntity().copy(available = true))
                        }
                        .onFailure { Timber.w("${geoPoint.title} was not enabled yet") }
                else Timber.w("${geoPoint.title} not posted yet")
            }
        }
    }
}
