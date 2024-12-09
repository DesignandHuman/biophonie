@file:OptIn(MapboxExperimental::class)

package fr.labomg.biophonie.feature.exploregeopoints

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mapbox.android.gestures.MoveGestureDetector
import com.mapbox.geojson.Feature
import com.mapbox.geojson.Geometry
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.CameraState
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.ImageHolder
import com.mapbox.maps.MapboxExperimental
import com.mapbox.maps.dsl.cameraOptions
import com.mapbox.maps.extension.compose.DisposableMapEffect
import com.mapbox.maps.extension.compose.MapEffect
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.MapboxMapComposable
import com.mapbox.maps.extension.compose.animation.viewport.MapViewportState
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.style.MapStyle
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import com.mapbox.maps.plugin.gestures.OnMoveListener
import com.mapbox.maps.plugin.gestures.addOnMoveListener
import com.mapbox.maps.plugin.gestures.removeOnMoveListener
import com.mapbox.maps.plugin.locationcomponent.createDefault2DPuck
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.maps.plugin.viewport.data.FollowPuckViewportStateOptions
import fr.labomg.biophonie.core.model.GeoPoint
import fr.labomg.biophonie.core.ui.theme.spacing

internal const val PROPERTY_NAME: String = "name"
internal const val PROPERTY_ID: String = "id"
private const val EASE_TO_DURATION = 100L
private const val FLY_TO_DURATION = 1000L
private const val BOTTOM_PLAYER_ASPECT_RATIO = 3.0
private const val TRACKING_ZOOM_LEVEL = 13.0

@Composable
fun Map(
    cameraOptions: CameraOptions,
    modifier: Modifier = Modifier,
    sourceUrl: String = "${BuildConfig.BASE_URL}/${stringResource(R.string.geojson_url)}",
    selectedPoint: Feature? = null,
    unavailableGeoPoints: List<GeoPoint> = listOf(),
    operationState: OperationState = OperationState.Idle,
    onPause: (CameraState?) -> Unit = {},
    onMapClick: ((Point) -> Boolean) = { _ -> false },
    onPointClick: (Int) -> Unit = {},
    onTrackingStart: (Boolean) -> Unit = {},
    onTrackingDismiss: () -> Unit = {},
) {
    val mapViewportState = rememberMapViewportState()
    LaunchedEffect(cameraOptions) { mapViewportState.setCameraOptions(cameraOptions) }
    OnPauseEffect { onPause(mapViewportState.cameraState) }
    MapboxMap(
        modifier = modifier.fillMaxSize(),
        mapViewportState = mapViewportState,
        scaleBar = {
            ScaleBar(
                alignment = Alignment.BottomStart,
                textColor = MaterialTheme.colorScheme.primary,
                primaryColor = MaterialTheme.colorScheme.onPrimary,
                secondaryColor = MaterialTheme.colorScheme.secondary,
                borderWidth = 0.dp,
                height = 6.dp,
                showTextBorder = true,
                ratio = 0.35f,
                modifier =
                    Modifier.padding(
                        bottom = MaterialTheme.spacing.medium,
                        start = MaterialTheme.spacing.small
                    )
            )
        },
        logo = {
            Logo(
                alignment = Alignment.BottomEnd,
                modifier = Modifier.padding(end = MaterialTheme.spacing.medium)
            )
        },
        attribution = {
            Attribution(
                alignment = Alignment.BottomEnd,
                iconColor = MaterialTheme.colorScheme.onSecondary
            )
        },
        style = { MapStyle(style = stringResource(R.string.style_url)) },
        onMapClickListener = onMapClick
    ) {
        val screenHeight = LocalContext.current.resources.displayMetrics.heightPixels
        val bottomOffset = screenHeight / BOTTOM_PLAYER_ASPECT_RATIO
        RemoteLayer(sourceUrl, selectedPoint) { id, geometry -> onPointClick(id) }
        UnavailableLayer(unavailableGeoPoints, selectedPoint) { id, geometry -> onPointClick(id) }
        SelectedLayer(selectedPoint, onPointClick(mapViewportState, bottomOffset))
        CameraOnGeoPointEffect(selectedPoint, mapViewportState, bottomOffset)
        TrackEffect(operationState, onTrackingDismiss, onTrackingStart, mapViewportState)
    }
}

@MapboxMapComposable
@Composable
private fun CameraOnGeoPointEffect(
    selectedPoint: Feature?,
    mapViewportState: MapViewportState,
    bottomOffset: Double
) {
    MapEffect(key1 = selectedPoint) {
        val geoPoint = selectedPoint?.geometry()
        if (geoPoint != null) {
            mapViewportState.flyToGeoPoint(geoPoint, bottomOffset)
        } else {
            mapViewportState.resetMapOffset()
        }
    }
}

@MapboxMapComposable
@Composable
private fun TrackEffect(
    operationState: OperationState,
    onTrackingDismiss: () -> Unit,
    onTrackingStart: (Boolean) -> Unit,
    mapViewportState: MapViewportState
) {
    DisposableMapEffect(operationState) { mapView ->
        val onMoveListener =
            object : OnMoveListener {
                override fun onMove(detector: MoveGestureDetector) = false

                override fun onMoveBegin(detector: MoveGestureDetector) = onTrackingDismiss()

                override fun onMoveEnd(detector: MoveGestureDetector) = Unit
            }
        if (operationState is OperationState.WaitingToTrack) {
            mapView.location.updateSettings {
                locationPuck =
                    createDefault2DPuck().apply {
                        topImage = ImageHolder.from(R.drawable.ic_location)
                    }
                enabled = true
            }
            mapViewportState.transitionToFollowPuckState(
                followPuckViewportStateOptions =
                    FollowPuckViewportStateOptions.Builder()
                        .pitch(null)
                        .zoom(TRACKING_ZOOM_LEVEL)
                        .build(),
                completionListener = onTrackingStart
            )
            mapView.mapboxMap.addOnMoveListener(onMoveListener)
        }

        onDispose { mapView.mapboxMap.removeOnMoveListener(onMoveListener) }
    }
}

private fun onPointClick(
    mapViewportState: MapViewportState,
    bottomOffset: Double
): (Int, Geometry) -> Unit = { _, geometry ->
    mapViewportState.flyToGeoPoint(geometry as Point, bottomOffset)
}

private fun MapViewportState.resetMapOffset() {
    easeTo(
        cameraOptions { padding(EdgeInsets(0.0, 0.0, 0.0, 0.0)) },
        MapAnimationOptions.mapAnimationOptions { duration(EASE_TO_DURATION) }
    )
}

private fun MapViewportState.flyToGeoPoint(geoPoint: Geometry, bottomOffset: Double) {
    flyTo(
        cameraOptions =
            cameraOptions {
                center(geoPoint as Point)
                pitch(.0)
                padding(EdgeInsets(0.0, 0.0, bottomOffset, 0.0))
            },
        MapAnimationOptions.mapAnimationOptions { duration(FLY_TO_DURATION) }
    )
}
