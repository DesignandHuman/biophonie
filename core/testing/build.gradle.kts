plugins {
    alias(libs.plugins.biophonie.android.library)
    alias(libs.plugins.biophonie.android.hilt)
}

android {
    namespace = "fr.labomg.biophonie.core.testing"
}

dependencies {
    api(libs.androidx.runner)
    api(libs.hilt.dagger)
    api(libs.javax.inject)
    api(projects.core.data)

    debugApi(libs.androidx.activity)
    debugApi(libs.androidx.lifecycle.viewmodel)
    debugApi(libs.hilt.core)

    implementation(libs.hilt.android.testing)
    implementation(projects.core.model)
    implementation(projects.core.network)

    releaseImplementation(libs.hilt.core)

    runtimeOnly(libs.androidx.testCore)
}