package fr.labomg.biophonie.data.source

import fr.labomg.biophonie.data.Coordinates
import fr.labomg.biophonie.data.GeoPoint
import fr.labomg.biophonie.templates
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.nio.file.Files
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.moveTo

class DefaultGeoPointRepository(
    private val geoPointRemoteDataSource: GeoPointDataSource,
    private val geoPointLocalDataSource: GeoPointDataSource,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
    ) : GeoPointRepository {

    override suspend fun cancelNetworkRequest() {
        geoPointRemoteDataSource.cancelCurrentJob()
    }

    override suspend fun saveAssetsInStorage(geoPoint: GeoPoint, dataPath: String) {
        val soundPath = Path(geoPoint.sound.local!!)
        val targetSound = Path(dataPath).resolve(soundPath.fileName)
        withContext(Dispatchers.IO) { soundPath.moveTo(targetSound) }
        geoPoint.sound.local = targetSound.absolutePathString()

        if (geoPoint.picture.local != null) {
            val picturePath = Path(geoPoint.picture.local!!)
            if (!templates.contains(picturePath.fileName.toString())) {
                val targetPicture = Path(dataPath).resolve(picturePath.fileName)
                withContext(Dispatchers.IO) {
                    picturePath.moveTo(targetPicture)
                }
                geoPoint.picture.local = targetPicture.absolutePathString()
            }
        }
    }

    override suspend fun fetchGeoPoint(id: Int): Result<GeoPoint> {
        cancelNetworkRequest()
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
        cancelNetworkRequest()
        return geoPointRemoteDataSource.getClosestGeoPointId(coord, not)
    }

    override suspend fun getUnavailableGeoPoints(): List<GeoPoint> =
        geoPointLocalDataSource.getUnavailableGeoPoints()

    override suspend fun saveNewGeoPoint(geoPoint: GeoPoint, dataPath: String): Result<GeoPoint> {
        saveAssetsInStorage(geoPoint, dataPath)
        return geoPointLocalDataSource.addGeoPoint(geoPoint, true)
    }

    override suspend fun addNewGeoPoints(): Boolean {
        var success = true
        val newGeoPoints = geoPointLocalDataSource.getNewGeoPoints()
        if (newGeoPoints.isNotEmpty()) {
            success = geoPointRemoteDataSource.pingRestricted().isSuccess
            if (success) {
                newGeoPoints.forEach { geoPoint ->
                    geoPointRemoteDataSource.addGeoPoint(geoPoint)
                        .onSuccess {
                            it.apply { id = geoPoint.id }
                            geoPointLocalDataSource.refreshGeoPoint(it)
                            Timber.i("${geoPoint.title} posted")
                        }
                        .onFailure {
                            Timber.e("could not post ${geoPoint.title}: $it")
                            success = false
                        }
                }
            }
        }
        return success
    }

    override suspend fun refreshUnavailableGeoPoints() {
        getUnavailableGeoPoints().forEach { geoPoint ->
            if (geoPoint.remoteId != 0)
                geoPointRemoteDataSource.getGeoPoint(geoPoint.remoteId)
                    .onSuccess {
                        geoPointLocalDataSource.makeAvailable(it)
                    }
                    .onFailure {
                        Timber.w("${geoPoint.title} was not enabled yet")
                    }
            else
                Timber.w("${geoPoint.title} not posted yet")
        }
    }
}
