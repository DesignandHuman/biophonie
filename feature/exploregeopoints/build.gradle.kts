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
    api(libs.androidx.appCompat)
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
    api(libs.mapbox.base)
    api(libs.mapbox.geojson)
    api(libs.mapbox.gestures)
    api(libs.material)
    api(projects.core.data)
    api(projects.core.model)
    api(projects.soundwave)

    implementation(libs.androidx.activity)
    implementation(libs.androidx.annotation)
    implementation(libs.androidx.appCompat.resources)
    implementation(libs.androidx.coreKtx)
    implementation(libs.androidx.lifecycle.common)
    implementation(libs.androidx.lifecycle.livedata)
    implementation(libs.androidx.navigation.fragment)
    implementation(libs.androidx.navigation.runtime)
    implementation(libs.androidx.vectordrawable)
    implementation(libs.coroutines.core)
    implementation(libs.mapbox.animation)
    implementation(libs.mapbox.annotation)
    implementation(libs.mapbox.attribution)
    implementation(libs.mapbox.common)
    implementation(libs.mapbox.compass)
    implementation(libs.mapbox.core)
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
    implementation(projects.core.database)
    implementation(projects.core.network)
    implementation(projects.core.ui)
    implementation(projects.core.utils)

    androidTestRuntimeOnly(libs.androidx.testCore)
    androidTestRuntimeOnly(projects.core.testing)

    testRuntimeOnly(libs.robolectric)
}