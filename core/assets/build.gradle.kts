plugins {
    alias(libs.plugins.biophonie.android.library)
}

android {
    namespace = "fr.labomg.biophonie.core.assets"
}

dependencies {
    implementation(libs.androidx.appCompat)
    implementation(libs.material)
    //needed to override soundwave color resources
    implementation(projects.soundwave)
}
