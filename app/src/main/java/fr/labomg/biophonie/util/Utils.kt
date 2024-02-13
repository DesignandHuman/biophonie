package fr.labomg.biophonie.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.text.InputFilter
import android.text.SpannableStringBuilder
import android.view.View
import android.widget.EditText
import java.util.regex.Pattern

fun isGPSEnabled(context: Context): Boolean {
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
        locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
}

fun dpToPx(context: Context, dp: Int): Int {
    return dp * (context.resources.displayMetrics.density).toInt()
}

fun EditText.setFiltersOnEditText(strict: Boolean = false) {
    val filter = InputFilter { source, start, end, _, _, _ ->
        val ps = Pattern.compile(if (strict) "^[\\-\\p{L} ]+$" else "^[\\-\'’\\p{L} ]+$")
        return@InputFilter if (source is SpannableStringBuilder) {
            for (i in end - 1 downTo start) {
                val currentChar: Char = source[i]
                if (!currentChar.isAllowed(ps)) {
                    source.delete(i, i + 1)
                }
            }
            source
        } else {
            val filteredStringBuilder = StringBuilder()
            for (i in start until end) {
                val currentChar: Char = source[i]
                if (currentChar.isAllowed(ps)) {
                    filteredStringBuilder.append(currentChar)
                }
            }
            filteredStringBuilder.toString()
        }
    }
    this.filters += filter
}

private fun Char.isAllowed(ps: Pattern): Boolean = ps.matcher(this.toString()).matches()

private const val FADE_IN_DURATION = 500L

fun View.fadeIn() {
    visibility = View.VISIBLE
    alpha = 0f
    animate().alpha(1f).setDuration(FADE_IN_DURATION)
}

class GPSCheck(private val locationCallBack: LocationCallBack) : BroadcastReceiver() {
    interface LocationCallBack {
        fun turnedOn()

        fun turnedOff()
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (isGPSEnabled(context)) locationCallBack.turnedOn() else locationCallBack.turnedOff()
    }
}
