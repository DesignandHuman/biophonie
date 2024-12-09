package fr.labomg.biophonie.core.testing.repository

import fr.labomg.biophonie.core.data.GeoPointRepository
import fr.labomg.biophonie.core.model.Coordinates
import fr.labomg.biophonie.core.model.GeoPoint
import fr.labomg.biophonie.core.network.NotFoundThrowable
import fr.labomg.biophonie.core.testing.data.geoPointTestData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

class TestGeoPointRepository : GeoPointRepository {

    private val unavailableGeoPoints: MutableList<GeoPoint> = mutableListOf()
    private val availableGeoPoints: MutableList<GeoPoint> = geoPointTestData.toMutableList()
    private val geoPointsStream: Flow<List<GeoPoint>> =
        flowOf(availableGeoPoints).combine(flowOf(unavailableGeoPoints)) { available, unavailable ->
            available + unavailable
        }

    override suspend fun fetchGeoPoint(id: Int): Result<GeoPoint> {
        var geoPoint = availableGeoPoints.find { it.id == id }
        if (geoPoint == null) {
            geoPoint = unavailableGeoPoints.find { it.id == id }
        }
        return if (geoPoint != null) Result.success(geoPoint)
        else Result.failure(NotFoundThrowable(""))
    }

    override suspend fun getClosestGeoPointId(coord: Coordinates, not: Array<Int>): Result<Int> =
        Result.success(availableGeoPoints[0].id)

    override fun getGeopointStream(id: Int): Flow<GeoPoint> =
        geoPointsStream.map { it.find { id == it.id }!! }

    override suspend fun getUnavailableGeoPoints(): List<GeoPoint> = unavailableGeoPoints

    override fun getUnavailableGeoPointsStream(): Flow<List<GeoPoint>> =
        flowOf(unavailableGeoPoints)

    override suspend fun addNewGeoPoints(): Boolean = true

    override suspend fun refreshUnavailableGeoPoints() {
        if (unavailableGeoPoints.isNotEmpty()) {
            availableGeoPoints.add(unavailableGeoPoints[0])
            unavailableGeoPoints.removeAt(0)
        }
    }

    override suspend fun saveNewGeoPoint(geoPoint: GeoPoint) {
        unavailableGeoPoints.add(geoPoint)
    }
}
