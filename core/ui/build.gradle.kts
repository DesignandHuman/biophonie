plugins {
    alias(libs.plugins.kapt)
    alias(libs.plugins.biophonie.android.library.compose)
    alias(libs.plugins.biophonie.android.buildconfig)
    alias(libs.plugins.biophonie.android.databinding)
}

android {
    namespace = "fr.labomg.biophonie.core.ui"
}

dependencies {
    implementation(projects.core.assets)
    implementation(projects.soundwave)

    implementation(libs.androidx.appCompat)
    implementation(libs.androidx.constraintLayout)
    implementation(libs.androidx.compose.material)
    implementation(libs.material)
    implementation(libs.glide)
}