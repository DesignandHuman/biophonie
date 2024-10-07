plugins {
    alias(libs.plugins.biophonie.android.feature)
    alias(libs.plugins.biophonie.android.buildconfig)
    alias(libs.plugins.androidx.navigationSafeArgs)
}

android {
    namespace = "fr.labomg.biophonie.feature.addgeopoint"

    buildTypes {
        getByName("release") {
            consumerProguardFiles("proguard-rules.pro")
        }
    }
}

dependencies {
    implementation(projects.core.assets)
    implementation(projects.core.domain)
    implementation(projects.core.model)
    implementation(projects.core.ui)
    implementation(projects.soundwave)

    implementation(libs.bundles.map)
    implementation(libs.bundles.navigation)
    implementation(libs.glide)
}