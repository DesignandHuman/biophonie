package com.example.biophonie.data.source.remote

import com.example.biophonie.data.Coordinates
import com.example.biophonie.data.GeoPoint
import com.example.biophonie.data.source.GeoPointDataSource
import com.example.biophonie.network.asDomainModel
import com.example.biophonie.network.asNewNetworkGeoPoint
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class GeoPointRemoteDataSource(private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO)
    : GeoPointDataSource {

    override suspend fun getGeoPoint(id: Int): Result<GeoPoint> = withContext(ioDispatcher) {
        ClientWeb.webService.getGeoPoint(id).map { it.asDomainModel() }
    }

    override suspend fun getClosestGeoPointId(coord: Coordinates, not: Array<Int>): Result<Int> =
        withContext(ioDispatcher) {
            ClientWeb.webService.getClosestGeoPoint(coord.latitude, coord.longitude, not).map { it.id }
        }

    override suspend fun getNewGeoPoints(): List<GeoPoint> {
        //NO-OP
        return listOf()
    }

    override suspend fun getUnavailableGeoPoints(): List<GeoPoint> {
        //NO-OP
        return listOf()
    }

    override suspend fun addGeoPoint(geoPoint: GeoPoint, fromUser: Boolean): Result<GeoPoint> =
        withContext(ioDispatcher) {
            val soundFile = File(geoPoint.sound.local!!)
            if (!geoPoint.picture.local!!.endsWith(".webp")) {
                ClientWeb.webService.postNewGeoPoint(
                    geoPoint.asNewNetworkGeoPoint(),
                    MultipartBody.Part.createFormData("sound","sound.wav",soundFile.asRequestBody("audio/x-wav".toMediaTypeOrNull())),
                    null
                )
            } else {
                val pictureFile = File(geoPoint.picture.local!!)
                ClientWeb.webService.postNewGeoPoint(
                    geoPoint.asNewNetworkGeoPoint(),
                    MultipartBody.Part.createFormData("sound","sound.wav",soundFile.asRequestBody("audio/x-wav".toMediaTypeOrNull())),
                    MultipartBody.Part.createFormData("picture","picture.jpg",pictureFile.asRequestBody("image/jpeg".toMediaTypeOrNull()))
                )
            }.map { it.asDomainModel() }
        }

    override suspend fun refreshGeoPoint(geoPoint: GeoPoint) {
        //NO-OP
    }
}