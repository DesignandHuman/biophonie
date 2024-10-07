package fr.labomg.biophonie.core.domain

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import fr.labomg.biophonie.core.data.GeoPointRepository
import fr.labomg.biophonie.core.model.GeoPoint
import fr.labomg.biophonie.core.utils.di.IoDispatcher
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.file.Path
import javax.inject.Inject
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.moveTo
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

private const val COMPRESSION_PERCENTAGE = 75

class CreateGeoPointUseCase
@Inject
constructor(
    private val geoPointRepository: GeoPointRepository,
    private val externalScope: CoroutineScope,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {

    operator fun invoke(geoPoint: GeoPoint, dataPath: String) {
        externalScope.launch {
            saveAssetsInStorage(geoPoint, dataPath)
            geoPointRepository.saveNewGeoPoint(geoPoint)
        }
    }

    // solved by lib desugaring
    @SuppressLint("NewApi")
    suspend fun saveAssetsInStorage(geoPoint: GeoPoint, dataPath: String) {
        val soundPath = Path(geoPoint.sound)
        val targetSound = Path(dataPath).resolve(soundPath.fileName)
        withContext(ioDispatcher) { soundPath.moveTo(targetSound) }
        geoPoint.sound = targetSound.absolutePathString()

        if (geoPoint.picture.contains("/")) {
            val picturePath = Path(geoPoint.picture)
            geoPoint.picture = convertToWebp(picturePath, dataPath)
        } else {
            geoPoint.picture += ".webp"
        }
    }

    // solved by lib desugaring
    @SuppressLint("NewApi")
    private suspend fun convertToWebp(imagePath: Path, dataPath: String): String {
        val compressedImage =
            File(dataPath, imagePath.fileName.toString().replaceAfter('.', "webp"))
        withContext(ioDispatcher) {
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
        withContext(ioDispatcher) {
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
}
