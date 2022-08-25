package com.example.biophonie.ui.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
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
import com.mapbox.android.core.location.*
import com.mapbox.android.core.permissions.PermissionsListener
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
import com.mapbox.maps.plugin.gestures.OnMapClickListener
import com.mapbox.maps.plugin.gestures.OnMoveListener
import com.mapbox.maps.plugin.gestures.addOnMapClickListener
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.locationcomponent.*
import com.mapbox.maps.plugin.scalebar.scalebar
import kotlinx.coroutines.*
import java.util.function.Consumer


private const val TAG = "MapActivity"
private const val ID_ICON: String = "biophonie.icon"
private const val ID_ICON_CACHE: String = "biophonie.icon.grey"
private const val ID_SOURCE_LOCAL: String = "biophonie.local"
private const val ID_SOURCE_REMOTE: String = "biophonie.remote"
private const val ID_LAYER_LOCAL: String = "biophonie.sound.local"
private const val ID_LAYER_REMOTE: String = "biophonie.sound.remote"
private const val ID_LAYER_REMOTE_SELECTED: String = "biophonie.sound.remote.selected"
private const val REQUEST_RECORD_AUDIO: Int = 1234
private const val FRAGMENT_TAG: String = "fragment"

class MapActivity : FragmentActivity(), OnMapClickListener, PermissionsListener, OnCameraChangeListener, OnIndicatorPositionChangedListener, OnMoveListener {

    private val viewModel: MapViewModel by lazy {
        ViewModelProvider(this, MapViewModel.ViewModelFactory(this)).get(MapViewModel::class.java)
    }
    private lateinit var permissionsManager: PermissionsManager
    private lateinit var binding: ActivityMapBinding
    private lateinit var mapboxMap: MapboxMap
    private lateinit var customLocationProvider: CustomLocationProvider

    private var bottomPlayer: BottomPlayerFragment = BottomPlayerFragment()
    private var about: AboutFragment = AboutFragment()
    private val gpsReceiver = GPSCheck(object : GPSCheck.LocationCallBack {
        override fun turnedOn() {
            binding.locationFab.setImageResource(R.drawable.ic_baseline_location_searching)
        }

        @SuppressLint("MissingPermission")
        override fun turnedOff() {
            binding.mapView.location.enabled = false
            binding.locationFab.setImageResource(R.drawable.ic_baseline_location_disabled)
        }
    })

    private val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            if (data?.extras != null) {
                viewModel.requestAddSound(data.extras)
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
                    +geoJsonSource(id = ID_SOURCE_REMOTE) {
                        url(getString(R.string.geojson_url))
                        cluster(false)
                    }
                    +image(imageId = ID_ICON) {
                        bitmap(BitmapFactory.decodeResource(resources,R.drawable.ic_marker))
                    }
                    +image(imageId = ID_ICON_CACHE) {
                        bitmap(BitmapFactory.decodeResource(resources,R.drawable.ic_syncing))
                    }
                    +symbolLayer(layerId = ID_LAYER_REMOTE, sourceId = ID_SOURCE_REMOTE) {
                        buildProperties(0.5, "Regular")
                    }
                    +symbolLayer(layerId = ID_LAYER_REMOTE_SELECTED, sourceId = ID_SOURCE_REMOTE) {
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
        permissionsManager = PermissionsManager(this)
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
        iconAllowOverlap(false)
        iconIgnorePlacement(false)
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
        textIgnorePlacement(false)
        textAllowOverlap(false)
    }


    private fun setDataObservers() {
        viewModel.newSounds.observe(this, {
            val symbolLayerIconFeatureList: MutableList<Feature> = ArrayList()
            if (!viewModel.newSounds.value.isNullOrEmpty()) {
                for (i in viewModel.newSounds.value!!) {
                    symbolLayerIconFeatureList.add(
                        Feature.fromGeometry(
                            Point.fromLngLat(i.longitude, i.latitude)
                        ).apply {
                            addStringProperty(PROPERTY_NAME, i.title)
                            addStringProperty(PROPERTY_ID, i.id)
                            addBooleanProperty(PROPERTY_CACHE, true)
                        }
                    )
                }
                mapboxMap.getStyle {
                    it.getSourceAs<GeoJsonSource>(ID_SOURCE_LOCAL)?.featureCollection(FeatureCollection.fromFeatures(symbolLayerIconFeatureList))
                }
            }
        })
    }

    private fun setUpFabResource(){
        if (isGPSEnabled(this))
            binding.locationFab.setImageResource(R.drawable.ic_baseline_location_searching)
        else
            binding.locationFab.setImageResource(R.drawable.ic_baseline_location_disabled)
    }

    private fun trackLocation(){
        enableLocationProvider()
        binding.mapView.run {
            location.addOnIndicatorPositionChangedListener(this@MapActivity)
            gestures.addOnMoveListener(this@MapActivity)
        }
        binding.locationFab.setImageResource(R.drawable.ic_baseline_my_location)
    }

    private val locationCallback = Consumer<Location> { location ->
        location?.let {
            launchRecActivity(location)
        }
    }

    @SuppressLint("MissingPermission")
    private fun retrieveLocation(){
        binding.rec.isEnabled = false
        Toast.makeText(this, "Acquisition de la position en cours", Toast.LENGTH_SHORT)
            .show()
        val locationManager =
            this.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            locationManager.getCurrentLocation(LocationManager.GPS_PROVIDER,null,this.mainExecutor,locationCallback)
        } else {
            locationManager.requestSingleUpdate(Criteria(), {
                launchRecActivity(it)
            }, null)
        }
    }

