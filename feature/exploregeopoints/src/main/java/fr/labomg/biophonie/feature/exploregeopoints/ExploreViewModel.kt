@file:OptIn(MapboxExperimental::class)

package fr.labomg.biophonie.feature.exploregeopoints

import android.Manifest
import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asFlow
import androidx.lifecycle.liveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.mapbox.geojson.Feature
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraState
import com.mapbox.maps.MapboxExperimental
import com.mapbox.maps.dsl.cameraOptions
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import fr.haran.soundwave.controller.DefaultPlayerController
import fr.haran.soundwave.ui.PlayerView
import fr.labomg.biophonie.core.data.GeoPointRepository
import fr.labomg.biophonie.core.data.LocationRepository
import fr.labomg.biophonie.core.data.PreferencesRepository
import fr.labomg.biophonie.core.data.UserRepository
import fr.labomg.biophonie.core.model.CameraConfiguration
import fr.labomg.biophonie.core.model.Coordinates
import fr.labomg.biophonie.core.model.GeoPoint
import fr.labomg.biophonie.core.network.InternalErrorThrowable
import fr.labomg.biophonie.core.network.NoConnectionThrowable
import fr.labomg.biophonie.core.network.NotFoundThrowable
import fr.labomg.biophonie.feature.exploregeopoints.Constants.FRANCE_LATITUDE
import fr.labomg.biophonie.feature.exploregeopoints.Constants.FRANCE_LONGITUDE
import fr.labomg.biophonie.feature.exploregeopoints.Constants.INITIAL_ZOOM_LEVEL
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

