package fr.labomg.biophonie

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.Project

/**
 * Configure base Kotlin with Android options
 */
internal fun Project.configureBuildConfig(
    commonExtension: CommonExtension<*, *, *, *, *>,
) {

    commonExtension.apply {
        buildFeatures {
            buildConfig = true
        }

        buildTypes {
            getByName("debug") {
                buildConfigField(
                    "String",
                    "BASE_URL",
                    "\"${(rootProject.property("BIOPHONIE_DEBUG_API_URL") ?: "") as String}\""
                )
            }
            getByName("release") {
                buildConfigField("String", "BASE_URL", "\"https://biophonie.fr\"")
            }
        }
    }
}