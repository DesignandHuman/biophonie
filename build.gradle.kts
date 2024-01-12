plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kapt) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.ksp) apply false
}

allprojects {
    repositories {
        google()
        mavenCentral()
        maven {
            url = uri("https://jitpack.io")
        }
        maven {
            url = uri("https://api.mapbox.com/downloads/v2/releases/maven")
            authentication {
                create<BasicAuthentication>("basic")
            }
            credentials {
                username = "mapbox"
                password = (project.properties["MAPBOX_DOWNLOAD_TOKEN"] ?: "") as String
            }
        }
    }
}

tasks.register<Delete>("clean"){
    delete(rootProject.buildDir)
}