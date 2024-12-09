plugins {
    alias(libs.plugins.biophonie.android.feature)
    alias(libs.plugins.biophonie.android.compose)
}

android {
    namespace = "fr.labomg.biophonie.feature.firstlaunch"
}

dependencies {
    kspAndroidTest(libs.hilt.android.compiler)

    api(libs.androidx.compose.foundation)
    api(libs.androidx.compose.foundation.layout)
    api(libs.androidx.compose.runtime)
    api(libs.androidx.compose.ui)
    api(libs.androidx.fragment)
    api(libs.androidx.lifecycle.viewmodel)
    api(libs.coroutines.core)
    api(libs.hilt.core)
    api(libs.hilt.dagger)
    api(libs.javax.inject)
    api(projects.core.data)

    implementation(libs.androidx.annotation)
    implementation(libs.androidx.appCompat)
    implementation(libs.androidx.compose.animation.core)
    implementation(libs.androidx.compose.animation.graphics)
    implementation(libs.androidx.compose.material)
    implementation(libs.androidx.compose.material.window.size)
    implementation(libs.androidx.compose.ui.geometry)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.text)
    implementation(libs.androidx.compose.ui.unit)
    implementation(libs.androidx.constraintLayout)
    implementation(libs.androidx.coreKtx)
    implementation(libs.androidx.lifecycle.common)
    implementation(libs.androidx.lifecycle.runtime)
    implementation(libs.androidx.navigation.runtime)
    implementation(libs.androidx.workRuntime)
    implementation(libs.bundles.compose)
    implementation(libs.material)
    implementation(projects.core.assets)
    implementation(projects.core.model)
    implementation(projects.core.network)
    implementation(projects.core.ui)
    implementation(projects.core.work)
    implementation(projects.soundwave)

    debugRuntimeOnly(libs.androidx.compose.ui.test.manifest)

    androidTestImplementation(libs.androidx.activity)
    androidTestImplementation(libs.androidx.compose.ui.test)
    androidTestImplementation(libs.androidx.compose.ui.test.junit)
    androidTestImplementation(libs.androidx.hiltWork)
    androidTestImplementation(libs.androidx.lifecycle.viewmodel.savedState)
    androidTestImplementation(libs.androidx.testCore)
    androidTestImplementation(libs.hilt.android.testing)
    androidTestImplementation(libs.jUnit)
    androidTestImplementation(projects.core.database)
    androidTestImplementation(projects.core.preferences)
    androidTestImplementation(projects.core.testing)
    androidTestImplementation(projects.core.utils)
}
