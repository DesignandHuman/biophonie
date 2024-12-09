plugins {
    alias(libs.plugins.biophonie.android.library)
    alias(libs.plugins.biophonie.android.hilt)
    alias(libs.plugins.biophonie.android.buildconfig)
    alias(libs.plugins.ksp)
}

android {
    namespace = "fr.labomg.biophonie.core.database"
    defaultConfig {
        ksp {
            arg("room.schemaLocation", "$projectDir/schemas")
        }
    }
}

dependencies {
    ksp(libs.androidx.roomCompiler)
    ksp(libs.moshi.codegen)

    api(libs.androidx.roomRuntime)
    api(libs.coroutines.core)
    api(libs.hilt.dagger)
    api(libs.javax.inject)
    api(projects.core.model)

    implementation(libs.androidx.roomCommon)
    implementation(libs.androidx.roomKtx)
    implementation(libs.androidx.sqlite)
    implementation(libs.hilt.core)
}
