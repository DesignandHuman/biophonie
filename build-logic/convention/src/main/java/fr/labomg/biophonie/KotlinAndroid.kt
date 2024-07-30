package fr.labomg.biophonie

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.jvm.toolchain.JavaToolchainService
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.gradleKotlinDsl
import org.gradle.kotlin.dsl.provideDelegate
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.kotlinExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.gradle.tasks.KotlinJavaToolchain
import org.jetbrains.kotlin.gradle.utils.toSetOrEmpty

/**
 * Configure base Kotlin with Android options
 */
internal fun Project.configureKotlinAndroid(
    commonExtension: CommonExtension<*, *, *, *, *>,
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
