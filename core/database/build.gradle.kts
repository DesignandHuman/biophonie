plugins {
    alias(libs.plugins.biophonie.android.library)
    alias(libs.plugins.biophonie.android.hilt)
    alias(libs.plugins.biophonie.android.buildconfig)
    alias(libs.plugins.ksp)
}

android { namespace = "fr.labomg.biophonie.core.database" }

dependencies {
    ksp(libs.androidx.roomCompiler)
    ksp(libs.moshi.codegen)

    api(libs.androidx.roomRuntime)
    api(libs.hilt.dagger)
    api(libs.javax.inject)
    api(projects.core.model)

    implementation(libs.androidx.roomCommon)
    implementation(libs.androidx.sqlite)
    implementation(libs.hilt.core)
    implementation(projects.core.utils)
}
