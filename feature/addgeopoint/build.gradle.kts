plugins {
    alias(libs.plugins.biophonie.android.feature)
    alias(libs.plugins.biophonie.android.databinding)
    alias(libs.plugins.biophonie.android.buildconfig)
    alias(libs.plugins.androidx.navigationSafeArgs)
}

android {
    namespace = "fr.labomg.biophonie.feature.addgeopoint"

    buildTypes {
        getByName("release") {
            consumerProguardFiles("proguard-rules.pro")
        }
    }
}

dependencies {
    api(libs.androidx.appCompat)
    api(libs.androidx.fragment)
    api(libs.androidx.lifecycle.common)
    api(libs.androidx.lifecycle.livedata.core)
    api(libs.androidx.lifecycle.viewmodel)
    api(libs.androidx.lifecycle.viewmodel.savedState)
    api(libs.androidx.navigation)
    api(libs.androidx.recyclerview)
    api(libs.hilt.core)
    api(libs.hilt.dagger)
    api(libs.javax.inject)
    api(libs.material)
    api(projects.core.domain)
    api(projects.core.model)
    api(projects.soundwave)

    implementation(libs.androidx.activity)
    implementation(libs.androidx.activityKtx)
    implementation(libs.androidx.annotation)
    implementation(libs.androidx.cardview)
    implementation(libs.androidx.constraintLayout)
    implementation(libs.androidx.core)
    implementation(libs.androidx.navigation.fragment)
    implementation(libs.androidx.navigation.runtime)
    implementation(libs.navigation.dynamic.features.fragment)
    implementation(projects.core.assets)
    implementation(projects.core.data)
    implementation(projects.core.network)
    implementation(projects.core.ui)
}