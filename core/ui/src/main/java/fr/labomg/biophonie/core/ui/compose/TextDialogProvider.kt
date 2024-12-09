package fr.labomg.biophonie.core.ui.compose

import androidx.annotation.StringRes
import fr.labomg.biophonie.core.ui.R

interface TextDialogProvider {
    @StringRes fun getTitle(): Int

    @StringRes fun getDescription(isPermanentlyDeclined: Boolean): Int

    @StringRes fun getButtonText(isPermanentlyDeclined: Boolean): Int
}

class LocationPermissionTextProvider : TextDialogProvider {
    override fun getTitle(): Int = R.string.location_access

    override fun getDescription(isPermanentlyDeclined: Boolean): Int =
        if (isPermanentlyDeclined) R.string.location_permission_permanently_declined
        else R.string.location_permission_required

    override fun getButtonText(isPermanentlyDeclined: Boolean): Int =
        if (isPermanentlyDeclined) R.string.go_to_settings else R.string.grant
}

class RecordPermissionTextProvider : TextDialogProvider {
    override fun getTitle(): Int = R.string.microphone_access

    override fun getDescription(isPermanentlyDeclined: Boolean): Int =
        if (isPermanentlyDeclined) R.string.record_permission_permanently_declined
        else R.string.record_permission_required

    override fun getButtonText(isPermanentlyDeclined: Boolean): Int =
        if (isPermanentlyDeclined) R.string.go_to_settings else R.string.grant
}

class GpsActivationProvider : TextDialogProvider {
    override fun getTitle(): Int = R.string.gps_activation

    override fun getDescription(isPermanentlyDeclined: Boolean): Int =
        R.string.gps_activation_required

    override fun getButtonText(isPermanentlyDeclined: Boolean): Int = R.string.go_to_gps_settings
}
