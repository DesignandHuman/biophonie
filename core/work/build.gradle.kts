plugins {
    alias(libs.plugins.biophonie.android.library)
    alias(libs.plugins.kapt)
}

android {
    namespace = "fr.labomg.biophonie.core.work"
}

dependencies {
    implementation(projects.core.data)
    implementation(libs.androidx.workRuntime)
    implementation(libs.androidx.hiltWork)
    kapt(libs.androidx.hiltCompiler)
    kapt(libs.hilt.compiler)

    implementation(libs.bundles.map)
}