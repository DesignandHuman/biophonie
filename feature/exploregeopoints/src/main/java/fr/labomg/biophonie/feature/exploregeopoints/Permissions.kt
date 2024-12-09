package fr.labomg.biophonie.feature.exploregeopoints

import android.Manifest
import androidx.activity.ComponentActivity
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import fr.labomg.biophonie.core.ui.compose.CallToActionDialog
import fr.labomg.biophonie.core.ui.compose.LocationPermissionTextProvider
import fr.labomg.biophonie.core.ui.compose.RecordPermissionTextProvider
import fr.labomg.biophonie.core.ui.compose.TextDialogProvider
import fr.labomg.biophonie.core.ui.compose.getActivity
import fr.labomg.biophonie.core.ui.compose.openAppSettings

@Composable
fun PermissionRequester(
    requestPermissions: List<String>,
    missingPermissions: List<String>,
    handlePermissionsResult: (Map<String, Boolean>) -> Unit,
    dismissDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    val permissionRequester =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestMultiplePermissions(),
            onResult = handlePermissionsResult
        )
    LaunchedEffect(requestPermissions) {
        if (requestPermissions.isNotEmpty())
            permissionRequester.launch(requestPermissions.toTypedArray())
    }
    PermissionDialogs(
        missingPermissions = missingPermissions.reversed(),
        onDismiss = dismissDialog,
        permissionRequester = permissionRequester,
        modifier = modifier
    )
}

@Composable
private fun PermissionDialogs(
    missingPermissions: List<String>,
    onDismiss: () -> Unit,
    permissionRequester:
        ManagedActivityResultLauncher<Array<String>, Map<String, @JvmSuppressWildcards Boolean>>,
    modifier: Modifier = Modifier
) {
    val activity = LocalContext.current.getActivity() ?: return
    missingPermissions.forEach { permission ->
        val isPermanentlyDeclined =
            !ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
        val onAction =
            createOnActionHandler(
                activity = activity,
                permission = permission,
                isPermanentlyDeclined = isPermanentlyDeclined,
                onDismiss = onDismiss,
                permissionRequester = permissionRequester
            )
        CallToActionDialog(
            textProvider = getTextProviderForPermission(permission),
            onDismiss = onDismiss,
            isPermanentlyDeclined = isPermanentlyDeclined,
            onAction = onAction,
            modifier = modifier
        )
    }
}

private fun getTextProviderForPermission(permission: String): TextDialogProvider =
    if (permission == Manifest.permission.RECORD_AUDIO) {
        RecordPermissionTextProvider()
    } else {
        LocationPermissionTextProvider()
    }

private fun createOnActionHandler(
    activity: ComponentActivity,
    permission: String,
    isPermanentlyDeclined: Boolean,
    onDismiss: () -> Unit,
    permissionRequester: ActivityResultLauncher<Array<String>>
): () -> Unit = {
    onDismiss()
    if (isPermanentlyDeclined) {
        activity.openAppSettings()
    } else {
        permissionRequester.launch(arrayOf(permission))
    }
}
