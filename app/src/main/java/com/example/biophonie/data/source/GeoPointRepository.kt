package com.example.biophonie.data.source

import com.example.biophonie.data.Coordinates
import com.example.biophonie.data.GeoPoint

interface GeoPointRepository {
    suspend fun fetchGeoPoint(id: Int): Result<GeoPoint>

    suspend fun getClosestGeoPointId(coord: Coordinates, not: Array<Int>): Result<Int>

    suspend fun getUnavailableGeoPoints(): List<GeoPoint>

    suspend fun addNewGeoPoints(): Boolean

    suspend fun refreshUnavailableGeoPoints()
    suspend fun saveNewGeoPoint(geoPoint: GeoPoint): Result<GeoPoint>
}