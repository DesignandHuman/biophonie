package fr.labomg.biophonie.core.testing.data

import com.mapbox.common.location.Location

val parisLocation =
    Location.Builder()
        .latitude(Locations.PARIS_LATITUDE)
        .longitude(Locations.PARIS_LONGITUDE)
        .build()

val santiagoLocation =
    Location.Builder()
        .latitude(Locations.SANTIAGO_LATITUDE)
        .longitude(Locations.SANTIAGO_LONGITUDE)
        .build()

private object Locations {
    const val SANTIAGO_LONGITUDE = -33.442033
    const val SANTIAGO_LATITUDE = -70.6550262
    const val PARIS_LONGITUDE = 48.8596826
    const val PARIS_LATITUDE = 2.3975114
}
