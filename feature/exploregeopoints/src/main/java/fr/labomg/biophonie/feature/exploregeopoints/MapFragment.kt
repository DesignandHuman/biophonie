package fr.labomg.biophonie.feature.exploregeopoints

import android.Manifest
import android.annotation.SuppressLint
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.net.toUri
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.vectordrawable.graphics.drawable.Animatable2Compat
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
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
import com.mapbox.maps.plugin.animation.MapAnimationOptions
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
import fr.labomg.biophonie.data.geopoint.Coordinates
import fr.labomg.biophonie.data.geopoint.GeoPoint
import fr.labomg.biophonie.feature.exploregeopoints.databinding.FragmentMapBinding
import timber.log.Timber

@AndroidEntryPoint
class MapFragment :
    Fragment(),
    OnMapClickListener,
    OnCameraChangeListener,
    OnIndicatorPositionChangedListener,
    OnMoveListener {

    private var isRecAnimating: Boolean = false
    private val viewModel: ExploreViewModel by activityViewModels()
    private var recAnimation: AnimatedVectorDrawableCompat? = null
    private var _binding: FragmentMapBinding? = null
    private val binding
        get() = _binding!!

    private lateinit var mapboxMap: MapboxMap
    private lateinit var customLocationProvider: CustomLocationProvider
    private var trackingExpected = false
    private var tracking = false
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_map, container, false)
        binding.viewModel = viewModel
        checkUserConnected()
        createBottomPlayer()
        return binding.root
    }

    private fun checkUserConnected() {
        if (!viewModel.isUserConnected())
            findNavController().navigate("android-app://fr.labomg.biophonie/firstlaunch".toUri())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setUpMapboxMap()
        setUpMapView()
        setUpFabResource()
        bindScaleView()
        setOnClickListeners()
        setDataObservers()
        setUpBackPressedCallback()
    }

    private fun createBottomPlayer() {
        childFragmentManager
            .beginTransaction()
            .add(R.id.fragment_map, BottomPlayerFragment())
            .commit()
    }

    private fun setUpMapboxMap() {
        mapboxMap = binding.mapView.getMapboxMap()
        mapboxMap.loadStyle(
            createStyle(
                dpToPx(requireContext(), LONG_DP_ICON),
                dpToPx(requireContext(), SHORT_DP_ICON)
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
        mapboxMap.addOnMapClickListener(this@MapFragment)
        mapboxMap.addOnCameraChangeListener(this@MapFragment)
    }

    private fun createStyle(longDimension: Int, shortDimension: Int): StyleContract.StyleExtension =
        style(styleUri = getString(R.string.style_url)) {
            +geoJsonSource(id = "${REMOTE}.${SOURCE}") {
                url("${BuildConfig.BASE_URL}/${getString(R.string.geojson_url)}")
                cluster(false)
            }
            +geoJsonSource("${CACHE}.${SOURCE}")
            +image(imageId = "${REMOTE}.${ICON}") {
                bitmap(
                    ContextCompat.getDrawable(requireContext(), R.drawable.ic_marker)!!.toBitmap(
                        longDimension,
                        longDimension
                    )
                )
            }
            +image(imageId = "${CACHE}.${ICON}") {
                bitmap(
                    ContextCompat.getDrawable(requireContext(), R.drawable.ic_syncing)!!.toBitmap(
                        longDimension,
                        shortDimension
                    )
                )
            }
            +symbolLayer(layerId = "${CACHE}.${LAYER}", sourceId = "${CACHE}.${SOURCE}") {
                buildProperties(UNSELECTED_CACHE_ICON_RATIO, "Regular", CACHE, false)
            }
            +symbolLayer(
                layerId = "${CACHE}.${LAYER}.${SELECTED}",
                sourceId = "${CACHE}.${SOURCE}"
            ) {
                buildProperties(SELECTED_CACHE_ICON_RATIO, "Bold", CACHE, false)
                filter(
                    boolean {
                        get(PROPERTY_ID)
                        literal(false)
                    }
                )
            }
            +symbolLayer(layerId = "${REMOTE}.${LAYER}", sourceId = "${REMOTE}.${SOURCE}") {
                buildProperties(UNSELECTED_REMOTE_ICON_RATIO, "Regular", REMOTE)
            }
            +symbolLayer(
                layerId = "${REMOTE}.${LAYER}.${SELECTED}",
                sourceId = "${REMOTE}.${SOURCE}"
            ) {
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
                        AppCompatResources.getDrawable(requireContext(), R.drawable.ic_location)
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
        val secondaryColor =
            ContextCompat.getColor(
                requireContext(),
                if (origin == REMOTE) R.color.colorAccent else R.color.colorPrimaryDark
            )
        iconImage(literal("$origin.${ICON}"))
        iconColor(literal(String.format("#%06X", BLACK_COLOR and secondaryColor)))
        iconSize(iconSize)
        iconOpacity(1.0)
        iconAllowOverlap(overlap)
        iconPadding(.0)
        textFont(listOf("IBM Plex Mono $fontFamily"))
        textColor(literal(String.format("#%06X", BLACK_COLOR and secondaryColor)))
        textField("{name}")
        textSize(LABEL_SIZE)
        textOffset(listOf(LABEL_OFFSET_LEFT, LABEL_OFFSET_RIGHT))
        textAnchor(TextAnchor.LEFT)
        textOptional(true)
    }

    private fun setDataObservers() {
        viewModel.newGeoPoints.observe(viewLifecycleOwner) { addNewGeoPointsToMap(it) }
        viewModel.geoPoint.observe(viewLifecycleOwner) { selectGeoPoint(it) }
    }

    private fun addNewGeoPointsToMap(it: List<GeoPoint>) {
        val geoPointsFeatures = buildListOfFeatures(it)
        mapboxMap.getStyle { style ->
            style
                .getSourceAs<GeoJsonSource>("${CACHE}.${SOURCE}")
                ?.featureCollection(FeatureCollection.fromFeatures(geoPointsFeatures))
        }
    }

    private fun selectGeoPoint(geoPoint: GeoPoint?) {
        if (geoPoint != null) {
            val point =
                Point.fromLngLat(geoPoint.coordinates.longitude, geoPoint.coordinates.latitude)
            val pointId = if (geoPoint.remoteId == 0) -geoPoint.id else geoPoint.id
            select(point, pointId)
            onBackPressedCallback.isEnabled = true
        } else {
            mapboxMap.flyTo(
                cameraOptions { padding(EdgeInsets(.0, .0, .0, .0)) },
            )
            unselectGeoPoint()
            onBackPressedCallback.isEnabled = false
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
        if (isGPSEnabled(requireContext()))
            binding.locationFab.setImageResource(R.drawable.ic_baseline_location_searching)
        else binding.locationFab.setImageResource(R.drawable.ic_baseline_location_disabled)
    }

    private fun trackLocation() {
        if (!checkLocationConditions()) return
        enableLocationProvider()
        binding.mapView.run {
            location.addOnIndicatorPositionChangedListener(this@MapFragment)
            gestures.addOnMoveListener(this@MapFragment)
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
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_microphone)
            )
            recAnimation?.stop()
        }
    }

    private fun createRecordAnimation() =
        AnimatedVectorDrawableCompat.create(requireContext(), R.drawable.loading_rec)?.apply {
            this.registerAnimationCallback(
                object : Animatable2Compat.AnimationCallback() {
                    override fun onAnimationEnd(drawable: Drawable?) {
                        binding.rec.post { this@apply.start() }
                    }
                }
            )
        }

    private fun setOnClickListeners() {
        binding.about.setOnClickListener { findNavController().navigate(R.id.open_about) }
        binding.locationFab.setOnClickListener {
            if (tracking) showClosestGeoPoint() else trackLocation()
        }
        binding.rec.setOnClickListener { record() }
    }

    private fun checkAudioPermission(): Boolean {
        if (
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO) !=
                PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                REQUEST_RECORD
            )
            return false
        }
        return true
    }

    private fun checkLocationConditions(): Boolean {
        return when {
            !PermissionsManager.areLocationPermissionsGranted(requireContext()) -> {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ),
                    REQUEST_LOCATION
                )
                false
            }
            !isGPSEnabled(requireContext()) -> {
                findNavController().navigate(R.id.open_settings)
                false
            }
            else -> true
        }
    }

    private fun showClosestGeoPoint() {
        trackingExpected = true
        if (!checkLocationConditions()) return
        customLocationProvider.addSingleRequestLocationConsumer {
            viewModel.displayClosestGeoPoint(Coordinates(this.latitude(), this.longitude()))
        }
    }

    private fun record() {
        trackingExpected = false
        if (!checkLocationConditions()) return
        if (!checkAudioPermission()) return
        getLocationAndLaunchRecord()
    }

    private fun enableLocationProvider() {
        initLocationProvider()
        binding.mapView.location.updateSettings { this.enabled = true }
    }

    private fun initLocationProvider() {
        if (!this::customLocationProvider.isInitialized) {
            customLocationProvider = CustomLocationProvider(requireContext())
            binding.mapView.location.setLocationProvider(customLocationProvider)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (
            grantResults.isNotEmpty() &&
                grantResults.all { it == PackageManager.PERMISSION_GRANTED }
        ) {
            dealWithRequest(requestCode)
        } else {
            Toast.makeText(requireContext(), R.string.permission_not_granted, Toast.LENGTH_LONG)
                .show()
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
        customLocationProvider.addSingleRequestLocationConsumer { launchRecording(this) }
    }

    private fun bindScaleView() {
        binding.apply {
            scaleView.metersOnly()
            scaleView.setTextFont(ResourcesCompat.getFont(requireContext(), R.font.ibm_plex_mono))
        }
    }

    private fun updateScaleBar(mapboxMap: MapboxMap) {
        val cameraState = mapboxMap.cameraState
        binding.scaleView.update(cameraState.zoom.toFloat(), cameraState.center.latitude())
    }

    override fun onStart() {
        super.onStart()
        activity?.registerReceiver(gpsReceiver, IntentFilter(LocationManager.MODE_CHANGED_ACTION))
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshUnavailableGeoPoints()
        toggleRecFabAnimated(false)
    }

    override fun onStop() {
        super.onStop()
        toggleRecFabAnimated(false)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        onCameraTrackingDismissed()
        _binding = null
    }

    override fun onDestroy() {
        super.onDestroy()
        recAnimation = null
        activity?.unregisterReceiver(gpsReceiver)
    }

    override fun onCameraChanged(eventData: CameraChangedEventData) {
        updateScaleBar(mapboxMap)
    }

    override fun onMapClick(point: Point): Boolean {
        val screenCoor = mapboxMap.pixelForCoordinate(point)
        mapboxMap.queryRenderedFeatures(withinScreenBox(screenCoor), queryLayers()) { expected ->
            val features = expected.value
            if (features.isNullOrEmpty()) {
                viewModel.unselect()
                return@queryRenderedFeatures
            }
            val clickedFeature = features.firstOrNull { it.feature.geometry() is Point }
            clickedFeature?.feature?.let { feature ->
                val id = feature.getNumberProperty(PROPERTY_ID).toInt()
                viewModel.setGeoPointQuery(id, true)
            }
        }
        return false
    }

    private fun queryLayers() =
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

    private fun select(point: Point, id: Int) {
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
                        ScreenMetricsCompat.getScreenSize(requireContext()).height /
                            BOTTOM_PLAYER_ASPECT_RATIO,
                        0.0
                    )
                )
            },
            MapAnimationOptions.mapAnimationOptions { duration(FLY_TO_DURATION) }
        )
    }

    private fun unselectGeoPoint(id: Int? = null) {
        val expression: Expression
        if (id == null) {
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
            location.removeOnIndicatorPositionChangedListener(this@MapFragment)
            gestures.removeOnMoveListener(this@MapFragment)
        }
        setUpFabResource()
        tracking = false
    }

    private fun launchRecording(location: Point) {
        val uri =
            Uri.Builder()
                .scheme("android-app")
                .authority("fr.labomg.biophonie")
                .appendPath("fragment_recording")
                .appendQueryParameter("longitude", location.longitude().toString())
                .appendQueryParameter("latitude", location.latitude().toString())
                .build()
        findNavController().navigate(uri)
    }

    private fun setUpBackPressedCallback() {
        requireActivity()
            .onBackPressedDispatcher
            .addCallback(viewLifecycleOwner, onBackPressedCallback)
    }

    private val onBackPressedCallback =
        object : OnBackPressedCallback(false) {
            override fun handleOnBackPressed() {
                viewModel.unselect()
            }
        }

    companion object {
        private const val REMOTE: String = "remote"
        private const val CACHE: String = "cache"
        private const val SOURCE: String = "source"
        private const val LAYER: String = "layer"
        private const val ICON: String = "icon"
        private const val SELECTED: String = "selected"
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
        private const val PROPERTY_NAME: String = "name"
        private const val PROPERTY_ID: String = "id"
    }
}
