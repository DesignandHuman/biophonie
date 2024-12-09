package fr.labomg.biophonie.core.network

import fr.labomg.biophonie.core.model.Coordinates
import fr.labomg.biophonie.core.model.GeoPoint
import fr.labomg.biophonie.core.network.model.Message
import fr.labomg.biophonie.core.network.model.NetworkGeoPoint
import fr.labomg.biophonie.core.network.model.asNewNetworkGeoPoint
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody

@Singleton
class GeoPointRemoteDataSource @Inject constructor(private val geoPointService: GeoPointService) {

    suspend fun getGeoPoint(id: Int): Result<NetworkGeoPoint> = geoPointService.getGeoPoint(id)

    suspend fun getClosestGeoPointId(coord: Coordinates, not: Array<Int>): Result<Int> =
        geoPointService.getClosestGeoPoint(coord.longitude, coord.latitude, not).map { it.id }

    suspend fun addGeoPoint(geoPoint: GeoPoint): Result<NetworkGeoPoint> {
        val soundFile = File(geoPoint.sound)
        return if (!geoPoint.picture.contains("/")) {
            geoPointService.postNewGeoPoint(
                geoPoint.asNewNetworkGeoPoint(),
                MultipartBody.Part.createFormData(
                    "sound",
                    "sound.wav",
                    soundFile.asRequestBody("audio/x-wav".toMediaTypeOrNull())
                ),
                null
            )
        } else {
            val pictureFile = File(geoPoint.picture)
            geoPointService.postNewGeoPoint(
                geoPoint.asNewNetworkGeoPoint(),
                MultipartBody.Part.createFormData(
                    "sound",
                    "sound.wav",
                    soundFile.asRequestBody("audio/x-wav".toMediaTypeOrNull())
                ),
                MultipartBody.Part.createFormData(
                    "picture",
                    "picture.webp",
                    pictureFile.asRequestBody("image/webp".toMediaTypeOrNull())
                )
            )
        }
    }

    suspend fun pingRestricted(): Result<Message> {
        return geoPointService.pingRestricted()
    }
}