@HiltViewModel
class ExploreViewModel
@Inject
constructor(
    locationRepository: LocationRepository,
    private val geoPointRepository: GeoPointRepository,
    private val userRepository: UserRepository,
    private val preferencesRepository: PreferencesRepository,
    @ApplicationContext appContext: Context
) : AndroidViewModel(appContext as Application) {
    private var currentIndex = 0
    private var lastLocation: Coordinates? = null
    private var isFetchingClose = false
    private var playerController: DefaultPlayerController? = null

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    val geoPointId = MutableLiveData<Int?>()

    private val _event = MutableLiveData<Event>()
    val event: LiveData<Event>
        get() = _event

    private val _eventNetworkError = MutableLiveData<String?>()
    val eventNetworkError: LiveData<String?>
        get() = _eventNetworkError

    private val _leftClickable = MutableLiveData<Boolean>()
    val leftClickable: LiveData<Boolean>
        get() = _leftClickable

    private val _rightClickable = MutableLiveData<Boolean>()
    val rightClickable: LiveData<Boolean>
        get() = _rightClickable

    val geoPoint: LiveData<GeoPoint?> =
        geoPointId.switchMap { id ->
            liveData {
                if (id == null) {
                    emit(null)
                    return@liveData
                }
                _event.value = Event.LOADING
                isFetchingClose = false
                lastLocation = null
                geoPointRepository
                    .fetchGeoPoint(id)
                    .onSuccess {
                        _event.value = Event.SUCCESS
                        emit(it)
                        displayGeoPoint()
                        if (passedIds.isEmpty()) passedIds += id
                        _eventNetworkError.value?.run { _eventNetworkError.value = null }
                    }
                    .onFailure {
                        _event.value = Event.FAILURE
                        _eventNetworkError.value =
                            when (it) {
                                is NotFoundThrowable -> "Ce son n’est plus disponible"
                                is InternalErrorThrowable -> "Oups, notre serveur a des soucis"
                                is NoConnectionThrowable -> "Connexion au serveur impossible"
                                else -> "Oups, une erreur s’est produite"
                            }
                        emit(null)
                    }
            }
        }

    private var passedIds: Array<Int> = arrayOf()

    fun setPlayerController(view: PlayerView) {
        playerController =
            DefaultPlayerController(view, getApplication<Application>().cacheDir.absolutePath)
                .apply {
                    setPlayerListener(
                        error = {
                            _eventNetworkError.value =
                                when (it) {
                                    is RuntimeException -> "Le son est corrompu, désolé"
                                    is IOException -> "Impossible de trouver le son"
                                    else -> "Le cache est corrompu"
                                }
                            Timber.e("could not play audio: $it")
                        }
                    )
                }
    }

    fun retry() {
        _eventNetworkError.value = null
        _event.value = Event.LOADING
        if (isFetchingClose && lastLocation != null) displayClosestGeoPoint(lastLocation!!)
        else {
            geoPointId.value?.let { geoPointId.value = it }
        }
    }

    fun previousGeoPoint() {
        currentIndex--
        geoPointId.value = passedIds[currentIndex]
        _rightClickable.value = true
    }

    fun nextGeoPoint() {
        if (currentIndex >= passedIds.size - 1) displayClosestGeoPoint(geoPoint.value!!.coordinates)
        else {
            currentIndex++
            geoPointId.value = passedIds[currentIndex]
        }
    }

    private fun displayClosestGeoPoint(coordinates: Coordinates) {
        _event.value = Event.LOADING
        isFetchingClose = true
        lastLocation = coordinates
        pauseController()
        viewModelScope.launch {
            geoPointRepository
                .getClosestGeoPointId(coordinates, passedIds)
                .onSuccess {
                    currentIndex++
                    if (!passedIds.contains(it)) passedIds += it
                    isFetchingClose = false
                    setGeoPointQuery(it)
                }
                .onFailure {
                    _event.value = Event.FAILURE
                    when (it) {
                        is NotFoundThrowable -> {
                            _eventNetworkError.value = "Vous avez écouté tous les sons en ligne"
                            _rightClickable.value = false
                        }
                        is InternalErrorThrowable ->
                            _eventNetworkError.value = "Oups, notre serveur a des soucis"
                        is NoConnectionThrowable ->
                            _eventNetworkError.value = "Connexion au serveur impossible"
                        else -> _eventNetworkError.value = "Oups, une erreur s’est produite"
                    }
                }
        }
    }

    private fun stopPlaylist() {
        passedIds = arrayOf()
        _rightClickable.value = true
        currentIndex = 0
        pauseController()
    }

    private fun checkClickability() {
        _leftClickable.value = currentIndex - 1 >= 0
    }

    private fun displayGeoPoint() {
        addSoundToPlayer()
        checkClickability()
    }

    private fun addSoundToPlayer() {
        try {
            playerController?.addAudioFileUri(
                getApplication(),
                Uri.fromFile(File(geoPoint.value!!.sound))
            )
            return
        } catch (e: IOException) {
            Timber.e("could not add local sound to player: $e")
        }

        val url = "${BuildConfig.BASE_URL}/api/v1/assets/sound/${geoPoint.value!!.sound}"
        try {
            playerController?.addAudioUrl(url)
            return
        } catch (e: FileNotFoundException) {
            _eventNetworkError.value = "Nous n’avons pas pu trouver le son. Réessayez plus tard."
            Timber.e("could not add remote sound to player: $e")
        }
    }

    private fun setGeoPointQuery(id: Int) {
        pauseController()
        geoPointId.postValue(id)
    }

    fun pauseController() {
        playerController?.pause()
    }

    fun destroyController() {
        playerController?.destroyPlayer()
        playerController = null
    }

    fun isUserConnected(): Boolean = userRepository.isUserConnected()

    fun unselect(@Suppress("UNUSED_PARAMETER") point: Point): Boolean {
        unselect()
        return true
    }

    fun unselect() {
        geoPointId.value = null
        stopPlaylist()
    }

    enum class Event {
        LOADING,
        FAILURE,
        SUCCESS
    }

    val locations =
        locationRepository.getLocationStream().onCompletion {
            targetState = OperationState.Idle
            _uiState.update { it.copy(operationState = targetState) }
        }
    val cameraOptions =
        preferencesRepository
            .getCameraConfigurationStream()
            .map {
                cameraOptions {
                    center(
                        Point.fromLngLat(
                            it.longitude ?: FRANCE_LONGITUDE,
                            it.latitude ?: FRANCE_LATITUDE
                        )
                    )
                    zoom(it.zoomLevel ?: INITIAL_ZOOM_LEVEL)
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue =
                    cameraOptions {
                        center(Point.fromLngLat(FRANCE_LONGITUDE, FRANCE_LATITUDE))
                        zoom(INITIAL_ZOOM_LEVEL)
                    }
            )
    private var grantedPermissions = setOf<String>()
    private var targetState: OperationState = OperationState.Idle
    private val _uiState = MutableStateFlow(MapUiState())
    private val _isGPSEnabled = locationRepository.getIsGpsEnabledStream()
    private val _unavailableGeoPoints = geoPointRepository.getUnavailableGeoPointsStream()
    private val _selectedPoint =
        geoPoint.asFlow().map { it?.toMapboxFeature() }.onStart { emit(null) }

    val uiState: StateFlow<MapUiState> =
        combine(_uiState, _isGPSEnabled, _unavailableGeoPoints, _selectedPoint) {
                mapUiState,
                gpsEnabled,
                unavailableGeoPoints,
                selectedPoint ->
                mapUiState.copy(
                    isGpsEnabled = gpsEnabled,
                    shouldRequestGps =
                        (mapUiState.operationState == OperationState.WaitingToTrack ||
                            mapUiState.operationState == OperationState.Recording) && !gpsEnabled,
                    operationState =
                        if (gpsEnabled) mapUiState.operationState else OperationState.Idle,
                    unavailableGeoPoints = unavailableGeoPoints,
                    selectedPoint = selectedPoint
                )
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = MapUiState()
            )

    fun saveCameraState(cameraState: CameraState?) {
        preferencesRepository.saveCameraConfiguration(
            CameraConfiguration(
                longitude = cameraState?.center?.longitude(),
                latitude = cameraState?.center?.latitude(),
                zoomLevel = cameraState?.zoom,
            )
        )
    }

    fun dismissDialog() {
        _uiState.update { it.copy(missingPermissions = it.missingPermissions.dropLast(1)) }
    }

    fun onTrackClick() {
        targetState = OperationState.WaitingToTrack
        _uiState.update { it.copy(requestPermissions = TRACKING_PERMISSIONS) }
    }

    fun onRecordClick() {
        targetState = OperationState.Recording
        _uiState.update { it.copy(requestPermissions = RECORDING_PERMISSIONS) }
    }

    fun handlePermissionsResult(permissions: Map<String, Boolean>) {
        updateGrantedPermissions(permissions)
        val newMissingPermissions = updateMissingPermissions(permissions)
        val newFeatureState = updateFeatureState()

        _uiState.update {
            it.copy(
                operationState = newFeatureState,
                missingPermissions = newMissingPermissions,
                requestPermissions = emptyList()
            )
        }
    }

    private fun updateGrantedPermissions(permissions: Map<String, Boolean>) {
        grantedPermissions += permissions.filterValues { it }.keys
    }

    private fun updateFeatureState(): OperationState {
        var newOperationState =
            when {
                targetState == OperationState.Recording &&
                    grantedPermissions.containsAll(REQUIRED_RECORDING_PERMISSIONS) ->
                    OperationState.Recording
                targetState == OperationState.WaitingToTrack &&
                    grantedPermissions.containsAll(REQUIRED_TRACKING_PERMISSIONS) ->
                    OperationState.WaitingToTrack
                else -> _uiState.value.operationState
            }
        if (newOperationState != targetState) {
            newOperationState = OperationState.Idle
        }
        return newOperationState
    }

    private fun updateMissingPermissions(permissions: Map<String, Boolean>): List<String> {
        val missingPermissions = _uiState.value.missingPermissions.toMutableList()
        permissions.forEach { (permission, isGranted) ->
            if (permission == Manifest.permission.ACCESS_FINE_LOCATION) return missingPermissions
            if (isGranted) {
                missingPermissions.remove(permission)
            } else if (!missingPermissions.contains(permission)) {
                missingPermissions.add(permission)
            }
        }
        return missingPermissions
    }

    fun onTrackingDismiss() {
        _uiState.update { it.copy(operationState = OperationState.Idle) }
    }

    fun onPointClick(id: Int) {
        setGeoPointQuery(id)
    }

    fun onTripClick() {
        TODO("Not yet implemented")
    }

    fun onTrackingStart(isTracking: Boolean) {
        if (isTracking) _uiState.update { it.copy(operationState = OperationState.Tracking) }
    }

    fun openAboutDialog() {
        _uiState.update { it.copy(shouldShowAboutDialog = true) }
    }

    fun dismissAboutDialog() {
        _uiState.update { it.copy(shouldShowAboutDialog = false) }
    }

    fun dismissGpsDialog() {
        targetState = OperationState.Idle
        _uiState.update { it.copy(operationState = OperationState.Idle) }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    companion object {
        val TRACKING_PERMISSIONS =
            listOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        val RECORDING_PERMISSIONS =
            listOf(
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        val REQUIRED_TRACKING_PERMISSIONS = listOf(Manifest.permission.ACCESS_COARSE_LOCATION)
        val REQUIRED_RECORDING_PERMISSIONS =
            listOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.ACCESS_COARSE_LOCATION)
    }
}

internal fun GeoPoint.toMapboxFeature(): Feature {
    val mapboxFeature =
        Feature.fromGeometry(Point.fromLngLat(coordinates.longitude, this.coordinates.latitude))
    mapboxFeature.properties()?.apply {
        addProperty(PROPERTY_ID, remoteId.takeIf { it != 0 } ?: this@toMapboxFeature.id)
        addProperty(PROPERTY_NAME, this@toMapboxFeature.title)
        addProperty(PROPERTY_CACHE, this@toMapboxFeature.remoteId == 0)
    }

    return mapboxFeature
}

data class MapUiState(
    val operationState: OperationState = OperationState.Idle,
    val missingPermissions: List<String> = emptyList(),
    val requestPermissions: List<String> = emptyList(),
    val selectedPoint: Feature? = null,
    val shouldShowAboutDialog: Boolean = false,
    val isGpsEnabled: Boolean = false,
    val shouldRequestGps: Boolean = false,
    val unavailableGeoPoints: List<GeoPoint> = emptyList()
)

sealed class OperationState {
    data object Idle : OperationState()

    data object WaitingToTrack : OperationState()

    data object Tracking : OperationState()

    data object Recording : OperationState()
}
