plugins {
    alias(libs.plugins.biophonie.android.library)
}

android {
    namespace = "fr.labomg.biophonie.core.ui"
}

dependencies {
    implementation(libs.androidx.appCompat)
}