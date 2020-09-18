package com.example.biophonie.ui.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.IntentSender
import android.graphics.PointF
import android.graphics.RectF
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.res.ResourcesCompat
import androidx.core.content.res.ResourcesCompat.getFont
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.work.*
import com.example.biophonie.R
import com.example.biophonie.databinding.ActivityMapBinding
import com.example.biophonie.ui.fragments.AboutFragment
import com.example.biophonie.ui.fragments.BottomPlayerFragment
import com.example.biophonie.util.GPSCheck
import com.example.biophonie.util.isGPSEnabled
import com.example.biophonie.viewmodels.MapViewModel
import com.example.biophonie.viewmodels.PROPERTY_CACHE
import com.example.biophonie.viewmodels.PROPERTY_ID
import com.example.biophonie.viewmodels.PROPERTY_NAME
import com.example.biophonie.work.SyncSoundsWorker
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
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.expressions.Expression.*
import com.mapbox.mapboxsdk.style.layers.Property.TEXT_ANCHOR_LEFT
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.*
import com.mapbox.mapboxsdk.style.layers.PropertyValue
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import com.mapbox.mapboxsdk.utils.BitmapUtils
import kotlinx.android.synthetic.main.activity_map.*
import kotlinx.coroutines.*

private const val TAG = "MapActivity"
private const val ID_ICON: String = "biophonie.icon"
private const val ID_ICON_CACHE: String = "biophonie.icon.grey"
private const val ID_SOURCE: String = "biophonie.remote"
private const val ID_LAYER: String = "biophonie.sound"
private const val PROPERTY_SELECTED: String = "selected"
private const val FRAGMENT_TAG: String = "fragment"
private const val REQUEST_SETTINGS_TRACKING = 0x1
private const val REQUEST_SETTINGS_SINGLE_UPDATE = 0x2
private const val REQUEST_ADD_SOUND = 0x3

