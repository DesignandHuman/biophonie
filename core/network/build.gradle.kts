plugins {
    alias(libs.plugins.biophonie.android.library)
    alias(libs.plugins.biophonie.android.hilt)
    alias(libs.plugins.biophonie.android.buildconfig)
    alias(libs.plugins.ksp)
}

android {
    namespace = "fr.labomg.biophonie.core.network"

    buildTypes {
        getByName("release") {
            consumerProguardFiles("proguard-rules.pro")
        }
    }
}

dependencies {
    ksp(libs.androidx.roomCompiler)
    ksp(libs.moshi.codegen)

    api(libs.bundles.remote)
    api(libs.hilt.dagger)
    api(libs.javax.inject)
    api(libs.okhttp)
    api(libs.okio)
    api(projects.core.model)
    api(projects.core.utils)

    implementation(libs.coroutines.core)
    implementation(libs.hilt.core)
    implementation(projects.core.assets)
}