    private fun launchRecActivity(location: Location) {
        resultLauncher.launch(
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
                    trackLocation()
                } else {
                    askLocationSettings()
                }
            else {
                permissionsManager = PermissionsManager(this)
                permissionsManager.requestLocationPermissions(this)
            }
        }
        binding.rec.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.RECORD_AUDIO),
                    REQUEST_RECORD_AUDIO)
                return@setOnClickListener
            }
            if (PermissionsManager.areLocationPermissionsGranted(this)) {
                if (isGPSEnabled(this)) {
                    retrieveLocation()
                } else {
                    askLocationSettings()
                }
            } else {
                permissionsManager.requestLocationPermissions(this)
            }
        }
    }

    private fun askLocationSettings() {
        AlertDialog.Builder(this).apply {
            setTitle("Paramètres GPS")
            setMessage("Le GPS n'est pas actif. Voulez-vous l'activer dans les menus ?")
            setPositiveButton("Paramètres") { _, _ ->
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                resultLauncher.launch(intent)
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
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (requestCode == REQUEST_RECORD_AUDIO) {
                retrieveLocation()
            }
        }
    }

    override fun onExplanationNeeded(permissionsToExplain: List<String?>?) {
        Toast.makeText(this, R.string.location_permission_explanation, Toast.LENGTH_LONG)
            .show()
    }

    override fun onPermissionResult(granted: Boolean) {
        if (granted) {
            //TODO maybe ?
        } else {
            Toast.makeText(this, R.string.location_permission_not_granted, Toast.LENGTH_LONG)
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
        mapboxMap.removeOnCameraChangeListener(this)
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
            RenderedQueryOptions(listOf(ID_LAYER_REMOTE, ID_LAYER_LOCAL), literal(true)))
        { expected ->
            val features = expected.value
            val clickedFeature = features?.firstOrNull { it.feature.geometry() is Point }
            clickedFeature?.feature?.let { feature ->
                val selectedLayer = mapboxMap.getStyle()?.getLayerAs<SymbolLayer>(
                    ID_LAYER_REMOTE_SELECTED)
                val id = feature.getNumberProperty(PROPERTY_ID).toDouble()
                selectedLayer?.filter(eq{
                    get(PROPERTY_ID)
                    literal(id)}
                )
                val cameraBuilder = mapboxMap.cameraState.toCameraOptions().toBuilder()
                binding.mapView.camera.flyTo(
                    cameraBuilder.center(clickedFeature.feature.geometry() as Point).build(),
                )
            }

            /*geoPointsFeatures.map { it.addBooleanProperty(PROPERTY_SELECTED, false) }
            geoPointsFeatures.first { it.getStringProperty(PROPERTY_ID) == clickedFeature.getStringProperty(
                PROPERTY_ID
            ) }.addBooleanProperty(PROPERTY_SELECTED, true)
            mapboxMap.style?.getSourceAs<GeoJsonSource>(ID_SOURCE_LOCAL)?.setGeoJson(
                FeatureCollection.fromFeatures(geoPointsFeatures)
            )
            val clickedPoint = feature.geometry() as Point
            bottomPlayer.clickOnGeoPoint(
                feature.getStringProperty(PROPERTY_ID),
                feature.getStringProperty(PROPERTY_NAME),
                LatLng(clickedPoint.latitude(), clickedPoint.longitude())
            )
            return true*/
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
    }
}