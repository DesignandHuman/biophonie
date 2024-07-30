plugins {
    alias(libs.plugins.biophonie.android.library)
    alias(libs.plugins.biophonie.android.hilt)
    alias(libs.plugins.biophonie.android.buildconfig)
    alias(libs.plugins.ksp)
}

android {
    namespace = "fr.labomg.biophonie.data.user"

    buildTypes {
        getByName("release") {
            consumerProguardFiles("proguard-rules.pro")
        }
    }
}

dependencies {
    implementation(projects.core.network)
    implementation(projects.core.utils)

    implementation(libs.bundles.remote)
    implementation(libs.bundles.local)
    implementation(libs.androidx.securityCrypto)
    ksp(libs.moshi.codegen)
    ksp(libs.androidx.roomCompiler)
}
