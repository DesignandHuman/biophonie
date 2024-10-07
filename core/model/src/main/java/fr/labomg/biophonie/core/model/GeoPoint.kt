package fr.labomg.biophonie.core.model

import java.time.Instant

data class GeoPoint(
    var id: Int,
    var remoteId: Int = 0,
    var coordinates: Coordinates,
    var title: String,
    var date: Instant,
    var amplitudes: List<Float>,
    var picture: String,
    var sound: String
)

data class NewGeoPoint(
    val title: String,
    val date: String,
    val amplitudes: List<Int>,
    var coordinates: Coordinates?,
    val soundPath: String,
    val landscapePath: String,
    val templatePath: String
)
