plugins {
    alias(libs.plugins.biophonie.android.library)
    alias(libs.plugins.biophonie.android.hilt)
}

android {
    namespace = "fr.labomg.biophonie.core.domain"
}

dependencies {
    api(libs.coroutines.core)
    api(libs.hilt.dagger)
    api(libs.javax.inject)
    api(projects.core.data)
    api(projects.core.model)
    api(projects.core.utils)
}
