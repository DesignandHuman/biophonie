package fr.labomg.biophonie.core.ui.compose

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import fr.labomg.biophonie.core.ui.R

@Composable
fun CallToActionDialog(
    textProvider: TextDialogProvider,
    modifier: Modifier = Modifier,
    isPermanentlyDeclined: Boolean = false,
    onDismiss: () -> Unit = {},
    onAction: () -> Unit = {}
) {
    AlertDialog(
        icon = { Icon(Icons.Default.Info, contentDescription = null) },
        title = { Text(stringResource(textProvider.getTitle())) },
        text = { Text(stringResource(textProvider.getDescription(isPermanentlyDeclined))) },
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onAction) {
                Text(stringResource(textProvider.getButtonText(isPermanentlyDeclined)))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(id = R.string.cancel)) }
        },
        modifier = modifier
    )
}

@Preview
@Composable
private fun CallToActionDialogPreview() {
    SurfacePreview {
        CallToActionDialog(LocationPermissionTextProvider(), isPermanentlyDeclined = false)
    }
}
