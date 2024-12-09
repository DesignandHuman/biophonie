plugins {
    alias(libs.plugins.biophonie.android.feature)
    alias(libs.plugins.biophonie.android.compose)
    alias(libs.plugins.biophonie.android.databinding)
    alias(libs.plugins.biophonie.android.buildconfig)
    alias(libs.plugins.biophonie.android.hilt)
    alias(libs.plugins.androidx.navigationSafeArgs)
    alias(libs.plugins.dropshots)
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
    api(libs.androidx.appCompat)
    api(libs.androidx.compose.foundation.layout)
    api(libs.androidx.compose.runtime)
    api(libs.androidx.compose.ui)
    api(libs.androidx.constraintLayout)
    api(libs.androidx.cooordinatorLayout)
    api(libs.androidx.core)
    api(libs.androidx.fragment)
    api(libs.androidx.lifecycle.livedata.core)
    api(libs.androidx.lifecycle.viewmodel)
    api(libs.androidx.navigation)
    api(libs.bundles.map)
    api(libs.hilt.core)
    api(libs.hilt.dagger)
    api(libs.javax.inject)
    api(libs.mapbox.common)
    api(libs.mapbox.compose)
    api(libs.mapbox.core)
    api(libs.mapbox.geojson)
    api(libs.material)
    api(projects.core.data)
    api(projects.core.model)
    api(projects.soundwave)

    implementation(libs.androidx.activity)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.annotation)
    implementation(libs.androidx.compose.animation.graphics)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material)
    implementation(libs.androidx.compose.runtime.saveable)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.text)
    implementation(libs.androidx.compose.ui.unit)
    implementation(libs.androidx.coreKtx)
    implementation(libs.androidx.lifecycle.common)
    implementation(libs.androidx.lifecycle.livedata)
    implementation(libs.androidx.lifecycle.runtime)
    implementation(libs.androidx.navigation.fragment)
    implementation(libs.androidx.navigation.runtime)
    implementation(libs.androidx.vectordrawable)
    implementation(libs.bundles.compose)
    implementation(libs.coroutines.core)
    implementation(libs.gson)
    implementation(libs.mapbox.annotation)
    implementation(libs.mapbox.attribution)
    implementation(libs.mapbox.base)
    implementation(libs.mapbox.compass)
    implementation(libs.mapbox.gestures)
    implementation(libs.mapbox.localization)
    implementation(libs.mapbox.location)
    implementation(libs.mapbox.logo)
    implementation(libs.mapbox.plugin.gestures)
    implementation(libs.mapbox.plugin.overlay)
    implementation(libs.mapbox.plugin.scalebar)
    implementation(libs.mapbox.style)
    implementation(libs.mapbox.viewport)
    implementation(libs.navigation.dynamic.features.fragment)
    implementation(projects.core.assets)
    implementation(projects.core.network)
    implementation(projects.core.ui)
    implementation(projects.core.work)

    runtimeOnly(libs.androidx.workRuntime)

    androidTestRuntimeOnly(libs.androidx.compose.ui.test.manifest)
    androidTestRuntimeOnly(projects.core.testing)

    testImplementation(libs.androidx.coreTesting)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.jUnit)
    testImplementation(libs.mockito.core)
    testImplementation(projects.core.testing)

    androidTestImplementation(libs.androidx.compose.ui.test)
    androidTestImplementation(libs.androidx.compose.ui.test.junit)
    androidTestImplementation(libs.androidx.testCore)
    androidTestImplementation(libs.androidx.testJUnit)
    androidTestImplementation(libs.androidx.testMonitor)
    androidTestImplementation(libs.androidx.testRules)
    androidTestImplementation(libs.dropbox.differ)
    androidTestImplementation(libs.jUnit)
    androidTestImplementation(libs.mockwebserver)
    androidTestImplementation(libs.okhttp)
    androidTestImplementation(projects.core.testing)
}