class MapActivity : FragmentActivity(), MapboxMap.OnMapClickListener, OnMapReadyCallback,
    PermissionsListener {

    private val viewModel: MapViewModel by lazy {
        ViewModelProvider(this, MapViewModel.ViewModelFactory(this)).get(MapViewModel::class.java)
    }
    private lateinit var permissionsManager: PermissionsManager
    private lateinit var binding: ActivityMapBinding
    private lateinit var mapboxMap: MapboxMap
    private lateinit var geoPointsFeatures: MutableList<Feature>
    private var bottomPlayer: BottomPlayerFragment = BottomPlayerFragment()
    private var about: AboutFragment = AboutFragment()
    private val gpsReceiver = GPSCheck(object : GPSCheck.LocationCallBack {
        override fun turnedOn() {
            binding.locationFab.setImageResource(R.drawable.ic_baseline_location_searching)
        }

        @SuppressLint("MissingPermission")
        override fun turnedOff() {
            if (mapboxMap.locationComponent.isLocationComponentActivated)
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
        addBottomPlayerFragment()
        bindMap(savedInstanceState)
        setOnClickListeners()
    }

    private fun setDataObservers() {
        viewModel.newSounds.observe(this, Observer {
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
                //This is only to avoid duplicates inside geoPointsFeatures
                var firstCache = 0
                for (feature in geoPointsFeatures) {
                    if (feature.getBooleanProperty(PROPERTY_CACHE))
                        break
                    firstCache++
                }
                if (firstCache != geoPointsFeatures.size) {
                    geoPointsFeatures =
                        geoPointsFeatures.subList(0, firstCache)
                }
                geoPointsFeatures.plusAssign(symbolLayerIconFeatureList)
                mapboxMap.getStyle {
                    (it.getSource(ID_SOURCE) as GeoJsonSource).setGeoJson(
                        FeatureCollection.fromFeatures(geoPointsFeatures)
                    )
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
        mapboxMap.getStyle {
            enableLocationComponent(it)
            val locationComponent = mapboxMap.locationComponent
            locationComponent.addOnCameraTrackingChangedListener(object :
                OnCameraTrackingChangedListener {
                override fun onCameraTrackingChanged(currentMode: Int) {
                    when (currentMode) {
                        CameraMode.TRACKING -> binding.locationFab.setImageResource(R.drawable.ic_baseline_my_location)
                        else -> binding.locationFab.setImageResource(R.drawable.ic_baseline_location_searching)
                    }
                }

                override fun onCameraTrackingDismissed() {
                }
            })

            // Enable to make component visible
            locationComponent.cameraMode = CameraMode.TRACKING
        }
    }

    object SingleShotLocationProvider {
        @SuppressLint("MissingPermission")
        fun requestSingleUpdate(
            context: Context,
            callback: LocationCallback
        ) {
            val locationManager =
                context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val criteria = Criteria()
            criteria.accuracy = Criteria.ACCURACY_FINE
            locationManager.requestSingleUpdate(criteria, object : LocationListener {
                override fun onLocationChanged(location: Location) =
                    callback.onNewLocationAvailable(
                        location
                    )

                override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                override fun onProviderEnabled(provider: String?) {}
                override fun onProviderDisabled(provider: String?) {}
            }, null)
        }
    }

    interface LocationCallback {
        fun onNewLocationAvailable(location: Location)
    }

    private fun launchRecActivity(){
        Toast.makeText(this, "Acquisition de la position en cours", Toast.LENGTH_SHORT)
            .show()
        SingleShotLocationProvider.requestSingleUpdate(this,
            object : LocationCallback {
                override fun onNewLocationAvailable(location: Location) {
                    startActivityForResult(
                        Intent(this@MapActivity, RecSoundActivity::class.java).apply {
                            putExtras(Bundle().apply {
                                putDouble("latitude", location.latitude)
                                putDouble("longitude", location.longitude)
                            })
                        },
                        REQUEST_ADD_SOUND
                    )
                }
            })
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
                activateLocationSettings(REQUEST_SETTINGS_TRACKING, ::trackLocation)
            else {
                permissionsManager = PermissionsManager(this)
                permissionsManager.requestLocationPermissions(this)
            }
        }
        binding.rec.setOnClickListener {
            if (PermissionsManager.areLocationPermissionsGranted(this)){
                it.isEnabled = false
                activateLocationSettings(REQUEST_SETTINGS_SINGLE_UPDATE, ::launchRecActivity)
            } else {
                permissionsManager = PermissionsManager(this)
                permissionsManager.requestLocationPermissions(this)
            }
        }
    }

    private fun createLocationRequest(): LocationRequest? =
        LocationRequest.create()?.apply {
            interval = 10000
            fastestInterval = 3000
            priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
        }

    private fun activateLocationSettings(requestCode: Int, onSuccess: () -> Unit){
        val googleApiClient = GoogleApiClient.Builder(this).addApi(LocationServices.API).build()
        googleApiClient.connect()
        val builder = createLocationRequest()?.let {
            LocationSettingsRequest.Builder()
                .addLocationRequest(it).apply { setAlwaysShow(true) }
        }
        val client: SettingsClient = LocationServices.getSettingsClient(this)
        client.checkLocationSettings(builder?.build()).apply {
            addOnSuccessListener {
                onSuccess()
            }
            addOnFailureListener {
                if (exception is ResolvableApiException){
                    try {
                        (exception as ResolvableApiException).startResolutionForResult(
                            this@MapActivity,
                            requestCode
                        )
                    } catch (sendEx: IntentSender.SendIntentException) { }
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
        locationComponent.renderMode = RenderMode.COMPASS
        locationComponent.isLocationComponentEnabled = true
    }

    private fun styleLocation(): LocationComponentOptions =
        LocationComponentOptions.builder(this)
            .foregroundDrawable(R.drawable.location)
            .backgroundTintColor(
                ResourcesCompat.getColor(
                    resources,
                    R.color.colorPrimary,
                    theme
                )
            )
            .bearingDrawable(R.drawable.bearing)
            .bearingTintColor(
                ResourcesCompat.getColor(
                    resources,
                    R.color.colorAccent,
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
                    R.color.colorPrimaryDark,
                    theme
                )
            )
            .accuracyColor(R.color.colorPrimaryDark)
            .elevation(0F)
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

            setNegativeButton("Annuler") { dialog, _ -> dialog.cancel()}
            show()
        }
    }

    private fun handleClickIcon(screenPoint: PointF?): Boolean {
        var rectF: RectF? = null
        screenPoint?.let {rectF = RectF(
            screenPoint.x - 10,
            screenPoint.y - 10,
            screenPoint.x + 10,
            screenPoint.y + 10
        ) }
        val features: List<Feature> =
            rectF?.let { mapboxMap.queryRenderedFeatures(
                it,
                ID_LAYER
            ) } as List<Feature>
        return if (features.isNotEmpty()) displayFeature(features)
        else false
    }

    private fun displayFeature(features: List<Feature>): Boolean {
        val clickedFeature = features.firstOrNull { it.geometry() is Point }
        clickedFeature?.let { feature ->
            geoPointsFeatures.map { it.addBooleanProperty(PROPERTY_SELECTED, false) }
            geoPointsFeatures.first { it.getStringProperty(PROPERTY_ID) == clickedFeature.getStringProperty(
                PROPERTY_ID
            ) }.addBooleanProperty(PROPERTY_SELECTED, true)
            mapboxMap.style?.getSourceAs<GeoJsonSource>(ID_SOURCE)?.setGeoJson(
                FeatureCollection.fromFeatures(geoPointsFeatures)
            )
            val clickedPoint = feature.geometry() as Point
                bottomPlayer.clickOnGeoPoint(
                    clickedFeature.getStringProperty(PROPERTY_ID),
                    clickedFeature.getStringProperty(PROPERTY_NAME),
                    LatLng(clickedPoint.latitude(), clickedPoint.longitude())
                )
                return true
        }
        return false
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK){
            when (requestCode){
                REQUEST_SETTINGS_TRACKING -> trackLocation()
                REQUEST_SETTINGS_SINGLE_UPDATE -> launchRecActivity()
                REQUEST_ADD_SOUND -> viewModel.requestAddSound(data?.extras)
                else -> return
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
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
        binding.apply {
            mapView.onCreate(savedInstanceState)
            mapView.getMapAsync(this@MapActivity)
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

    // For a future research function...
    fun setFeaturesListener(){
        viewModel.features.observe(this, Observer<List<Feature>> { features ->
            mapboxMap.getStyle {
                it.addSource(
                    GeoJsonSource(
                        ID_SOURCE, FeatureCollection.fromFeatures(
                            features
                        )
                    )
                )
            }
        })
    }

    override fun onMapReady(mapboxMap: MapboxMap) {
        this.mapboxMap = mapboxMap
        geoPointsFeatures = (viewModel.features.value as MutableList<Feature>?)!!
        //TODO("Bug with the icon color, see dev as a reference")
        val properties = buildPropertyValues()
        mapboxMap.getStyle { Log.d(TAG, "onMapReady: ${it.json}") }
        mapboxMap.addOnCameraMoveListener{ updateScaleBar(mapboxMap) }
        mapboxMap.addOnCameraIdleListener{ updateScaleBar(mapboxMap)}
        //val url: URI = URI.create("https://biophonie.fr/geojson")
        mapboxMap.setStyle(
            Style.Builder().fromUri(getString(R.string.style_url))
                .withImage(
                    ID_ICON, BitmapUtils.getBitmapFromDrawable(
                        ResourcesCompat.getDrawable(
                            resources,
                            R.drawable.marker,
                            theme
                        )
                    )!!
                )
                .withImage(
                    ID_ICON_CACHE, BitmapUtils.getBitmapFromDrawable(
                        ResourcesCompat.getDrawable(
                            resources,
                            R.drawable.syncing,
                            theme
                        )
                    )!!
                )
                .withSource(
                    GeoJsonSource(
                        ID_SOURCE,
                        FeatureCollection.fromFeatures(geoPointsFeatures)
                    )
                )
                .withLayers(
                    SymbolLayer(ID_LAYER, ID_SOURCE)
                        .withProperties(*properties)
                )
        ) {
            //LoadGeoJsonDataTask(this).execute()
            mapboxMap.addOnMapClickListener(this)
            setDataObservers()
        }
    }

    private fun buildPropertyValues(): Array<PropertyValue<out Any>> {
        return arrayOf(
            iconImage(
                switchCase(
                    eq(get(PROPERTY_CACHE), true), literal(ID_ICON_CACHE),
                    eq(get(PROPERTY_CACHE), false), literal(ID_ICON),
                    literal(ID_ICON_CACHE)
                )
            ),
            iconOpacity(1f),
            iconSize(
                switchCase(
                    eq(get(PROPERTY_SELECTED), true), literal(0.7f),
                    eq(get(PROPERTY_SELECTED), false), literal(0.5f),
                    literal(0.5f)
                )
            ),
            iconAllowOverlap(false),
            iconIgnorePlacement(false),
            textFont(
                switchCase(
                    eq(get(PROPERTY_SELECTED), true), literal(arrayOf("Arial Unicode MS Bold")),
                    eq(get(PROPERTY_SELECTED), false), literal(arrayOf("Arial Unicode MS Regular")),
                    literal(arrayOf("Arial Unicode MS Regular"))
                )
            ),
            textColor(
                switchCase(
                    eq(get(PROPERTY_CACHE), true), literal(
                        String.format(
                            "#%06X",
                            0xFFFFFF and resources.getColor(R.color.colorPrimaryDark, theme)
                        )
                ),
                eq(get(PROPERTY_CACHE), false), literal(
                        String.format(
                            "#%06X",
                            0xFFFFFF and resources.getColor(R.color.colorAccent, theme)
                        )
                ),
                literal(String.format(
                    "#%06X",
                    0xFFFFFF and resources.getColor(R.color.colorAccent, theme)
                ))
            )
        ),
            textField("{name}"),
            textSize(12f),
            textOffset(arrayOf(0.8f, -0.05f)),
            textAnchor(TEXT_ANCHOR_LEFT),
            textIgnorePlacement(false),
            textAllowOverlap(false)
        )
    }

    private fun updateScaleBar(mapboxMap: MapboxMap) {
        val cameraPosition = mapboxMap.cameraPosition
        scaleView.update(cameraPosition.zoom.toFloat(), cameraPosition.target.latitude)
    }

    override fun onMapClick(point: LatLng): Boolean {
        return handleClickIcon(mapboxMap.projection.toScreenLocation(point))
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
        binding.rec.isEnabled = true
        binding.mapView.onResume()
        registerReceiver(gpsReceiver, IntentFilter(LocationManager.MODE_CHANGED_ACTION))
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