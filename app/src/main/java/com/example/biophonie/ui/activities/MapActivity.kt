package com.example.biophonie.ui.activities

import android.Manifest.permission.*
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.graphics.*
import android.location.*
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat.getFont
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.work.*
import com.example.biophonie.R
import com.example.biophonie.databinding.ActivityMapBinding
import com.example.biophonie.domain.Coordinates
import com.example.biophonie.ui.fragments.AboutFragment
import com.example.biophonie.ui.fragments.BottomPlayerFragment
import com.example.biophonie.util.CustomLocationProvider
import com.example.biophonie.util.GPSCheck
import com.example.biophonie.util.isGPSEnabled
import com.example.biophonie.viewmodels.MapViewModel
import com.example.biophonie.viewmodels.PROPERTY_CACHE
import com.example.biophonie.viewmodels.PROPERTY_ID
import com.example.biophonie.viewmodels.PROPERTY_NAME
import com.example.biophonie.work.SyncSoundsWorker
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.android.gestures.MoveGestureDetector
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import com.mapbox.maps.*
import com.mapbox.maps.extension.observable.eventdata.CameraChangedEventData
import com.mapbox.maps.extension.observable.eventdata.MapLoadingErrorEventData
import com.mapbox.maps.extension.style.expressions.dsl.generated.*
import com.mapbox.maps.extension.style.image.image
import com.mapbox.maps.extension.style.layers.generated.*
import com.mapbox.maps.extension.style.layers.getLayerAs
import com.mapbox.maps.extension.style.layers.properties.generated.TextAnchor
import com.mapbox.maps.extension.style.sources.generated.GeoJsonSource
import com.mapbox.maps.extension.style.sources.generated.geoJsonSource
import com.mapbox.maps.extension.style.sources.getSourceAs
import com.mapbox.maps.extension.style.style
import com.mapbox.maps.plugin.LocationPuck2D
import com.mapbox.maps.plugin.animation.camera
import com.mapbox.maps.plugin.delegates.listeners.OnCameraChangeListener
import com.mapbox.maps.plugin.delegates.listeners.OnMapLoadErrorListener
import com.mapbox.maps.plugin.gestures.*
import com.mapbox.maps.plugin.locationcomponent.*
import com.mapbox.maps.plugin.scalebar.scalebar
import kotlinx.coroutines.*
import java.util.function.Consumer

private const val TAG = "MapActivity"
private const val ID_ICON: String = "biophonie.icon"
private const val ID_ICON_CACHE: String = "biophonie.icon.grey"
private const val ID_SOURCE: String = "biophonie.source"
private const val ID_LAYER: String = "biophonie.geopoint"
private const val ID_LAYER_SELECTED: String = "biophonie.geopoint.selected"
private const val REQUEST_RECORD: Int = 0x01
private const val REQUEST_LOCATION: Int = 0x02
private const val FRAGMENT_TAG: String = "fragment"

class MapActivity : FragmentActivity(), OnMapClickListener, OnCameraChangeListener, OnIndicatorPositionChangedListener, OnMoveListener {

    private val viewModel: MapViewModel by lazy {
        ViewModelProvider(this, MapViewModel.ViewModelFactory(this)).get(MapViewModel::class.java)
    }
    private lateinit var binding: ActivityMapBinding
    private lateinit var mapboxMap: MapboxMap
    private lateinit var customLocationProvider: CustomLocationProvider

    private var trackingExpected = false
    private var tracking = false
    private var bottomPlayer: BottomPlayerFragment = BottomPlayerFragment()
    private var about: AboutFragment = AboutFragment()
    private val gpsReceiver = GPSCheck(object : GPSCheck.LocationCallBack {
        override fun turnedOn() {
            binding.locationFab.setImageResource(R.drawable.ic_baseline_location_searching)
        }

        @SuppressLint("MissingPermission")
        override fun turnedOff() {
            tracking = false
            binding.mapView.location.enabled = false
            binding.locationFab.setImageResource(R.drawable.ic_baseline_location_disabled)
        }
    })

