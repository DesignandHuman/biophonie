package com.example.biophonie

import androidx.annotation.DrawableRes

const val REQUEST_CAMERA = 0
const val REQUEST_GALLERY = 1
const val PROPERTY_CACHE: String = "fromCache?"
const val PROPERTY_NAME: String = "name"
const val PROPERTY_ID: String = "id"

// adjust with res/values/templates.xml
val templates = mapOf<String, @DrawableRes Int>(
    "clearing" to R.drawable.clearing,
    "desert" to R.drawable.desert,
    "fields" to R.drawable.fields,
    "garden" to R.drawable.garden,
    "grassland" to R.drawable.grassland,
    "hedge" to R.drawable.hedge,
    "lake" to R.drawable.lake,
    "mountains" to R.drawable.mountains,
    "potager" to R.drawable.potager,
    "rainforest" to R.drawable.rainforest,
    "temperateforest" to R.drawable.temperateforest
)