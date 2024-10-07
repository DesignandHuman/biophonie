plugins {
    alias(libs.plugins.biophonie.android.library)
}

android {
    namespace = "fr.labomg.biophonie.core.model"
}

dependencies {

    implementation(project(":core:utils"))
}
