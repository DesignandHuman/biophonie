plugins {
    alias(libs.plugins.biophonie.android.library)
    alias(libs.plugins.biophonie.android.hilt)
}

android { namespace = "fr.labomg.biophonie.core.location" }

dependencyAnalysis {
    issues {
        onModuleStructure {
            severity("ignore")
        }
    }
}

dependencies {
    api(libs.coroutines.core)
    api(libs.hilt.dagger)
    api(libs.javax.inject)
    api(libs.mapbox.common)
}
