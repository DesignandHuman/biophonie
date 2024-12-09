plugins {
    alias(libs.plugins.biophonie.android.library)
    alias(libs.plugins.biophonie.android.hilt)
}

android {
    namespace = "fr.labomg.biophonie.core.testing"
}

dependencies {
    api(libs.androidx.activity)
    api(libs.androidx.lifecycle.viewmodel)
    api(libs.androidx.runner)
    api(libs.coroutines.core)
    api(libs.hilt.core)
    api(libs.hilt.dagger)
    api(libs.jUnit)
    api(libs.javax.inject)
    api(libs.mapbox.common)
    api(projects.core.data)
    api(projects.core.model)

    implementation(libs.coroutines.test)
    implementation(libs.hilt.android.testing)
    implementation(projects.core.network)

    runtimeOnly(libs.androidx.testCore)
}
