plugins {
    alias(libs.plugins.biophonie.android.feature)
    alias(libs.plugins.biophonie.android.databinding)
}

android {
    namespace = "fr.labomg.biophonie.feature.firstlaunch"
}

dependencies {
    implementation(projects.core.assets)
    implementation(projects.core.network)
    implementation(projects.core.ui)
    implementation(projects.data.user)
    implementation(projects.core.work)

    implementation(libs.androidx.workRuntime)
    implementation(libs.material)
}
