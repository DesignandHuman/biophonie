package fr.labomg.biophonie.data.source.local

import fr.labomg.biophonie.data.Coordinates
import fr.labomg.biophonie.data.GeoPoint
import fr.labomg.biophonie.data.asDatabaseModel
import fr.labomg.biophonie.data.source.GeoPointDataSource
import fr.labomg.biophonie.data.source.GeoPointSync
import fr.labomg.biophonie.data.source.asDomainModel
import fr.labomg.biophonie.data.source.remote.Message
import fr.labomg.biophonie.di.IoDispatcher
import fr.labomg.biophonie.templates
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeoPointLocalDataSource @Inject constructor(
    private val geoPointDao: GeoPointDao,
    @IoDispatcher private val dispatcher: CoroutineDispatcher,
) : GeoPointDataSource {

    override suspend fun getGeoPoint(id: Int): Result<GeoPoint> =
        withContext(dispatcher) {
            val geoPoint =
                if (id > 0) geoPointDao.getGeoPoint(id) else geoPointDao.getNewGeoPoint(-id)
            if (geoPoint != null) Result.success(geoPoint.asDomainModel())
            else Result.failure(Exception("geoPoint not found"))
        }

    override suspend fun refreshGeoPoint(geoPoint: GeoPoint) =
        withContext(dispatcher) {
            geoPointDao.syncGeoPoint(
                GeoPointSync(
                    id = geoPoint.id,
                    remoteId = geoPoint.remoteId,
                    remoteSound = geoPoint.sound.remote,
                    remotePicture = geoPoint.picture.remote
                )
            )
        }

    override suspend fun pingRestricted(): Result<Message> {
        // NO-OP
        return Result.success(Message(""))
    }

    override suspend fun makeAvailable(geoPoint: GeoPoint) {
        geoPointDao.setGeoPointAvailable(geoPoint.remoteId)
    }

    override suspend fun cancelCurrentJob() {
        // NO-OP
    }

    override suspend fun getClosestGeoPointId(coord: Coordinates, not: Array<Int>): Result<Int> {
        // NO-OP
        return Result.failure(Exception(""))
    }

    override suspend fun getNewGeoPoints(): List<GeoPoint> =
        withContext(dispatcher) { geoPointDao.getNewGeoPoints().map { it.asDomainModel() } }

    override suspend fun getUnavailableGeoPoints(): List<GeoPoint> =
        withContext(dispatcher) { geoPointDao.getUnavailableGeoPoints().map { it.asDomainModel() } }

    override suspend fun addGeoPoint(geoPoint: GeoPoint, fromUser: Boolean): Result<GeoPoint> =
        withContext(dispatcher) {
            if (templates.contains(geoPoint.picture.remote?.removeSuffix(".webp")))
                geoPoint.picture.local = geoPoint.picture.remote?.removeSuffix(".webp")
            geoPointDao.insert(geoPoint.asDatabaseModel(fromUser))
            Result.success(geoPoint)
        }
}
