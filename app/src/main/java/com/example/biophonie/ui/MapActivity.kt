package com.example.biophonie.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.*
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.PointF
import android.graphics.RectF
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.location.LocationManager
import android.net.Uri
import android.net.Uri.fromFile
import android.net.Uri.parse
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.biophonie.R
import com.example.biophonie.databinding.ActivityMapBinding
import com.example.biophonie.util.GPSCheck
import com.example.biophonie.util.isGPSEnabled
import com.example.biophonie.viewmodels.MapViewModel
import com.example.biophonie.viewmodels.PROPERTY_ID
import com.example.biophonie.viewmodels.PROPERTY_NAME
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.SettingsClient
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.location.LocationComponent
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions
import com.mapbox.mapboxsdk.location.LocationComponentOptions
import com.mapbox.mapboxsdk.location.OnCameraTrackingChangedListener
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.*
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import kotlinx.android.synthetic.main.activity_map.*
import java.net.URI

private const val TAG = "MapActivity"
private const val ID_ICON: String = "biophonie.icon"
private const val ID_SOURCE: String = "biophonie"
private const val ID_LAYER: String = "biophonie.sound"
private const val FRAGMENT_TAG: String = "fragment"
private const val REQUEST_CHECK_SETTINGS = 0x1

class MapActivity : FragmentActivity(), MapboxMap.OnMapClickListener, OnMapReadyCallback,
    PermissionsListener {

    private val viewModel: MapViewModel by lazy {
        ViewModelProvider(this, MapViewModel.ViewModelFactory()).get(MapViewModel::class.java)
    }
    private lateinit var permissionsManager: PermissionsManager
    private lateinit var binding: ActivityMapBinding
    private lateinit var mapboxMap: MapboxMap
    private var bottomPlayer: BottomPlayerFragment =
        BottomPlayerFragment()
    private var about: AboutFragment = AboutFragment()
    private val gpsReceiver = GPSCheck(object : GPSCheck.LocationCallBack {
        override fun turnedOn() {
            binding.locationFab.setImageResource(R.drawable.ic_baseline_location_searching)
        }

        @SuppressLint("MissingPermission")
        override fun turnedOff() {
            mapboxMap.locationComponent.isLocationComponentEnabled = false
            binding.locationFab.setImageResource(R.drawable.ic_baseline_location_disabled)
        }
    })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token))
        binding = DataBindingUtil.setContentView(this, R.layout.activity_map)
        binding.viewModel = viewModel
        setUpFabResource()
        addBottomSheetFragment()
        bindMap(savedInstanceState)
        setOnClickListeners()
    }

    private fun setUpFabResource(){
        if (isGPSEnabled(applicationContext))
            binding.locationFab.setImageResource(R.drawable.ic_baseline_location_searching)
        else
            binding.locationFab.setImageResource(R.drawable.ic_baseline_location_disabled)
    }

    private fun setOnClickListeners() {
        binding.about.setOnClickListener {
            supportFragmentManager.beginTransaction()
                .add(R.id.containerMap, about, FRAGMENT_TAG+"about")
                .addToBackStack(null)
                .commit()
        }
        binding.locationFab.setOnClickListener {
            //askLocationSettings()
            mapboxMap.locationComponent.apply {
                if (PermissionsManager.areLocationPermissionsGranted(this@MapActivity))
                    activateLocationSettings()
                else {
                    permissionsManager = PermissionsManager(this@MapActivity)
                    permissionsManager.requestLocationPermissions(this@MapActivity)
                }
            }
        }
    }

    private fun createLocationRequest(): LocationRequest? =
        LocationRequest.create()?.apply {
            interval = 10000
            fastestInterval = 3000
            priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
        }

    private fun activateLocationSettings(){
        val googleApiClient = GoogleApiClient.Builder(this).addApi(LocationServices.API).build()
        googleApiClient.connect()
        val builder = createLocationRequest()?.let {
            LocationSettingsRequest.Builder()
                .addLocationRequest(it).apply { setAlwaysShow(true) }
        }

        val client: SettingsClient = LocationServices.getSettingsClient(this)
        client.checkLocationSettings(builder?.build()).apply {
            addOnSuccessListener {
                mapboxMap.getStyle {
                    enableLocationComponent(it)
                }
            }
            addOnFailureListener {
                if (exception is ResolvableApiException){
                    try {
                        // Show the dialog by calling startResolutionForResult(),
                        // and check the result in onActivityResult().
                        (exception as ResolvableApiException).startResolutionForResult(this@MapActivity,
                            REQUEST_CHECK_SETTINGS)
                    } catch (sendEx: IntentSender.SendIntentException) {
                        // Ignore the error.
                    }
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun enableLocationComponent(loadedMapStyle: Style) {
        val locationComponent: LocationComponent = mapboxMap.locationComponent
        // Activate with options
        locationComponent.activateLocationComponent(
            LocationComponentActivationOptions.builder(this, loadedMapStyle)
                .locationComponentOptions(styleLocation())
                .build()
        )

        locationComponent.addOnCameraTrackingChangedListener(object :
            OnCameraTrackingChangedListener {
            override fun onCameraTrackingChanged(currentMode: Int) {
                when(currentMode){
                    CameraMode.TRACKING -> binding.locationFab.setImageResource(R.drawable.ic_baseline_my_location)
                    else -> binding.locationFab.setImageResource(R.drawable.ic_baseline_location_searching)
                }
            }

            override fun onCameraTrackingDismissed() {
            }})

        // Enable to make component visible
        locationComponent.isLocationComponentEnabled = true
        locationComponent.cameraMode = CameraMode.TRACKING
    }

    private fun styleLocation(): LocationComponentOptions =
        LocationComponentOptions.builder(this)
            .foregroundTintColor(
                ResourcesCompat.getColor(
                    resources,
                    R.color.design_default_color_background,
                    theme
                )
            )
            .backgroundTintColor(
                ResourcesCompat.getColor(
                    resources,
                    R.color.colorPrimaryDark,
                    theme
                )
            )
            .foregroundStaleTintColor(
                ResourcesCompat.getColor(
                    resources,
                    R.color.design_default_color_background,
                    theme
                )
            )
            .backgroundStaleTintColor(
                ResourcesCompat.getColor(
                    resources,
                    R.color.colorPrimary,
                    theme
                )
            )
            .elevation(0F)
            .bearingTintColor(ResourcesCompat.getColor(resources, R.color.colorPrimaryDark, theme))
            .trackingGesturesManagement(true)
            .build()

    /**
     * Unused but might be useful with devices not linked to GooglePlay
     *
     */
    private fun askLocationSettings(){
        AlertDialog.Builder(this).apply {
            setTitle("Paramètres GPS")
            setMessage("Le GPS n'est pas actif. Voulez-vous l'activer dans les menus ?")
            setPositiveButton("Paramètres") { _, _ ->
                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }

            setNegativeButton("Annuler") {dialog, _ -> dialog.cancel()}
            show()
        }
    }

    private fun handleClickIcon(screenPoint: PointF?): Boolean {
        var rectF: RectF? = null
        // This is a hack. It does not allow for a real hitbox of the size of the text
        screenPoint?.let {rectF = RectF(screenPoint.x - 10, screenPoint.y - 10, screenPoint.x + 50, screenPoint.y + 10) }

        val features: List<Feature> =
            rectF?.let { mapboxMap.queryRenderedFeatures(it,
                ID_LAYER
            ) } as List<Feature>
        return if (features.isEmpty()) false
        else {
            val clickedFeature: Feature? = features.first { it.geometry() is Point }
            val clickedPoint: Point? = clickedFeature?.geometry() as Point?
            clickedPoint?.let {
                bottomPlayer.clickOnGeoPoint(clickedFeature!!.getStringProperty(PROPERTY_ID),
                    clickedFeature.getStringProperty(PROPERTY_NAME),
                    LatLng(clickedPoint.latitude(), clickedPoint.longitude())
                )
                return true
            }
            return false
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode){
            REQUEST_CHECK_SETTINGS -> when(resultCode){
                Activity.RESULT_OK -> mapboxMap.getStyle { enableLocationComponent(it) }
                else -> return
            }
            else -> return
        }
    }


    override fun onRequestPermissionsResult(requestCode: Int,
                                   permissions: Array<String?>,
                                   grantResults: IntArray
    ) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onExplanationNeeded(permissionsToExplain: List<String?>?) {
        Toast.makeText(this, R.string.location_permission_explanation, Toast.LENGTH_LONG)
            .show()
    }

    override fun onPermissionResult(granted: Boolean) {
        if (granted) {
            mapboxMap.getStyle { style -> enableLocationComponent(style) }
        } else {
            Toast.makeText(this, R.string.location_permission_not_granted, Toast.LENGTH_LONG)
                .show()
        }
    }

    private fun bindMap(savedInstanceState: Bundle?) {
        binding.mapView.onCreate(savedInstanceState)
        binding.mapView.getMapAsync(this)
        binding.scaleView.metersOnly()
    }

    private fun addBottomSheetFragment() {
        supportFragmentManager.beginTransaction()
            .add(
                R.id.containerMap, bottomPlayer,
                FRAGMENT_TAG+"bottomSheet"
            )
            .commit()
    }

    // For a future research function...
    fun setFeaturesListener(){
        viewModel.features.observe(this, Observer<List<Feature>>{features ->
            mapboxMap.getStyle { it.addSource(GeoJsonSource(ID_SOURCE, FeatureCollection.fromFeatures(features))) }
        })
    }

    override fun onMapReady(mapboxMap: MapboxMap) {
        this.mapboxMap = mapboxMap
        mapboxMap.addOnCameraMoveListener{ updateScaleBar(mapboxMap) }
        mapboxMap.addOnCameraIdleListener{ updateScaleBar(mapboxMap)}
        val d = resources.getDrawable(R.drawable.ic_marker, theme)
        //val url: URI = URI.create("https://biophonie.fr/geojson")
        mapboxMap.setStyle(Style.Builder().fromUri(getString(R.string.style_url))
            .withImage(ID_ICON, d.toBitmap())
            .withSource(GeoJsonSource(ID_SOURCE, FeatureCollection.fromFeatures(viewModel.features.value as MutableList<Feature>)))
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

    private fun updateScaleBar(mapboxMap: MapboxMap) {
        val cameraPosition = mapboxMap.cameraPosition
        scaleView.update(cameraPosition.zoom.toFloat(), cameraPosition.target.latitude)
    }

    override fun onMapClick(point: LatLng): Boolean {
        return handleClickIcon(mapboxMap.projection.toScreenLocation(point))
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

    override fun onBackPressed() {
        if (bottomPlayer.bottomSheetBehavior.state != BottomSheetBehavior.STATE_HIDDEN
            && !supportFragmentManager.fragments.contains(about))
            bottomPlayer.bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        else
            super.onBackPressed()
    }

    override fun onStart() {
        super.onStart()
        binding.mapView.onStart()
    }


    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
        registerReceiver(gpsReceiver, IntentFilter(LocationManager.MODE_CHANGED_ACTION))
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
        unregisterReceiver(gpsReceiver)
    }

    override fun onStop() {
        super.onStop()
        binding.mapView.onStop()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.mapView.onSaveInstanceState(outState)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapView.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapboxMap.removeOnMapClickListener(this)
        binding.mapView.onDestroy()
    }
}