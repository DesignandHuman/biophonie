package com.example.biophonie.domain

import com.example.biophonie.util.LocationConverter
import com.mapbox.mapboxsdk.geometry.LatLng

class GeoPoint(var id: String?, var sounds: List<Sound>?) {

    var name: String = ""
    var coordinates: LatLng = LatLng()

    fun coordinatesToString(): String{
        return LocationConverter.latitudeAsDMS(
            coordinates.latitude,
            4
        ) + LocationConverter.longitudeAsDMS(coordinates.longitude, 4)
    }
}