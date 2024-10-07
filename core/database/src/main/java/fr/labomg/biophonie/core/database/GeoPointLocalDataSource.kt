package fr.labomg.biophonie.core.database

/*@Singleton
class GeoPointLocalDataSource
@Inject
constructor(
    private val geoPointDao: GeoPointDao,
    @IoDispatcher private val dispatcher: CoroutineDispatcher,
) {

    suspend fun getGeoPoint(id: Int): Result<GeoPointEntity> =
        withContext(dispatcher) {
            val geoPoint =
                if (id > 0) geoPointDao.getGeoPoint(id) else geoPointDao.getNewGeoPoint(-id)
            if (geoPoint != null) Result.success(geoPoint)
            else Result.failure(Exception("geoPoint not found"))
        }

    suspend fun refreshGeoPoint(geoPoint: GeoPoint) =
        withContext(dispatcher) {
            geoPointDao.upsert(
                GeoPointEntity(
                    id = geoPoint.id,
                    remoteId = geoPoint.remoteId,
                    remoteSound = geoPoint.sound,
                    remotePicture = geoPoint.picture
                )
            )
        }

    suspend fun getNewGeoPoints(): List<GeoPoint> =
        withContext(dispatcher) { geoPointDao.getNewGeoPoints().map { it.toExternal() } }

    suspend fun getUnavailableGeoPoints(): List<GeoPoint> =
        withContext(dispatcher) { geoPointDao.getUnavailableGeoPoints().map { it.toExternal() } }

    suspend fun addGeoPoint(geoPoint: GeoPoint, fromUser: Boolean): Result<GeoPoint> =
        withContext(dispatcher) {
            if (templates.contains(geoPoint.picture.remote?.removeSuffix(".webp")))
                geoPoint.picture.local = geoPoint.picture.remote?.removeSuffix(".webp")
            geoPointDao.insert(geoPoint.asDatabaseModel(fromUser))
            Result.success(geoPoint)
        }
}*/
