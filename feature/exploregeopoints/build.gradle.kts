plugins {
    alias(libs.plugins.biophonie.android.feature)
    alias(libs.plugins.biophonie.android.databinding)
    alias(libs.plugins.biophonie.android.buildconfig)
}

android {
    namespace = "fr.labomg.biophonie.feature.exploregeopoints"

    defaultConfig {
        resValue("string", "mapbox_access_token", (extra["MAPBOX_ACCESS_TOKEN"] ?: "") as String)
        resValue("string", "style_url", (extra["BIOPHONIE_STYLE_URL"] ?: "") as String)
    }
}

dependencies {
    implementation(projects.core.utils)
    implementation(projects.core.assets)
    implementation(projects.core.ui)
    implementation(projects.data.user)
    implementation(projects.core.work)
    implementation(projects.soundwave)
    implementation(projects.data.geopoint)
    implementation(projects.data.geopoint)
    implementation(projects.core.network)

    implementation(libs.androidx.workRuntime)
    implementation(libs.glide)
    implementation(libs.material) // BottomSheetPlayer implementation
    implementation(libs.bundles.map)

    // ---testing dependencies--- //
    testImplementation(libs.androidx.coreTesting)
    debugImplementation(libs.androidx.fragmentTesting)
    androidTestImplementation(libs.androidx.testCore)
    androidTestImplementation(libs.androidx.testEspresso)
    androidTestImplementation(libs.androidx.testJUnit)
    androidTestImplementation(libs.androidx.testRules)
    testImplementation(libs.hamcrest)
    testImplementation(libs.jUnit)
    testImplementation(libs.navigation.testing)
    testImplementation(libs.robolectric)
}