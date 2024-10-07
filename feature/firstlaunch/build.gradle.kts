plugins {
    alias(libs.plugins.biophonie.android.feature)
}

android {
    namespace = "fr.labomg.biophonie.feature.firstlaunch"

}

dependencies {
    implementation(projects.core.assets)
    implementation(projects.core.data)
    implementation(projects.core.network)
    androidTestImplementation(projects.core.testing)
    implementation(projects.core.ui)
    implementation(projects.core.utils)
    implementation(projects.core.work)

    val composeBom = platform(libs.androidx.compose.bom)
    implementation(composeBom)
    androidTestImplementation(composeBom)
    implementation(libs.androidx.compose.animation)
    implementation(libs.androidx.compose.material)
    implementation(libs.androidx.compose.material.window.size)

    androidTestImplementation(libs.hilt.android.testing)
    kaptAndroidTest(libs.hilt.android.compiler)

    androidTestImplementation(libs.androidx.compose.ui.test.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    implementation(libs.bundles.navigation)
    implementation(libs.androidx.workRuntime)
    implementation(libs.material)
}
