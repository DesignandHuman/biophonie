package fr.labomg.biophonie.data.source

import fr.labomg.biophonie.data.Coordinates
import fr.labomg.biophonie.data.GeoPoint
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.sin
import kotlin.math.cos

class FakeGeoPointSource(var geoPoints: MutableList<GeoPoint>? = mutableListOf()): GeoPointDataSource {
    override suspend fun getGeoPoint(id: Int): Result<GeoPoint> {
        val geopoint = geoPoints?.find { it.remoteId == id }
        return if (geopoint != null) {
            Result.success(geopoint)
        } else {
            Result.failure(Exception("geopoint not found"))
        }
    }

    override suspend fun getClosestGeoPointId(coord: Coordinates, not: Array<Int>): Result<Int> {
        val closestGeopoint = geoPoints
            ?.filter { not.contains(it.remoteId) }
            ?.minByOrNull { abs(calculateDistance(it.coordinates, coord)) }
        return if (closestGeopoint != null) {
            Result.success(closestGeopoint.remoteId)
        } else {
            Result.failure(Exception("geopoint not found"))
        }
    }

    override suspend fun getNewGeoPoints(): List<GeoPoint> {
        return geoPoints ?: listOf()
    }

    override suspend fun getUnavailableGeoPoints(): List<GeoPoint> {
        return geoPoints ?: listOf()
    }

    override suspend fun addGeoPoint(geoPoint: GeoPoint, fromUser: Boolean): Result<GeoPoint> {
        geoPoints?.add(geoPoint)
        return Result.success(geoPoint)
    }

    override suspend fun refreshGeoPoint(geoPoint: GeoPoint) {
        geoPoints?.set(geoPoint.id, geoPoint)
    }

    private fun calculateDistance(coord1: Coordinates, coord2: Coordinates): Double {
        return acos(sin(coord1.latitude)*sin(coord2.latitude)+cos(coord1.latitude)*cos(coord2.latitude)*cos(coord2.longitude-coord2.longitude))*6371
    }
}