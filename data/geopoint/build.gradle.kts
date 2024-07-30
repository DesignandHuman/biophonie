plugins {
    alias(libs.plugins.biophonie.android.library)
    alias(libs.plugins.biophonie.android.hilt)
    alias(libs.plugins.biophonie.android.buildconfig)
    alias(libs.plugins.ksp)
}

android {
    namespace = "fr.labomg.biophonie.data.geopoint"
}

dependencies {
    implementation(projects.core.assets)
    implementation(projects.core.network)
    implementation(projects.core.utils)
    implementation(projects.data.user)
    
    implementation(libs.bundles.remote)
    implementation(libs.bundles.local)
    ksp(libs.moshi.codegen)
    ksp(libs.androidx.roomCompiler)
}
