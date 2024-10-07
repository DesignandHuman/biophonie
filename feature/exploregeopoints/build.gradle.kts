plugins {
    alias(libs.plugins.biophonie.android.feature)
    alias(libs.plugins.biophonie.android.databinding)
    alias(libs.plugins.biophonie.android.buildconfig)
    alias(libs.plugins.androidx.navigationSafeArgs)
}

android {
    namespace = "fr.labomg.biophonie.feature.exploregeopoints"

    defaultConfig {
        resValue("string", "mapbox_access_token", (extra["MAPBOX_ACCESS_TOKEN"] ?: "") as String)
        resValue("string", "style_url", (extra["BIOPHONIE_STYLE_URL"] ?: "") as String)
    }

    buildTypes {
        getByName("release") {
            consumerProguardFiles("proguard-rules.pro")
        }
    }
}

dependencies {
    implementation(projects.core.assets)
    implementation(projects.core.data)
    implementation(projects.core.ui)
    implementation(projects.core.utils)
    implementation(projects.soundwave)
    implementation(projects.core.model)
    implementation(projects.core.network)

    implementation(libs.androidx.workRuntime)
    implementation(libs.glide)
    implementation(libs.material) // BottomSheetPlayer implementation
    implementation(libs.bundles.map)
    implementation(libs.bundles.navigation)

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