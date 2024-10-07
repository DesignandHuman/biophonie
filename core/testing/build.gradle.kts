plugins {
    alias(libs.plugins.biophonie.android.library)
    alias(libs.plugins.biophonie.android.hilt)
}

android {
    namespace = "fr.labomg.biophonie.core.testing"
}

dependencies {
    implementation(projects.core.network)
    implementation(libs.androidx.runner)
    implementation(libs.hilt.android.testing)
    implementation(libs.androidx.testCore)
}