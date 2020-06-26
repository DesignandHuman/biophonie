package com.example.biophonie.ui

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.PointF
import android.graphics.RectF
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import com.example.biophonie.R
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


private const val PROPERTY_NAME: String = "name"
private const val PROPERTY_ID: String = "id"
private const val ID_ICON: String = "biophonie.icon"
private const val ID_SOURCE: String = "biophonie"
private const val ID_LAYER: String = "biophonie.sound"
private const val FRAGMENT_TAG: String = "bottomSheet"

class MapActivity : FragmentActivity(), MapboxMap.OnMapClickListener, OnMapReadyCallback, BottomSheetFragment.SoundSheetListener {

    private var mapView: MapView? = null
    private var mapboxMap: MapboxMap? = null
    private var bottomSheet: BottomSheetFragment =
        BottomSheetFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token))
        setContentView(R.layout.activity_map)

        supportFragmentManager.beginTransaction()
            .add(
                R.id.containerMap, bottomSheet,
                FRAGMENT_TAG
            )
            .addToBackStack(FRAGMENT_TAG)
            .commit()

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
            ).apply { addStringProperty(PROPERTY_NAME, "Point 1")
                addStringProperty(PROPERTY_ID, "1")}
        )
        symbolLayerIconFeatureList.add(
            Feature.fromGeometry(
                Point.fromLngLat(-1.814496,47.534516)
            ).apply { addStringProperty(PROPERTY_NAME, "Point 2")
                addStringProperty(PROPERTY_ID, "2")}
        )
        symbolLayerIconFeatureList.add(
            Feature.fromGeometry(
                Point.fromLngLat(-1.778381,47.512238)
            ).apply { addStringProperty(PROPERTY_NAME, "Point 3")
                addStringProperty(PROPERTY_ID, "3")}
        )
        val d = resources.getDrawable(R.drawable.ic_marker, theme)
        mapboxMap.setStyle(Style.Builder().fromUri(getString(R.string.style_url))
            .withImage(ID_ICON, d.toBitmap())
            .withSource(GeoJsonSource(ID_SOURCE, FeatureCollection.fromFeatures(symbolLayerIconFeatureList)))
            .withLayer(SymbolLayer(
                ID_LAYER,
                ID_SOURCE
            )
                .withProperties(
                    iconImage(ID_ICON),
                    iconOpacity(8f),
                    iconSize(0.7f),
                    iconAllowOverlap(true),
                    iconIgnorePlacement(true),
                    textColor("#000000"),
                    textField("{name}"),
                    textSize(12f),
                    textOffset(arrayOf(2.2f,0f)),
                    textIgnorePlacement(false),
                    textAllowOverlap(false)
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
        var rectF: RectF? = null
        // This is a hack. It does not allow for a real hitbox of the size of the text
        screenPoint?.let {rectF = RectF(screenPoint.x - 10, screenPoint.y - 10, screenPoint.x + 50, screenPoint.y + 10) }

        val features: List<Feature> =
            rectF?.let { mapboxMap?.queryRenderedFeatures(it,
                ID_LAYER
            ) } as List<Feature>
        return if (features.isEmpty()) false
        else {
            val clickedFeature: Feature? = features.first { it.geometry() is Point }
            val clickedPoint: Point? = clickedFeature?.geometry() as Point?
            clickedPoint?.let {
                bottomSheet.clickOnGeoPoint(clickedFeature!!.getStringProperty(PROPERTY_ID), clickedFeature.getStringProperty(
                    PROPERTY_NAME
                ), LatLng(clickedPoint.latitude(), clickedPoint.longitude()))
                return true
            }
            return false
        }
    }

    /**
     * Convert a drawable to a bitmap
     *
     * @return bitmap
     */
    private fun Drawable.toBitmap(): Bitmap {
        if (this is BitmapDrawable) {
            return this.bitmap
        }

        val bitmap = Bitmap.createBitmap(this.intrinsicWidth, this.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        this.setBounds(0, 0, canvas.width, canvas.height)
        this.draw(canvas)

        return bitmap
    }

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

    override fun onButtonClicked(text: String) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
    }
}