plugins {
    alias(libs.plugins.biophonie.android.library)
    alias(libs.plugins.biophonie.android.hilt)
    alias(libs.plugins.biophonie.android.buildconfig)
    alias(libs.plugins.ksp)
}

android {
    namespace = "fr.labomg.biophonie.core.preferences"
}

dependencies {
    api(libs.hilt.dagger)
    api(libs.javax.inject)
    api(projects.core.model)

    implementation(libs.androidx.coreKtx)
    implementation(libs.androidx.securityCrypto)
    implementation(libs.androidx.securityCryptoKtx)
    implementation(libs.hilt.core)
}
