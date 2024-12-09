plugins {
    alias(libs.plugins.biophonie.android.library)
    alias(libs.plugins.biophonie.android.buildconfig)
    alias(libs.plugins.biophonie.android.databinding)
    alias(libs.plugins.biophonie.android.compose)
}

android {
    namespace = "fr.labomg.biophonie.core.ui"
}

dependencies {
    api(libs.androidx.activity)
    api(libs.androidx.appCompat)
    api(libs.androidx.compose.runtime)
    api(libs.androidx.compose.ui)
    api(libs.androidx.compose.ui.text)
    api(libs.material)
    api(projects.soundwave)

    implementation(libs.androidx.annotation)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.foundation.layout)
    implementation(libs.androidx.compose.material)
    implementation(libs.androidx.compose.material.icons)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.unit)
    implementation(libs.androidx.coreKtx)
    implementation(libs.glide)
    implementation(projects.core.assets)
}
