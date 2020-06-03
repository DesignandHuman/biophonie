package com.example.biophonie

import android.graphics.BitmapFactory
import android.graphics.PointF
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.*
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource


class MapActivity : AppCompatActivity(), MapboxMap.OnMapClickListener, OnMapReadyCallback {

    private val propertyName: String = "name"
    private var idIcon: String = "biophonie.icon"
    private var idSource: String = "biophonie"
    private var idLayer: String = "biophonie.sound"
    private var mapView: MapView? = null
    private var mapboxMap: MapboxMap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token))
        setContentView(R.layout.activity_map)

        mapView = findViewById(R.id.mapView)
        mapView?.onCreate(savedInstanceState)
        mapView?.getMapAsync(this)

    }

    override fun onMapReady(mapboxMap: MapboxMap) {
        this.mapboxMap = mapboxMap
        val symbolLayerIconFeatureList: MutableList<Feature> = ArrayList()
        symbolLayerIconFeatureList.add(
            Feature.fromGeometry(
                Point.fromLngLat(-1.803165, 47.516218)
            ).apply { addStringProperty(propertyName, "Point 1") }
        )
        symbolLayerIconFeatureList.add(
            Feature.fromGeometry(
                Point.fromLngLat(-1.814496,47.534516)
            ).apply { addStringProperty(propertyName, "Point 2") }
        )
        symbolLayerIconFeatureList.add(
            Feature.fromGeometry(
                Point.fromLngLat(-1.778381,47.512238)
            ).apply { addStringProperty(propertyName, "Point 3") }
        )
        mapboxMap.setStyle(Style.Builder().fromUri(getString(R.string.style_url))
            .withImage(idIcon, BitmapFactory.decodeResource(this.resources, R.drawable.mapbox_marker_icon_default))
            .withSource(GeoJsonSource(idSource, FeatureCollection.fromFeatures(symbolLayerIconFeatureList)))
            .withLayer(SymbolLayer(idLayer, idSource)
                .withProperties(
                    iconImage(idIcon),
                    iconAllowOverlap(true),
                    iconIgnorePlacement(true)
                )
            )) {
            //LoadGeoJsonDataTask(this).execute()
            mapboxMap.addOnMapClickListener(this)
        }
    }

    override fun onMapClick(point: LatLng): Boolean {
        return handleClickIcon(mapboxMap?.projection?.toScreenLocation(point));
    }

    private fun handleClickIcon(screenPoint: PointF?): Boolean {
        val features: List<Feature> =
            screenPoint?.let { mapboxMap?.queryRenderedFeatures(it, idLayer) } as List<Feature>
        return if (features.isEmpty()) false
        else {
            Toast.makeText(
                this,
                "Click on" + features[0].getStringProperty(propertyName),
                Toast.LENGTH_SHORT
            ).show()
            true
        }
    }
    /*

            mapboxMap.setStyle(Style.Builder().fromUri(getString(R.string.style_url))

            .withImage(idIcon, BitmapFactory.decodeResource(this.resources, R.drawable.mapbox_marker_icon_default))

            .withSource(GeoJsonSource(idSource, FeatureCollection.fromFeatures(symbolLayerIconFeatureList)))

            .withLayer(SymbolLayer(idLayer, idSource)
                .withProperties(
                    iconImage(idIcon),
                    iconAllowOverlap(true),
                    iconIgnorePlacement(true)
                )
            )){
            *//*val sound = SymbolLayer("count", idSource)
            sound.setProperties(
                textField("{point_count}"),
                textSize(8f),
                textOffset(arrayOf(0.0f, 0.0f)),
                textColor(Color.WHITE),
                textIgnorePlacement(true)
            )
            mapboxMap.style?.addLayerBelow(sound, idSounds)*//*
        }*/

    override fun onStart() {
        super.onStart()
        mapView?.onStart()
    }


    override fun onResume() {
        super.onResume()
        mapView?.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView?.onPause()
    }

    override fun onStop() {
        super.onStop()
        mapView?.onStop()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView?.onSaveInstanceState(outState)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapboxMap?.removeOnMapClickListener(this);
        mapView?.onDestroy()
    }
}