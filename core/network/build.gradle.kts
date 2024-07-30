plugins {
    alias(libs.plugins.biophonie.android.library)
    alias(libs.plugins.biophonie.android.hilt)
    alias(libs.plugins.biophonie.android.buildconfig)
}

android {
    namespace = "fr.labomg.biophonie.core.network"

    buildTypes {
        getByName("release") {
            consumerProguardFiles("proguard-rules.pro")
        }
    }
}

dependencies {
    implementation(libs.bundles.remote)
}