pluginManagement {
    includeBuild("build-logic")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven { url = uri("https://www.jitpack.io") }
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
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
                password = (extra["MAPBOX_DOWNLOAD_TOKEN"] ?: "") as String
            }
        }
    }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include(":app")
include(":core:assets")
include(":core:data")
include(":core:database")
include(":core:domain")
include(":core:location")
include(":core:model")
include(":core:network")
include(":core:testing")
include(":core:preferences")
include(":core:ui")
include(":core:utils")
include(":core:work")
include(":feature:addgeopoint")
include(":feature:exploregeopoints")
include(":feature:firstlaunch")

rootProject.name = "Biophonie"

// integrate local soundwave dependency
// requires soundwave PATH to be defined in global gradle.properties extra SOUNDWAVE_DIR
// TODO: consider moving WaveFormPlayer into this project
include(":soundwave")
project(":soundwave").projectDir = File((extra["SOUNDWAVE_DIR"] ?: "") as String)
