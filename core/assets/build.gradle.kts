plugins {
    alias(libs.plugins.biophonie.android.library)
}

android {
    namespace = "fr.labomg.biophonie.core.assets"
}

dependencies {
    //needed to override soundwave color resources
    implementation(projects.soundwave)

    implementation(libs.androidx.appCompat)
    implementation(libs.material)
}