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

    implementation(libs.hilt.core)

    runtimeOnly(libs.bundles.coroutines)
    runtimeOnly(libs.mapbox.common)
}
