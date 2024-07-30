package fr.labomg.biophonie.data.geopoint.source.remote

import fr.labomg.biophonie.core.network.di.IoDispatcher
import fr.labomg.biophonie.data.geopoint.Coordinates
import fr.labomg.biophonie.data.geopoint.GeoPoint
import fr.labomg.biophonie.data.geopoint.source.GeoPointDataSource
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody

@Singleton
class GeoPointRemoteDataSource
@Inject
constructor(
    private val geoPointService: GeoPointService,
    @IoDispatcher private val dispatcher: CoroutineDispatcher
) : GeoPointDataSource {

    private var currentJob: Job? = null

    override suspend fun cancelCurrentJob() {
        currentJob?.cancel()
    }

    override suspend fun getGeoPoint(id: Int): Result<GeoPoint> =
        withContext(dispatcher) { geoPointService.getGeoPoint(id).map { it.asDomainModel() } }

    override suspend fun getClosestGeoPointId(coord: Coordinates, not: Array<Int>): Result<Int> =
        with(
            CoroutineScope(dispatcher).async {
                return@async geoPointService
                    .getClosestGeoPoint(coord.latitude, coord.longitude, not)
                    .map { it.id }
            }
        ) {
            currentJob = this
            return@with this.await()
        }

    override suspend fun getNewGeoPoints(): List<GeoPoint> {
        // NO-OP
        return listOf()
    }

    override suspend fun getUnavailableGeoPoints(): List<GeoPoint> {
        // NO-OP
        return listOf()
    }

    override suspend fun addGeoPoint(geoPoint: GeoPoint, fromUser: Boolean): Result<GeoPoint> =
        withContext(dispatcher) {
            val soundFile = File(geoPoint.sound.local!!)
            if (!geoPoint.picture.local!!.endsWith(".webp")) {
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
                    val pictureFile = File(geoPoint.picture.local!!)
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
                .map { it.asDomainModel() }
        }

    override suspend fun refreshGeoPoint(geoPoint: GeoPoint) {
        // NO-OP
    }

    override suspend fun pingRestricted(): Result<Message> {
        return geoPointService.pingRestricted()
    }

    override suspend fun makeAvailable(geoPoint: GeoPoint) {
        // NO-OP
    }
}
