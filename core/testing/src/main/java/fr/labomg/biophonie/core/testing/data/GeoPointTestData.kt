package fr.labomg.biophonie.core.testing.data

import fr.labomg.biophonie.core.model.Coordinates
import fr.labomg.biophonie.core.model.GeoPoint
import java.time.Instant

val geoPointTestData =
    listOf(
        GeoPoint(
            id = 0,
            remoteId = 0,
            coordinates = Coordinates(0.0, 0.0),
            title = "New Local Point",
            date = Instant.parse("2007-12-03T10:15:30.00Z"),
            amplitudes = (1..10).map { it.toFloat() }.toList(),
            picture = "clearing.webp",
            sound = "c02c264d-76ba-41f5-b9b9-f6d3731f31ff.aac"
        ),
        GeoPoint(
            id = 1,
            remoteId = 1,
            coordinates = Coordinates(1.0, 1.0),
            title = "Remote Point",
            date = Instant.parse("2007-12-03T10:15:30.00Z"),
            amplitudes = (1..10).map { it.toFloat() }.toList(),
            picture = "df82696a-6851-4051-a432-5b7bf0d7f2da.webp",
            sound = "c02c264d-76ba-41f5-b9b9-f6d3731f31ff.aac"
        ),
    )
