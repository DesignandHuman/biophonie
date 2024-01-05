plugins {
    id ("com.android.application") version "8.2.0" apply false
    id ("com.github.dcendents.android-maven") version "2.1" apply false
    // TODO version of kotlin should be used in ksp version
    id("com.google.devtools.ksp") version "1.8.0-1.0.9" apply false
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