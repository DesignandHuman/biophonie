plugins {
    alias(libs.plugins.biophonie.android.application)
    alias(libs.plugins.biophonie.android.buildconfig)
    alias(libs.plugins.biophonie.android.databinding)
    alias(libs.plugins.biophonie.android.hilt)
    alias(libs.plugins.kapt)
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
        versionName = "0.2.0"
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
    implementation(projects.core.work)
    implementation(projects.feature.addgeopoint)
    implementation(projects.feature.exploregeopoints)
    implementation(projects.feature.firstlaunch)

    implementation(libs.androidx.appCompat)
    implementation(libs.androidx.hiltWork)
    implementation(libs.bundles.navigation)
    kapt(libs.androidx.hiltCompiler)
    kapt(libs.hilt.compiler)

    implementation(libs.androidx.workRuntime)
}
