package com.example.biophonie.data.source

import com.example.biophonie.data.Coordinates
import com.example.biophonie.data.GeoPoint
import com.example.biophonie.network.Message

interface GeoPointDataSource {

    suspend fun getGeoPoint(id: Int): Result<GeoPoint>

    suspend fun getClosestGeoPointId(coord: Coordinates, not: Array<Int>): Result<Int>

    suspend fun getNewGeoPoints(): List<GeoPoint>

    suspend fun getUnavailableGeoPoints(): List<GeoPoint>

    suspend fun addGeoPoint(geoPoint: GeoPoint, fromUser: Boolean = false): Result<GeoPoint>

    suspend fun refreshGeoPoint(geoPoint: GeoPoint)

    suspend fun pingRestricted(): Result<Message>

    suspend fun makeAvailable(geoPoint: GeoPoint)

    suspend fun cancelCurrentJob()
}