plugins {
    alias(libs.plugins.kapt)
    alias(libs.plugins.biophonie.android.library)
    alias(libs.plugins.biophonie.android.databinding)
}

android {
    namespace = "fr.labomg.biophonie.core.ui"
}

dependencies {
    implementation(projects.core.assets)
    implementation(projects.data.geopoint)
    implementation(projects.soundwave)

    implementation(libs.androidx.appCompat)
    implementation(libs.androidx.constraintLayout)
    implementation(libs.material)
    implementation(libs.glide)
}