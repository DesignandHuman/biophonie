pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven { url = uri("https://www.jitpack.io") }
    }
}

// integrate local soundwave dependency
// requires soundwave PATH to be defined in global gradle.properties extra SOUNDWAVE_DIR
// TODO: consider moving WaveFormPlayer into this project
include(":soundwave")
project(":soundwave").projectDir = File((extra["SOUNDWAVE_DIR"] ?: "") as String)

include(":app")
rootProject.name = "Biophonie"