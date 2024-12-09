plugins {
    alias(libs.plugins.biophonie.android.application)
    alias(libs.plugins.biophonie.android.buildconfig)
    alias(libs.plugins.biophonie.android.databinding)
    alias(libs.plugins.biophonie.android.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "fr.labomg.biophonie"

    signingConfigs {
        getByName("debug") {
            keyPassword = "android"
        }
    }

    defaultConfig {
        versionCode = 4
        versionName = "0.3.0"
        vectorDrawables.useSupportLibrary = true
        signingConfig = signingConfigs.getByName("debug")
    }

    buildTypes {
        getByName("debug") {
            // allow debugging with a proxy
            manifestPlaceholders["usesCleartextTraffic"] = "true"
        }
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}

dependencies {
    ksp(libs.androidx.hiltCompiler)

    implementation(libs.androidx.activity)
    implementation(libs.androidx.appCompat)
    implementation(libs.androidx.datastore.core)
    implementation(libs.androidx.datastore.preferences.core)
    implementation(libs.androidx.fragment)
    implementation(libs.androidx.hiltWork)
    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.androidx.lifecycle.viewmodel.savedState)
    implementation(libs.androidx.workRuntime)
    implementation(libs.converter.moshi)
    implementation(libs.coroutines.core)
    implementation(libs.hilt.core)
    implementation(libs.hilt.dagger)
    implementation(libs.javax.inject)
    implementation(libs.okhttp)
    implementation(libs.retrofit)
    implementation(projects.core.assets)
    implementation(projects.core.data)
    implementation(projects.core.database)
    implementation(projects.core.domain)
    implementation(projects.core.location)
    implementation(projects.core.network)
    implementation(projects.core.preferences)
    implementation(projects.core.utils)
    implementation(projects.core.work)
    implementation(projects.feature.addgeopoint)
    implementation(projects.feature.exploregeopoints)
    implementation(projects.feature.firstlaunch)

    androidTestRuntimeOnly(libs.androidx.runner)
}
