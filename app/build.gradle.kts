plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    // kapt still needed to use databinding
    // (see https://issuetracker.google.com/issues/173030256#comment10)
    id("org.jetbrains.kotlin.kapt")
    id("com.google.devtools.ksp")
}

android {
    namespace = "fr.labomg.biophonie"
    compileSdk = 33

    signingConfigs {
        getByName("debug") {
            keyPassword = "android"
        }
    }

    // set a specific Java version
    java.toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }

    buildFeatures{
        dataBinding = true
        viewBinding = true
    }

    // use ndk to keep debug symbols in AAB
    android.ndkVersion = "26.1.10909125"

    defaultConfig {
        minSdk = 23
        targetSdk = 33
        versionCode = 3
        versionName = "0.2.0"
        vectorDrawables.useSupportLibrary = true
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
            proguardFiles (
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            // keep debug symbols in AAB
            ndk {
                debugSymbolLevel = "FULL"
            }
        }
    }

    testOptions {
        unitTests {
            // keep manifest to get rid of Robolectric warning
            isIncludeAndroidResources = true
        }
    }
    lint {
        // avoid errors with res references in fragment_gallery.xml
        checkReleaseBuilds = false
    }
}

dependencies {
    // ---kotlin--- //
    implementation(libs.kotlin.reflect)

    // ---android--- //
    implementation(libs.androidx.appCompat)
    implementation(libs.androidx.coreKtx)
    debugImplementation(libs.androidx.fragmentKtx)
    implementation(libs.androidx.securityCrypto) // encrypted shared preferences
    implementation(libs.androidx.workRuntime)
    coreLibraryDesugaring(libs.bundles.desugar) // support of new java classes such as
    // java.time.Instant for Android versions < 26

    // ---data layer--- //
    ksp(libs.androidx.roomCompiler)
    implementation(libs.bundles.coroutines)
    implementation(libs.bundles.lifecycle)
    implementation(libs.bundles.local)
    implementation(libs.bundles.remote)
    ksp(libs.moshi.codegen)

    // ---ui layer--- //
    implementation(libs.androidx.constraintLayout)
    implementation(libs.bundles.map)
    implementation(libs.bundles.navigation)
    implementation(libs.glide) // picture display
    implementation(libs.material) // BottomSheetPlayer implementation
    implementation(project(":soundwave")) // sound reading and display

    // ---development utils--- //
    debugImplementation(libs.leakCanary)
    implementation(libs.timber)

    // ---testing dependencies--- //
    testImplementation(libs.androidx.coreTesting)
    debugImplementation(libs.androidx.fragmentTesting)
    androidTestImplementation(libs.androidx.testCore)
    androidTestImplementation(libs.androidx.testEspresso)
    androidTestImplementation(libs.androidx.testJUnit)
    androidTestImplementation(libs.androidx.testRules)
    testImplementation(libs.harmcrest)
    testImplementation(libs.jUnit)
    testImplementation(libs.navigation.testing)
    testImplementation(libs.robolectric)
}