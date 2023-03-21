@file:Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")

package com.example.biophonie.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.text.InputFilter
import android.text.SpannableStringBuilder
import android.view.View
import android.widget.EditText

class GPSCheck(private val locationCallBack: LocationCallBack) :
    BroadcastReceiver() {
    interface LocationCallBack {
        fun turnedOn()
        fun turnedOff()
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (isGPSEnabled(context))
            locationCallBack.turnedOn()
        else locationCallBack.turnedOff()
    }
}

fun isGPSEnabled(context: Context): Boolean{
    val locationManager =
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)||
            locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
}

fun dpToPx(context: Context, dp: Int): Int {
    return dp*(context.resources.displayMetrics.density).toInt()
}

fun EditText.setFiltersOnEditText() {
    val filter = InputFilter { source, start, end, _, _, _ ->
        return@InputFilter if (source is SpannableStringBuilder) {
            for (i in end - 1 downTo start) {
                val currentChar: Char = source[i]
                if (!Character.isLetterOrDigit(currentChar) && !Character.isSpaceChar(
                        currentChar
                    )
                ) {
                    source.delete(i, i + 1)
                }
            }
            source
        } else {
            val filteredStringBuilder = StringBuilder()
            for (i in start until end) {
                val currentChar: Char = source[i]
                if (Character.isLetterOrDigit(currentChar) || Character.isSpaceChar(
                        currentChar
                    )
                ) {
                    filteredStringBuilder.append(currentChar)
                }
            }
            filteredStringBuilder.toString()
        }
    }
    this.filters += filter
}

fun View.fadeIn() {
    visibility = View.VISIBLE
    alpha = 0f
    animate().alpha(1f).setDuration(500)
}