plugins {
    alias(libs.plugins.biophonie.android.library)
    alias(libs.plugins.ksp)
}

android {
    namespace = "fr.labomg.biophonie.core.work"
}

dependencies {
    ksp(libs.androidx.hiltCompiler)
    ksp(libs.hilt.compiler)

    api(libs.androidx.hiltWork)
    api(libs.androidx.workRuntime)
    api(libs.hilt.dagger)
    api(libs.javax.inject)
    api(projects.core.data)

    implementation(libs.mapbox)
    implementation(libs.mapbox.common)
    implementation(libs.mapbox.core)
}