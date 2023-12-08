plugins {
    id ("com.android.application") version "8.0.0" apply false
    id ("org.jetbrains.kotlin.android") version "1.8.0" apply false
    id ("com.github.dcendents.android-maven") version "2.1" apply false
}

allprojects {
    repositories {
        google()
        jcenter()
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