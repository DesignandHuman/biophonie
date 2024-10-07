plugins {
    alias(libs.plugins.biophonie.android.library)
    alias(libs.plugins.biophonie.android.hilt)
    alias(libs.plugins.biophonie.android.buildconfig)
}

android {
    namespace = "fr.labomg.biophonie.core.utils"
}

dependencies {
    implementation(libs.androidx.coreKtx)
    implementation(libs.bundles.map)
    implementation(libs.bundles.coroutines)
}