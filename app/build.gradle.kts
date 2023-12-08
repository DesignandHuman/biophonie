plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    // kapt still needed to use databinding
    id("org.jetbrains.kotlin.kapt")
    id("com.google.devtools.ksp")
}

val kotlinVersion = "1.8.0"
val roomVersion = "2.5.2"

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
        // neeeded because errors with res references in fragment_gallery.xml
        checkReleaseBuilds = false
    }
}

dependencies {
    implementation("androidx.security:security-crypto-ktx:1.1.0-alpha06")
    androidTestImplementation("androidx.test:rules:1.5.0")
    val navVersion = "2.3.5"

    // allows support of new java classes such as Instant for older Android versions
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs_nio:2.0.3")
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.3")

    implementation(fileTree("dir" to "libs", "include" to arrayOf("*.jar")))
    implementation(project(":soundwave"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")

    debugImplementation("androidx.fragment:fragment-ktx:1.6.1")
    implementation("androidx.core:core-ktx:1.10.1")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.lifecycle:lifecycle-extensions:2.2.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.1")
    implementation("org.jetbrains.kotlin:kotlin-reflect:${kotlinVersion}")

    implementation("androidx.room:room-runtime:${roomVersion}")
    ksp("androidx.room:room-compiler:${roomVersion}")
    implementation("androidx.room:room-ktx:${roomVersion}")

    // implementation("com.github.Haransis:WaveFormPlayer:1.3.0")
    implementation("com.google.android.material:material:1.9.0")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")
    implementation("com.squareup.moshi:moshi:1.15.0")
    implementation("com.squareup.moshi:moshi-kotlin:1.15.0")
    implementation("com.squareup.retrofit2:converter-moshi:2.9.0")
    ksp("com.squareup.moshi:moshi-kotlin-codegen:1.15.0")

    implementation("com.jakewharton.timber:timber:5.0.1")

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.hamcrest:hamcrest:2.2")
    testImplementation("org.robolectric:robolectric:4.3.1")
    debugImplementation("androidx.test:core:1.5.0")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    debugImplementation("androidx.fragment:fragment-testing:1.6.1")
    testImplementation("androidx.arch.core:core-testing:2.2.0")

    implementation("com.mapbox.maps:android:10.6.2")
    implementation("com.github.bumptech.glide:glide:4.11.0")

    // Kotlin
    implementation("androidx.navigation:navigation-fragment-ktx:${navVersion}")
    implementation("androidx.navigation:navigation-ui-ktx:${navVersion}")

    // Dynamic Feature Module Support
    implementation("androidx.navigation:navigation-dynamic-features-fragment:${navVersion}")

    // Testing Navigation
    androidTestImplementation("androidx.navigation:navigation-testing:${navVersion}")

    implementation("androidx.work:work-runtime-ktx:2.4.0")
    implementation("com.github.pengrad:mapscaleview:1.6.0")

    debugImplementation("com.squareup.leakcanary:leakcanary-android:2.10")
}