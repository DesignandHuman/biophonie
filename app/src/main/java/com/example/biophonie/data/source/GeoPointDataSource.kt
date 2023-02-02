package com.example.biophonie.data.source

import com.example.biophonie.data.Coordinates
import com.example.biophonie.data.GeoPoint

interface GeoPointDataSource {

    suspend fun getGeoPoint(id: Int): Result<GeoPoint>

    suspend fun getClosestGeoPointId(coord: Coordinates, not: Array<Int>): Result<Int>

    suspend fun getNewGeoPoints(): List<GeoPoint>

    suspend fun getUnavailableGeoPoints(): List<GeoPoint>

    suspend fun addGeoPoint(geoPoint: GeoPoint, fromUser: Boolean = false): Result<GeoPoint>

    suspend fun refreshGeoPoint(geoPoint: GeoPoint)

}