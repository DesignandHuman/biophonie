package fr.labomg.biophonie.feature.addgeopoint

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

data class Landscape(@DrawableRes var image: Int, @StringRes var title: Int)

data class DialogAdapterItem(var text: String, var icon: Int) {
    override fun toString(): String = text
}
