plugins {
    alias(libs.plugins.biophonie.android.library)
    alias(libs.plugins.biophonie.android.hilt)
    alias(libs.plugins.biophonie.android.buildconfig)
    alias(libs.plugins.ksp)
}

android {
    namespace = "fr.labomg.biophonie.core.data"
}

dependencyAnalysis {
    issues {
        onModuleStructure {
            severity("ignore")
        }
    }
}

dependencies {
    ksp(libs.androidx.roomCompiler)
    ksp(libs.moshi.codegen)

    api(libs.androidx.datastore.core)
    api(libs.androidx.datastore.preferences.core)
    api(libs.coroutines.core)
    api(libs.hilt.dagger)
    api(libs.javax.inject)
    api(libs.mapbox.common)
    api(projects.core.database)
    api(projects.core.location)
    api(projects.core.model)
    api(projects.core.network)
    api(projects.core.preferences)
    api(projects.core.utils)

    implementation(libs.androidx.datastore.preferences)
    implementation(libs.hilt.core)
}
