plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    // kapt still needed to use databinding
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

    // needed to compile with JDK 17
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
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
            // allows debugging with a proxy
            manifestPlaceholders["usesCleartextTraffic"] = "true"
        }
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles (
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            // keeps debug symbols in AAB
            ndk {
                debugSymbolLevel = "FULL"
            }
        }
    }

    //test with Roboelectric
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
    lint {
        // avoid errors with res references in fragment_gallery.xml
        checkReleaseBuilds = false
    }
}

dependencies {
    // kotlin
    implementation(libs.kotlin.reflect)

    // android
    implementation(libs.androidx.coreKtx)
    implementation(libs.androidx.appCompat)
    implementation(libs.androidx.workRuntime)
    debugImplementation(libs.androidx.fragmentKtx)
    implementation(libs.androidx.securityCrypto) // encrypted shared preferences
    // support of new java classes such as Instant for older Android versions
    coreLibraryDesugaring(libs.bundles.desugar)

    // data layer
    implementation(libs.bundles.lifecycle)
    implementation(libs.bundles.coroutines)
    implementation(libs.bundles.remote)
    ksp(libs.moshi.codegen)
    implementation(libs.bundles.local)
    ksp(libs.androidx.roomCompiler)

    // ui layer
    implementation(libs.androidx.constraintLayout)
    implementation(libs.bundles.navigation)
    implementation(libs.bundles.map)
    implementation(libs.material) // bottomsheetplayer implementation
    implementation(libs.glide) // display picture
    implementation(project(":soundwave")) // display sound

    // development utils
    implementation(libs.timber)
    debugImplementation(libs.leakCanary)

    // testing dependencies
    testImplementation(libs.navigation.testing)
    testImplementation(libs.jUnit)
    testImplementation(libs.harmcrest)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.coreTesting)
    androidTestImplementation(libs.androidx.testCore)
    androidTestImplementation(libs.androidx.testRules)
    androidTestImplementation(libs.androidx.testJUnit)
    androidTestImplementation(libs.androidx.testEspresso)
    debugImplementation(libs.androidx.fragmentTesting)
}