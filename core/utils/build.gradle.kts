plugins {
    alias(libs.plugins.biophonie.android.library)
    alias(libs.plugins.biophonie.android.hilt)
    alias(libs.plugins.biophonie.android.buildconfig)
}

android {
    namespace = "fr.labomg.biophonie.core.utils"
}

dependencies {
    api(libs.coroutines.core)
    api(libs.hilt.dagger)
    api(libs.javax.inject)
    api(libs.mapbox.base)
    api(libs.mapbox.geojson)

    implementation(libs.hilt.core)
    implementation(libs.mapbox.common)
    implementation(libs.mapbox.location)

    runtimeOnly(libs.bundles.coroutines)
}