package fr.labomg.biophonie.data.source

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import fr.labomg.biophonie.data.Coordinates
import fr.labomg.biophonie.data.GeoPoint
import fr.labomg.biophonie.di.LocalDataSource
import fr.labomg.biophonie.di.RemoteDataSource
import fr.labomg.biophonie.templates
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.moveTo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

private const val COMPRESSION_PERCENTAGE = 75

class DefaultGeoPointRepository @Inject constructor(
    @RemoteDataSource private val geoPointRemoteDataSource: GeoPointDataSource,
    @LocalDataSource private val geoPointLocalDataSource: GeoPointDataSource
) : GeoPointRepository {

    override suspend fun cancelNetworkRequest() {
        geoPointRemoteDataSource.cancelCurrentJob()
    }

    // solved by lib desugaring
    @SuppressLint("NewApi")
    override suspend fun saveAssetsInStorage(geoPoint: GeoPoint, dataPath: String) {
        val soundPath = Path(geoPoint.sound.local!!)
        val targetSound = Path(dataPath).resolve(soundPath.fileName)
        withContext(Dispatchers.IO) { soundPath.moveTo(targetSound) }
        geoPoint.sound.local = targetSound.absolutePathString()

        if (geoPoint.picture.local != null) {
            val picturePath = Path(geoPoint.picture.local!!)
            if (!templates.contains(picturePath.fileName.toString())) {
                geoPoint.picture.local = convertToWebp(picturePath, dataPath)
            }
        }
    }

    // solved by lib desugaring
    @SuppressLint("NewApi")
    private suspend fun convertToWebp(imagePath: Path, dataPath: String): String {
        val compressedImage =
            File(dataPath, imagePath.fileName.toString().replaceAfter('.', "webp"))
        withContext(Dispatchers.IO) {
            try {
                compressedImage.createNewFile()
            } catch (e: IOException) {
                Timber.e("could not create file for compressed image: $e")
            }
            compressPicture(imagePath.toAbsolutePath().toString(), compressedImage)
        }
        return compressedImage.absolutePath
    }

    private suspend fun compressPicture(input: String, output: File) {
        withContext(Dispatchers.IO) {
            val picture: Bitmap
            val out: FileOutputStream
            try {
                picture = BitmapFactory.decodeFile(input)
                out = FileOutputStream(output)
            } catch (e: IOException) {
                Timber.e("file do not exist: $e")
                return@withContext
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
                picture.compress(Bitmap.CompressFormat.WEBP_LOSSY, COMPRESSION_PERCENTAGE, out)
            else picture.compress(Bitmap.CompressFormat.WEBP, COMPRESSION_PERCENTAGE, out)
            out.close()
        }
    }

    override suspend fun fetchGeoPoint(id: Int): Result<GeoPoint> {
        cancelNetworkRequest()
        return with(geoPointLocalDataSource.getGeoPoint(id)) {
            if (isSuccess) return@with this
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
                    geoPointRemoteDataSource
                        .addGeoPoint(geoPoint)
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
                geoPointRemoteDataSource
                    .getGeoPoint(geoPoint.remoteId)
                    .onSuccess { geoPointLocalDataSource.makeAvailable(it) }
                    .onFailure { Timber.w("${geoPoint.title} was not enabled yet") }
            else Timber.w("${geoPoint.title} not posted yet")
        }
    }
}
