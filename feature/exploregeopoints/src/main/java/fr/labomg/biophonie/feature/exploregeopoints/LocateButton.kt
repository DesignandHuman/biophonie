package fr.labomg.biophonie.feature.exploregeopoints

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import fr.labomg.biophonie.core.ui.compose.OutlinedFloatingActionButton
import fr.labomg.biophonie.core.ui.compose.SurfacePreview

@Composable
fun LocateButton(
    isGpsEnabled: Boolean,
    isTracking: Boolean,
    onTrackClick: () -> Unit,
    onTripClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val iconResource =
        when {
            isTracking && isGpsEnabled -> R.drawable.ic_trip
            !isTracking && isGpsEnabled -> R.drawable.ic_baseline_location_searching
            else -> R.drawable.ic_baseline_location_disabled
        }
    val contentDescription = if (isTracking) R.string.trip else R.string.locate
    val onClick = if (isTracking) onTripClick else onTrackClick

    OutlinedFloatingActionButton(
        onClick = onClick,
        painter = painterResource(iconResource),
        contentDesc = contentDescription,
        modifier = modifier
    )
}

@Preview
@Composable
private fun LocateButtonPreview() {
    SurfacePreview {
        LocateButton(isGpsEnabled = true, isTracking = false, onTrackClick = {}, onTripClick = {})
    }
}