    private val locationSettings = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            if (data?.extras != null) {
                viewModel.requestAddGeoPoint(data.extras)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_map)
        binding.viewModel = viewModel
        setUpMapbox()
        setUpFabResource()
        addBottomPlayerFragment()
        bindScaleView()
        setOnClickListeners()
    }

    private fun setUpMapbox() {
        this.mapboxMap = binding.mapView.getMapboxMap().apply {
            loadStyle(
                style(styleUri = getString(R.string.style_url)) {
                    +geoJsonSource(id = ID_SOURCE) {
                        url(getString(R.string.geojson_url))
                        cluster(false)
                    }
                    +image(imageId = ID_ICON) {
                        bitmap(BitmapFactory.decodeResource(resources,R.drawable.ic_marker))
                    }
                    +image(imageId = ID_ICON_CACHE) {
                        bitmap(BitmapFactory.decodeResource(resources,R.drawable.ic_syncing))
                    }
                    +symbolLayer(layerId = ID_LAYER, sourceId = ID_SOURCE) {
                        buildProperties(0.5, "Regular")
                    }
                    +symbolLayer(layerId = ID_LAYER_SELECTED, sourceId = ID_SOURCE) {
                        buildProperties(0.7, "Bold")
                        filter(boolean{
                            get(PROPERTY_ID)
                            literal(false)})
                    }
                },
                {
                    // on style loaded
                },
                object : OnMapLoadErrorListener {
                    override fun onMapLoadError(eventData: MapLoadingErrorEventData) {
                        Log.i(TAG, "onMapLoadError: could not refresh sounds, retry later")
                    }
                }
            )
            addOnMapClickListener(this@MapActivity)
            addOnCameraChangeListener(this@MapActivity)
            setDataObservers()
        }
        binding.mapView.scalebar.enabled = false
        binding.mapView.location.updateSettings {
            locationPuck = LocationPuck2D(
                bearingImage = AppCompatResources.getDrawable(this@MapActivity, R.drawable.bearing),
                topImage = AppCompatResources.getDrawable(this@MapActivity, R.drawable.ic_location),
            )
        }
    }

    private fun SymbolLayerDsl.buildProperties(iconSize: Double, fontFamily: String) {
        iconImage(
            switchCase {
                boolean {
                    get(PROPERTY_CACHE)
                    literal(false)
                }
                literal(ID_ICON_CACHE)
                literal(ID_ICON)
            }
        )
        iconSize(iconSize)
        textFont(listOf("IBM Plex Mono $fontFamily"))
        iconOpacity(1.0)
        iconAllowOverlap(true)
        iconPadding(.0)
        textColor(
            switchCase {
                boolean {
                    get(PROPERTY_CACHE)
                    literal(false)
                }
                literal(
                    String.format(
                        "#%06X",
                        0xFFFFFF and resources.getColor(R.color.colorPrimaryDark, theme)
                    )
                )
                literal(
                    String.format(
                        "#%06X",
                        0xFFFFFF and resources.getColor(R.color.colorAccent, theme)
                    )
                )
            }
        )
        textField("{name}")
        textSize(12.0)
        textOffset(listOf(0.8, -0.05))
        textAnchor(TextAnchor.LEFT)
        textOptional(true)
    }


    private fun setDataObservers() {
        viewModel.newGeoPoints.observe(this) {
            val symbolLayerIconFeatureList: MutableList<Feature> = ArrayList()
            if (!it.isNullOrEmpty()) {
                for (geoPoint in it) {
                    symbolLayerIconFeatureList.add(
                        Feature.fromGeometry(
                            Point.fromLngLat(geoPoint.coordinates.longitude, geoPoint.coordinates.latitude)
                        ).apply {
                            addStringProperty(PROPERTY_NAME, geoPoint.title)
                            addNumberProperty(PROPERTY_ID, geoPoint.id)
                            addBooleanProperty(PROPERTY_CACHE, true)
                        }
                    )
                }
                mapboxMap.getStyle { style ->
                    style.getSourceAs<GeoJsonSource>(ID_SOURCE)?.featureCollection(
                        FeatureCollection.fromFeatures(symbolLayerIconFeatureList)
                    )
                }
            }
        }
    }

    private fun setUpFabResource(){
        if (isGPSEnabled(this))
            binding.locationFab.setImageResource(R.drawable.ic_baseline_location_searching)
        else
            binding.locationFab.setImageResource(R.drawable.ic_baseline_location_disabled)
    }

    private fun trackLocation(){
        trackingExpected
        tracking = true
        enableLocationProvider()
        binding.mapView.run {
            location.addOnIndicatorPositionChangedListener(this@MapActivity)
            gestures.addOnMoveListener(this@MapActivity)
        }
        binding.locationFab.setImageResource(R.drawable.ic_baseline_music_note)
    }

    private val launchRecCallback = Consumer<Location> { location ->
        location?.let {
            launchRecActivity(location)
        }
    }
    private val playClosestCallback = Consumer<Location> { location ->
        location?.let {
            bottomPlayer.displayClosestGeoPoint(Coordinates(location.altitude,location.longitude))
        }
    }

    @SuppressLint("MissingPermission") //TODO there may be a better way to distinguish between build versions
    private fun retrieveLocation(callback: Consumer<Location>, listener: LocationListener){
        val locationManager =
            this.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            locationManager.getCurrentLocation(LocationManager.GPS_PROVIDER,null,this.mainExecutor,callback)
        } else {
            locationManager.requestSingleUpdate(Criteria(), listener, null)
        }
    }

    private fun launchRecActivity(location: Location) {
        binding.rec.isEnabled = false
        Toast.makeText(this, "Acquisition de la position en cours", Toast.LENGTH_SHORT)
            .show()
        locationSettings.launch(
            Intent(this@MapActivity, RecSoundActivity::class.java).apply {
                putExtras(Bundle().apply {
                    putDouble("latitude", location.latitude)
                    putDouble("longitude", location.longitude)
                })
            },
        )
    }

    private fun setOnClickListeners() {
        binding.about.setOnClickListener {
            supportFragmentManager.beginTransaction()
                .add(R.id.containerMap, about, FRAGMENT_TAG + "about")
                .addToBackStack(null)
                .commit()
        }
        binding.locationFab.setOnClickListener {
            if (PermissionsManager.areLocationPermissionsGranted(this))
                if (isGPSEnabled(this)) {
                    if (tracking) retrieveLocation(playClosestCallback) { bottomPlayer.displayClosestGeoPoint(
                        Coordinates(it.latitude,it.longitude)
                    ) }
                    else trackLocation()
                } else {
                    askLocationSettings()
                }
            else {
                trackingExpected = true
                ActivityCompat.requestPermissions(this, arrayOf(ACCESS_FINE_LOCATION,ACCESS_COARSE_LOCATION),REQUEST_LOCATION)
            }
        }
        binding.rec.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, RECORD_AUDIO)
                != PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(RECORD_AUDIO), REQUEST_RECORD)
                trackingExpected = false
                return@setOnClickListener
            }
            if (PermissionsManager.areLocationPermissionsGranted(this)) {
                if (isGPSEnabled(this)) {
                    retrieveLocation(launchRecCallback) { launchRecActivity(it) }
                } else {
                    askLocationSettings()
                }
            } else {
                trackingExpected = false
                ActivityCompat.requestPermissions(this, arrayOf(ACCESS_FINE_LOCATION,ACCESS_COARSE_LOCATION),REQUEST_LOCATION)
            }
        }
    }

    private fun askLocationSettings() {
        AlertDialog.Builder(this).apply {
            setTitle("Paramètres GPS")
            setMessage("Le GPS n'est pas actif. Voulez-vous l'activer dans les menus ?")
            setPositiveButton("Paramètres") { _, _ ->
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                locationSettings.launch(intent)
            }
            setNegativeButton("Annuler") { dialog, _ -> dialog.cancel() }
            show()
        }
    }

    private fun enableLocationProvider() {
        if (!this::customLocationProvider.isInitialized) {
            customLocationProvider = CustomLocationProvider(this)
            binding.mapView.location.setLocationProvider(customLocationProvider)
        }
        binding.mapView.location.run {
            updateSettings {
                enabled = true
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty() && grantResults[0] == PERMISSION_GRANTED) {
            when (requestCode) {
                REQUEST_RECORD -> {
                    if (PermissionsManager.areLocationPermissionsGranted(this))
                        retrieveLocation(launchRecCallback) {launchRecActivity(it)}
                    else
                        ActivityCompat.requestPermissions(this,arrayOf(ACCESS_FINE_LOCATION,ACCESS_COARSE_LOCATION),REQUEST_LOCATION)
                }
                REQUEST_LOCATION -> {
                    if (trackingExpected) trackLocation()
                    else retrieveLocation(launchRecCallback) {launchRecActivity(it)}
                }
            }
        } else {
            Toast.makeText(this, R.string.permission_not_granted, Toast.LENGTH_LONG)
                .show()
        }
    }

    private fun bindScaleView() {
        binding.apply {
            scaleView.metersOnly()
            scaleView.setTextFont(getFont(this@MapActivity, R.font.ibm_plex_mono))
        }
    }

    private fun addBottomPlayerFragment() {
        val previousFragment = supportFragmentManager.findFragmentByTag(FRAGMENT_TAG + "bottomSheet") as? BottomPlayerFragment
        if (previousFragment == null){
            bottomPlayer = BottomPlayerFragment()
            supportFragmentManager.beginTransaction()
                .replace(
                    R.id.containerMap, bottomPlayer,
                    FRAGMENT_TAG + "bottomSheet"
                )
                .commit()
        } else
            bottomPlayer = previousFragment
    }

    private fun updateScaleBar(mapboxMap: MapboxMap) {
        val cameraState = mapboxMap.cameraState
        binding.scaleView.update(cameraState.zoom.toFloat(), cameraState.center.latitude())
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
        registerReceiver(gpsReceiver, IntentFilter(LocationManager.MODE_CHANGED_ACTION))
    }

    override fun onResume() {
        super.onResume()
        binding.rec.isEnabled = true
        syncToServer()
    }

    private val applicationScope = CoroutineScope(Dispatchers.Default)

    private fun setUpOnTimeWork(){
        Log.d(TAG, "setUpOnTimeWork: syncing to server")
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .build()
        val workRequest = OneTimeWorkRequestBuilder<SyncSoundsWorker>()
            .setConstraints(constraints)
            .build()
        WorkManager.getInstance(this).enqueueUniqueWork(
            SyncSoundsWorker.WORK_NAME,
            ExistingWorkPolicy.APPEND_OR_REPLACE,
            workRequest
        )
    }

    private fun syncToServer() {
        applicationScope.launch { setUpOnTimeWork() }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(gpsReceiver)
        onCameraTrackingDismissed()
    }

    override fun onCameraChanged(eventData: CameraChangedEventData) {
        updateScaleBar(mapboxMap)
    }

    override fun onMapClick(point: Point): Boolean {
        val screenCoor = mapboxMap.pixelForCoordinate(point)

        mapboxMap.queryRenderedFeatures(
            RenderedQueryGeometry(
                ScreenBox(
                    ScreenCoordinate(screenCoor.x-10,screenCoor.y-10),
                    ScreenCoordinate(screenCoor.x+10,screenCoor.y+10)
                )
            ),
            RenderedQueryOptions(listOf(ID_LAYER, ID_LAYER_SELECTED), literal(true)))
        { expected ->
            val features = expected.value
            val clickedFeature = features?.firstOrNull { it.feature.geometry() is Point }
            clickedFeature?.feature?.let { feature ->
                val selectedLayer = mapboxMap.getStyle()?.getLayerAs<SymbolLayer>(
                    ID_LAYER_SELECTED)
                val id = feature.getNumberProperty(PROPERTY_ID).toLong()
                selectedLayer?.filter(eq{
                    get(PROPERTY_ID)
                    literal(id)}
                )

                with(mapboxMap.cameraState.toCameraOptions().toBuilder()) {
                    binding.mapView.camera.flyTo(
                        center(feature.geometry() as Point).build(),
                    )
                }

                bottomPlayer.clickOnGeoPoint(id.toInt())
            }
        }
        return false
    }

    override fun onMoveBegin(detector: MoveGestureDetector) {
        onCameraTrackingDismissed()
    }

    override fun onMove(detector: MoveGestureDetector): Boolean {
        return false
    }

    override fun onMoveEnd(detector: MoveGestureDetector) {}

    override fun onIndicatorPositionChanged(point: Point) {
        mapboxMap.setCamera(CameraOptions.Builder().center(point).build())
        binding.mapView.gestures.focalPoint = mapboxMap.pixelForCoordinate(point)
    }

    private fun onCameraTrackingDismissed() {
        binding.mapView.run {
            location.removeOnIndicatorPositionChangedListener(this@MapActivity)
            gestures.removeOnMoveListener(this@MapActivity)
        }
        setUpFabResource()
        tracking = false
    }

}