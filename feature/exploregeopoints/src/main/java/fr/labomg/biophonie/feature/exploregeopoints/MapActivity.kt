package fr.labomg.biophonie.feature.exploregeopoints

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.Manifest.permission.RECORD_AUDIO
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.graphics.drawable.Drawable
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.content.res.ResourcesCompat.getFont
import androidx.core.graphics.drawable.toBitmap
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentActivity
import androidx.vectordrawable.graphics.drawable.Animatable2Compat
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.android.gestures.MoveGestureDetector
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.MapboxMap
import com.mapbox.maps.RenderedQueryGeometry
import com.mapbox.maps.RenderedQueryOptions
import com.mapbox.maps.ScreenBox
import com.mapbox.maps.ScreenCoordinate
import com.mapbox.maps.dsl.cameraOptions
import com.mapbox.maps.extension.observable.eventdata.CameraChangedEventData
import com.mapbox.maps.extension.observable.eventdata.MapLoadingErrorEventData
import com.mapbox.maps.extension.style.StyleContract
import com.mapbox.maps.extension.style.expressions.dsl.generated.boolean
import com.mapbox.maps.extension.style.expressions.dsl.generated.eq
import com.mapbox.maps.extension.style.expressions.dsl.generated.literal
import com.mapbox.maps.extension.style.expressions.generated.Expression
import com.mapbox.maps.extension.style.image.image
import com.mapbox.maps.extension.style.layers.generated.SymbolLayer
import com.mapbox.maps.extension.style.layers.generated.SymbolLayerDsl
import com.mapbox.maps.extension.style.layers.generated.symbolLayer
import com.mapbox.maps.extension.style.layers.getLayerAs
import com.mapbox.maps.extension.style.layers.properties.generated.TextAnchor
import com.mapbox.maps.extension.style.sources.generated.GeoJsonSource
import com.mapbox.maps.extension.style.sources.generated.geoJsonSource
import com.mapbox.maps.extension.style.sources.getSourceAs
import com.mapbox.maps.extension.style.style
import com.mapbox.maps.plugin.LocationPuck2D
import com.mapbox.maps.plugin.animation.MapAnimationOptions.Companion.mapAnimationOptions
import com.mapbox.maps.plugin.animation.camera
import com.mapbox.maps.plugin.animation.flyTo
import com.mapbox.maps.plugin.delegates.listeners.OnCameraChangeListener
import com.mapbox.maps.plugin.delegates.listeners.OnMapLoadErrorListener
import com.mapbox.maps.plugin.gestures.OnMapClickListener
import com.mapbox.maps.plugin.gestures.OnMoveListener
import com.mapbox.maps.plugin.gestures.addOnMapClickListener
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.maps.plugin.scalebar.scalebar
import dagger.hilt.android.AndroidEntryPoint
import fr.labomg.biophonie.core.ui.ScreenMetricsCompat
import fr.labomg.biophonie.core.ui.dpToPx
import fr.labomg.biophonie.core.utils.CustomLocationProvider
import fr.labomg.biophonie.core.utils.GPSCheck
import fr.labomg.biophonie.core.utils.isGPSEnabled
import fr.labomg.biophonie.core.work.SyncSoundsWorker
import fr.labomg.biophonie.data.geopoint.Coordinates
import fr.labomg.biophonie.data.geopoint.GeoPoint
import fr.labomg.biophonie.feature.exploregeopoints.databinding.ActivityMapBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

const val PROPERTY_NAME: String = "name"
const val PROPERTY_ID: String = "id"

