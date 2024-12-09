package fr.labomg.biophonie.feature.exploregeopoints

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.mapbox.common.location.Location
import com.mapbox.maps.MapboxExperimental
import fr.labomg.biophonie.core.ui.compose.CallToActionDialog
import fr.labomg.biophonie.core.ui.compose.GpsActivationProvider
import fr.labomg.biophonie.core.ui.compose.SurfacePreview
import fr.labomg.biophonie.core.ui.theme.spacing
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@OptIn(MapboxExperimental::class)
@Composable
fun MapScreen(
    modifier: Modifier = Modifier,
    viewModel: ExploreViewModel = hiltViewModel(),
    onRecord: (Location) -> Unit = { _ -> }
) {
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(uiState.operationState, onRecord) {
        if (uiState.operationState is OperationState.Recording) {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { onRecord(viewModel.locations.first()) }
            }
        }
    }
    PermissionRequester(
        requestPermissions = uiState.requestPermissions,
        missingPermissions = uiState.missingPermissions,
        handlePermissionsResult = viewModel::handlePermissionsResult,
        dismissDialog = viewModel::dismissDialog
    )
    GpsRequester(
        onDismiss = viewModel::dismissGpsDialog,
        shouldRequestGps = uiState.shouldRequestGps,
    )
    Box(modifier = modifier) {
        val cameraOptions by viewModel.cameraOptions.collectAsStateWithLifecycle()
        Map(
            cameraOptions = cameraOptions,
            selectedPoint = uiState.selectedPoint,
            unavailableGeoPoints = uiState.unavailableGeoPoints,
            operationState = uiState.operationState,
            onPause = viewModel::saveCameraState,
            onMapClick = viewModel::unselect,
            onPointClick = viewModel::onPointClick,
            onTrackingStart = viewModel::onTrackingStart,
            onTrackingDismiss = viewModel::onTrackingDismiss,
        )
        AboutButton(
            modifier = Modifier.align(Alignment.TopStart).padding(MaterialTheme.spacing.medium),
            onClick = viewModel::openAboutDialog
        )
        AboutDialog(
            shouldShowDialog = uiState.shouldShowAboutDialog,
            onDismissRequest = viewModel::dismissAboutDialog
        )
        RecordButton(
            onClick = viewModel::onRecordClick,
            isRecording = uiState.operationState is OperationState.Recording,
            modifier =
                Modifier.align(Alignment.BottomCenter)
                    .padding(bottom = MaterialTheme.spacing.medium)
        )
        LocateButton(
            onTrackClick = viewModel::onTrackClick,
            onTripClick = viewModel::onTripClick,
            isGpsEnabled = uiState.isGpsEnabled,
            isTracking = uiState.operationState is OperationState.Tracking,
            modifier =
                Modifier.align(Alignment.BottomEnd)
                    .padding(
                        bottom = MaterialTheme.spacing.extraLarge,
                        end = MaterialTheme.spacing.medium
                    )
        )
    }
}

@Composable
fun OnPauseEffect(onPause: () -> Unit) {
    val lifeCycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifeCycle, onPause) {
        val observer = LifecycleEventObserver { source, event ->
            if (event == Lifecycle.Event.ON_PAUSE) {
                onPause()
            }
        }
        lifeCycle.addObserver(observer)
        onDispose { lifeCycle.removeObserver(observer) }
    }
}

@Composable
fun GpsRequester(
    shouldRequestGps: Boolean,
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit = {}
) {
    val context = LocalContext.current
    if (shouldRequestGps) {
        CallToActionDialog(
            textProvider = GpsActivationProvider(),
            onDismiss = onDismiss,
            onAction = {
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                context.startActivity(intent)
            },
            modifier = modifier,
        )
    }
}

@Preview
@Composable
private fun MapScreenPreview() {
    SurfacePreview { MapScreen() }
}
