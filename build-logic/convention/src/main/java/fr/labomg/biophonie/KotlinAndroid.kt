package fr.labomg.biophonie

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.gradle.dsl.kotlinExtension

/**
 * Configure base Kotlin with Android options
 */
internal fun Project.configureKotlinAndroid(
    commonExtension: CommonExtension<*, *, *, *, *, *>,
) {
    // set a specific Java version
    kotlinExtension.apply {
        jvmToolchain(17)
    }

    commonExtension.apply {
        compileSdk = libs.findVersion("sdk").get().toString().toInt()

        defaultConfig.minSdk = libs.findVersion("minSdk").get().toString().toInt()

        compileOptions.isCoreLibraryDesugaringEnabled = true

        // use ndk to keep debug symbols in AAB
        ndkVersion = "25.1.8937393"
        buildTypes {
            getByName("release") {
                ndk { debugSymbolLevel = "FULL" }
            }
        }

        dependencies {
            add("coreLibraryDesugaring", libs.findLibrary("desugar.jdk").get())
        }
    }
}

/**
 * Configure base Kotlin options for JVM (non-Android)
 */
internal fun Project.configureKotlinJvm() {
    kotlinExtension.apply {
        jvmToolchain(17)
    }
}