@AndroidEntryPoint
class MapActivity :
    FragmentActivity(),
    OnMapClickListener,
    OnCameraChangeListener,
    OnIndicatorPositionChangedListener,
    OnMoveListener {

    private var isRecAnimating: Boolean = false
    private val viewModel: MapViewModel by viewModels()
    private var recAnimation: AnimatedVectorDrawableCompat? = null
    private lateinit var binding: ActivityMapBinding
    private lateinit var mapboxMap: MapboxMap
    private lateinit var customLocationProvider: CustomLocationProvider
    private var trackingExpected = false
    private var tracking = false
    private var bottomPlayer: BottomPlayerFragment = BottomPlayerFragment()
    private var about: AboutFragment = AboutFragment()
    private val gpsReceiver =
        GPSCheck(
            object : GPSCheck.LocationCallBack {
                override fun turnedOn() {
                    binding.locationFab.setImageResource(R.drawable.ic_baseline_location_searching)
                }

                @SuppressLint("MissingPermission")
                override fun turnedOff() {
                    tracking = false
                    binding.mapView.location.enabled = false
                    binding.locationFab.setImageResource(R.drawable.ic_baseline_location_disabled)
                }
            }
        )

    private val recordLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            isRecAnimating = false
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                if (data?.extras != null) {
                    viewModel.requestAddGeoPoint(
                        data.extras,
                        applicationContext.filesDir.absolutePath
                    )
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        checkTutorial()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_map)
        binding.viewModel = viewModel
        setUpMapboxMap()
        setUpMapView()
        setUpFabResource()
        addBottomPlayerFragment()
        bindScaleView()
        setOnClickListeners()
        setDataObservers()
    }

    private fun checkTutorial() {
        if (viewModel.isUserConnected()) {
            val intent =
                Intent().apply {
                    setClassName(
                        this@MapActivity,
                        "fr.labomg.biophonie.feature.firstlaunch.TutorialActivity"
                    )
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                }
            startActivity(intent)
        }
    }

    private fun setUpMapboxMap() {
        mapboxMap = binding.mapView.getMapboxMap()
        mapboxMap.loadStyle(
            createStyle(
                dpToPx(this@MapActivity, LONG_DP_ICON),
                dpToPx(this@MapActivity, SHORT_DP_ICON)
            ),
            {
                // on style loaded
            },
            object : OnMapLoadErrorListener {
                override fun onMapLoadError(eventData: MapLoadingErrorEventData) {
                    Timber.w("onMapLoadError: could not refresh sounds, retry later")
                }
            }
        )
        mapboxMap.addOnMapClickListener(this@MapActivity)
        mapboxMap.addOnCameraChangeListener(this@MapActivity)
    }

    private fun createStyle(longDimension: Int, shortDimension: Int): StyleContract.StyleExtension =
        style(styleUri = getString(R.string.style_url)) {
            +geoJsonSource(id = "$REMOTE.$SOURCE") {
                url("${BuildConfig.BASE_URL}/${getString(R.string.geojson_url)}")
                cluster(false)
            }
            +geoJsonSource("$CACHE.$SOURCE")
            +image(imageId = "$REMOTE.$ICON") {
                bitmap(
                    ContextCompat.getDrawable(this@MapActivity, R.drawable.ic_marker)!!.toBitmap(
                        longDimension,
                        longDimension
                    )
                )
            }
            +image(imageId = "$CACHE.$ICON") {
                bitmap(
                    ContextCompat.getDrawable(this@MapActivity, R.drawable.ic_syncing)!!.toBitmap(
                        longDimension,
                        shortDimension
                    )
                )
            }
            +symbolLayer(layerId = "$CACHE.$LAYER", sourceId = "$CACHE.$SOURCE") {
                buildProperties(UNSELECTED_CACHE_ICON_RATIO, "Regular", CACHE, false)
            }
            +symbolLayer(layerId = "$CACHE.$LAYER.$SELECTED", sourceId = "$CACHE.$SOURCE") {
                buildProperties(SELECTED_CACHE_ICON_RATIO, "Bold", CACHE, false)
                filter(
                    boolean {
                        get(PROPERTY_ID)
                        literal(false)
                    }
                )
            }
            +symbolLayer(layerId = "$REMOTE.$LAYER", sourceId = "$REMOTE.$SOURCE") {
                buildProperties(UNSELECTED_REMOTE_ICON_RATIO, "Regular", REMOTE)
            }
            +symbolLayer(layerId = "$REMOTE.$LAYER.$SELECTED", sourceId = "$REMOTE.$SOURCE") {
                buildProperties(SELECTED_REMOTE_ICON_RATIO, "Bold", REMOTE)
                filter(
                    boolean {
                        get(PROPERTY_ID)
                        literal(false)
                    }
                )
            }
        }

    private fun setUpMapView() {
        binding.mapView.scalebar.enabled = false
        binding.mapView.location.updateSettings {
            locationPuck =
                LocationPuck2D(
                    topImage =
                        AppCompatResources.getDrawable(this@MapActivity, R.drawable.ic_location)
                )
        }
    }

    // format is used to define hex colors
    @SuppressWarnings("ImplicitDefaultLocale")
    private fun SymbolLayerDsl.buildProperties(
        iconSize: Double,
        fontFamily: String,
        origin: String,
        overlap: Boolean = true
    ) {
        iconImage(literal("$origin.$ICON"))
        iconColor(
            literal(
                String.format(
                    "#%06X",
                    BLACK_COLOR and
                        resources.getColor(
                            if (origin == REMOTE) R.color.colorAccent else R.color.colorPrimaryDark,
                            theme
                        )
                )
            )
        )
        iconSize(iconSize)
        iconOpacity(1.0)
        iconAllowOverlap(overlap)
        iconPadding(.0)
        textFont(listOf("IBM Plex Mono $fontFamily"))
        textColor(
            literal(
                String.format(
                    "#%06X",
                    BLACK_COLOR and
                        resources.getColor(
                            if (origin == REMOTE) R.color.colorAccent else R.color.colorPrimaryDark,
                            theme
                        )
                )
            )
        )
        textField("{name}")
        textSize(LABEL_SIZE)
        textOffset(listOf(LABEL_OFFSET_LEFT, LABEL_OFFSET_RIGHT))
        textAnchor(TextAnchor.LEFT)
        textOptional(true)
    }

    private fun setDataObservers() {
        viewModel.newGeoPoints.observe(this) {
            if (!it.isNullOrEmpty()) {
                val geoPointsFeatures = buildListOfFeatures(it)
                mapboxMap.getStyle { style ->
                    style
                        .getSourceAs<GeoJsonSource>("$CACHE.$SOURCE")
                        ?.featureCollection(FeatureCollection.fromFeatures(geoPointsFeatures))
                }
            }
        }
    }

    private fun buildListOfFeatures(geopoints: List<GeoPoint>): List<Feature> =
        geopoints.map {
            Feature.fromGeometry(
                    Point.fromLngLat(it.coordinates.longitude, it.coordinates.latitude)
                )
                .apply {
                    addStringProperty(PROPERTY_NAME, it.title)
                    // set a negative id to know it is from cache
                    addNumberProperty(PROPERTY_ID, -it.id)
                }
        }

    private fun setUpFabResource() {
        if (isGPSEnabled(this))
            binding.locationFab.setImageResource(R.drawable.ic_baseline_location_searching)
        else binding.locationFab.setImageResource(R.drawable.ic_baseline_location_disabled)
    }

    private fun trackLocation() {
        if (!checkLocationConditions()) return
        enableLocationProvider()
        binding.mapView.run {
            location.addOnIndicatorPositionChangedListener(this@MapActivity)
            gestures.addOnMoveListener(this@MapActivity)
        }
    }

    private fun toggleRecFabAnimated(animate: Boolean) {
        if (animate) {
            isRecAnimating = true
            if (recAnimation == null) {
                recAnimation = createRecordAnimation()
            }
            binding.rec.setImageDrawable(recAnimation)
            binding.rec.isEnabled = false
            recAnimation?.start()
        } else {
            binding.rec.isEnabled = true
            binding.rec.setImageDrawable(
                ResourcesCompat.getDrawable(resources, R.drawable.ic_microphone, theme)
            )
            recAnimation?.stop()
        }
    }

    private fun createRecordAnimation() =
        AnimatedVectorDrawableCompat.create(this, R.drawable.loading_rec)?.apply {
            this.registerAnimationCallback(
                object : Animatable2Compat.AnimationCallback() {
                    override fun onAnimationEnd(drawable: Drawable?) {
                        binding.rec.post { this@apply.start() }
                    }
                }
            )
        }

    private fun launchRecActivity(location: Point) {
        recordLauncher.launch(
            Intent().apply {
                setClassName(
                    this@MapActivity,
                    "fr.labomg.biophonie.feature.addgeopoint.RecSoundActivity"
                )
                putExtras(
                    Bundle().apply {
                        putDouble("latitude", location.latitude())
                        putDouble("longitude", location.longitude())
                    }
                )
            },
        )
    }

    private fun setOnClickListeners() {
        binding.about.setOnClickListener { openAbout() }
        binding.locationFab.setOnClickListener {
            if (tracking) showClosestGeoPoint() else trackLocation()
        }
        binding.rec.setOnClickListener { record() }
    }

    private fun checkAudioPermission(): Boolean {
        if (ContextCompat.checkSelfPermission(this, RECORD_AUDIO) != PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(RECORD_AUDIO, ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION),
                REQUEST_RECORD
            )
            return false
        }
        return true
    }

    private fun checkLocationConditions(): Boolean {
        return when {
            !PermissionsManager.areLocationPermissionsGranted(this) -> {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION),
                    REQUEST_LOCATION
                )
                false
            }
            !isGPSEnabled(this) -> {
                createSettingsDialog().show()
                false
            }
            else -> true
        }
    }

    private fun showClosestGeoPoint() {
        trackingExpected = true
        if (!checkLocationConditions()) return
        customLocationProvider.addSingleRequestLocationConsumer {
            bottomPlayer.displayClosestGeoPoint(Coordinates(this.latitude(), this.longitude()))
        }
    }

    private fun record() {
        trackingExpected = false
        if (!checkLocationConditions()) return
        if (!checkAudioPermission()) return
        getLocationAndLaunchRecord()
    }

    private fun openAbout() {
        supportFragmentManager
            .beginTransaction()
            .add(R.id.containerMap, about, FRAGMENT_TAG + "about")
            .addToBackStack(null)
            .commit()
    }

    private fun createSettingsDialog(): AlertDialog.Builder =
        AlertDialog.Builder(this).apply {
            setTitle("Paramètres GPS")
            setMessage("Le GPS n'est pas actif. Voulez-vous l'activer dans les menus ?")
            setPositiveButton("Paramètres") { _, _ ->
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                recordLauncher.launch(intent)
            }
            setNegativeButton("Annuler") { dialog, _ -> dialog.cancel() }
        }

    private fun enableLocationProvider() {
        initLocationProvider()
        binding.mapView.location.updateSettings { this.enabled = true }
    }

    private fun initLocationProvider() {
        if (!this::customLocationProvider.isInitialized) {
            customLocationProvider = CustomLocationProvider(this)
            binding.mapView.location.setLocationProvider(customLocationProvider)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty() && grantResults.all { it == PERMISSION_GRANTED }) {
            dealWithRequest(requestCode)
        } else {
            Toast.makeText(this, R.string.permission_not_granted, Toast.LENGTH_LONG).show()
        }
    }

    private fun dealWithRequest(requestCode: Int) {
        when (requestCode) {
            REQUEST_RECORD -> {
                trackingExpected = false
                if (!checkAudioPermission()) return
                getLocationAndLaunchRecord()
            }
            REQUEST_LOCATION -> {
                trackingExpected = true
                @Suppress("KotlinConstantConditions")
                if (trackingExpected) trackLocation() else getLocationAndLaunchRecord()
            }
        }
    }

    private fun getLocationAndLaunchRecord() {
        initLocationProvider()
        toggleRecFabAnimated(true)
        customLocationProvider.addSingleRequestLocationConsumer { launchRecActivity(this) }
    }

    private fun bindScaleView() {
        binding.apply {
            scaleView.metersOnly()
            scaleView.setTextFont(getFont(this@MapActivity, R.font.ibm_plex_mono))
        }
    }

    private fun addBottomPlayerFragment() {
        val previousFragment =
            supportFragmentManager.findFragmentByTag(FRAGMENT_TAG + "bottomSheet")
                as? BottomPlayerFragment
        if (previousFragment == null) {
            bottomPlayer = BottomPlayerFragment()
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.containerMap, bottomPlayer, "${FRAGMENT_TAG}bottomSheet")
                .commit()
        } else bottomPlayer = previousFragment
    }

    private fun updateScaleBar(mapboxMap: MapboxMap) {
        val cameraState = mapboxMap.cameraState
        binding.scaleView.update(cameraState.zoom.toFloat(), cameraState.center.latitude())
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (
            bottomPlayer.bottomSheetBehavior.state != BottomSheetBehavior.STATE_HIDDEN &&
                !supportFragmentManager.fragments.contains(about)
        ) {
            bottomPlayer.bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        } else super.onBackPressed()
    }

    override fun onStart() {
        super.onStart()
        registerReceiver(gpsReceiver, IntentFilter(LocationManager.MODE_CHANGED_ACTION))
    }

    override fun onResume() {
        super.onResume()
        if (isRecAnimating) toggleRecFabAnimated(true)
        syncToServer()
    }

    override fun onStop() {
        super.onStop()
        toggleRecFabAnimated(false)
    }

    private val applicationScope = CoroutineScope(Dispatchers.Default)

    private fun setUpOnTimeWork() {
        val constraints =
            Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .build()
        val workRequest =
            OneTimeWorkRequestBuilder<SyncSoundsWorker>().setConstraints(constraints).build()
        WorkManager.getInstance(this)
            .enqueueUniqueWork(
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
        recAnimation = null
        unregisterReceiver(gpsReceiver)
        onCameraTrackingDismissed()
    }

    override fun onCameraChanged(eventData: CameraChangedEventData) {
        updateScaleBar(mapboxMap)
    }

    override fun onMapClick(point: Point): Boolean {
        val screenCoor = mapboxMap.pixelForCoordinate(point)
        mapboxMap.queryRenderedFeatures(withinScreenBox(screenCoor), insideLayers()) { expected ->
            val features = expected.value
            if (features.isNullOrEmpty()) {
                unselectGeoPoint()
                return@queryRenderedFeatures
            }
            val clickedFeature = features.firstOrNull { it.feature.geometry() is Point }
            clickedFeature?.feature?.let { feature ->
                val id = feature.getNumberProperty(PROPERTY_ID).toInt()
                selectPoint(feature.geometry() as Point, id)

                bottomPlayer.clickOnGeoPoint(id)
            }
        }
        return false
    }

    private fun insideLayers() =
        RenderedQueryOptions(
            listOf(
                "$REMOTE.$LAYER",
                "$REMOTE.$LAYER.$SELECTED",
                "$CACHE.$LAYER",
                "$CACHE.$LAYER.$SELECTED"
            ),
            literal(true)
        )

    private fun withinScreenBox(screenCoor: ScreenCoordinate, tolerance: Int = 10) =
        RenderedQueryGeometry(
            ScreenBox(
                ScreenCoordinate(screenCoor.x - tolerance, screenCoor.y - tolerance),
                ScreenCoordinate(screenCoor.x + tolerance, screenCoor.y + tolerance)
            )
        )

    fun selectPoint(point: Point, id: Int) {
        flyToPoint(point)
        unselectGeoPoint(id)
    }

    private fun flyToPoint(point: Point) {
        binding.mapView.camera.flyTo(
            cameraOptions {
                center(point)
                padding(
                    EdgeInsets(
                        0.0,
                        0.0,
                        ScreenMetricsCompat.getScreenSize(this@MapActivity).height /
                            BOTTOM_PLAYER_ASPECT_RATIO,
                        0.0
                    )
                )
            },
            mapAnimationOptions { duration(FLY_TO_DURATION) }
        )
    }

    private fun unselectGeoPoint(id: Int? = null) {
        val expression: Expression?
        if (id == null) {
            bottomPlayer.bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            expression = eq {
                get(PROPERTY_ID)
                literal(false)
            }
        } else {
            expression = eq {
                get(PROPERTY_ID)
                literal(id.toLong())
            }
        }
        mapboxMap.getStyle()?.apply {
            getLayerAs<SymbolLayer>("$REMOTE.$LAYER.$SELECTED")?.filter(expression)
            getLayerAs<SymbolLayer>("$CACHE.$LAYER.$SELECTED")?.filter(expression)
        }
    }

    override fun onMoveBegin(detector: MoveGestureDetector) {
        onCameraTrackingDismissed()
    }

    override fun onMove(detector: MoveGestureDetector): Boolean {
        return false
    }

    override fun onMoveEnd(detector: MoveGestureDetector) = Unit

    override fun onIndicatorPositionChanged(point: Point) {
        binding.mapView.camera.easeTo(
            cameraOptions {
                center(point)
                zoom(ZOOM_POSITION_LEVEL)
            }
        )
        binding.locationFab.setImageResource(R.drawable.ic_trip)
        tracking = true
    }

    private fun onCameraTrackingDismissed() {
        binding.mapView.run {
            location.removeOnIndicatorPositionChangedListener(this@MapActivity)
            gestures.removeOnMoveListener(this@MapActivity)
        }
        setUpFabResource()
        tracking = false
    }

    fun onBottomSheetClose() {
        unselectGeoPoint()
        mapboxMap.flyTo(
            cameraOptions { padding(EdgeInsets(.0, .0, .0, .0)) },
        )
    }

    companion object {
        private const val REMOTE: String = "remote"
        private const val CACHE: String = "cache"
        private const val SOURCE: String = "source"
        private const val LAYER: String = "layer"
        private const val ICON: String = "icon"
        private const val SELECTED: String = "selected"
        private const val FRAGMENT_TAG: String = "fragment"
        private const val REQUEST_RECORD: Int = 0x01
        private const val REQUEST_LOCATION: Int = 0x02
        private const val LONG_DP_ICON = 30
        private const val SHORT_DP_ICON = 23
        private const val UNSELECTED_CACHE_ICON_RATIO = 0.6
        private const val LABEL_OFFSET_LEFT = 0.8
        private const val LABEL_OFFSET_RIGHT = -0.05
        private const val SELECTED_CACHE_ICON_RATIO = 0.8
        private const val UNSELECTED_REMOTE_ICON_RATIO = 0.5
        private const val SELECTED_REMOTE_ICON_RATIO = 0.7
        private const val BLACK_COLOR = 0xFFFFFF
        private const val LABEL_SIZE = 12.0
        private const val FLY_TO_DURATION = 1000L
        private const val BOTTOM_PLAYER_ASPECT_RATIO = 3.0
        private const val ZOOM_POSITION_LEVEL = 10.0
    }
}
