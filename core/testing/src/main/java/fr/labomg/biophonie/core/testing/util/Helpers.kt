package fr.labomg.biophonie.core.testing.util

import android.app.Instrumentation

fun Instrumentation.readAsset(fileName: String): String {
    return context.assets.open(fileName).bufferedReader().use { it.readText() }
}
