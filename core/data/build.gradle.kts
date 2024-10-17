plugins {
    alias(libs.plugins.biophonie.android.library)
    alias(libs.plugins.biophonie.android.hilt)
    alias(libs.plugins.biophonie.android.buildconfig)
    alias(libs.plugins.ksp)
}

android {
    namespace = "fr.labomg.biophonie.core.data"
}

dependencies {
    ksp(libs.androidx.roomCompiler)
    ksp(libs.moshi.codegen)

    api(libs.coroutines.core)
    api(libs.hilt.dagger)
    api(libs.javax.inject)
    api(projects.core.database)
    api(projects.core.model)
    api(projects.core.network)
    api(projects.core.preferences)
    api(projects.core.utils)

    implementation(libs.hilt.core)
}
