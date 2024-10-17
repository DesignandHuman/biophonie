import com.android.build.gradle.LibraryExtension
import fr.labomg.biophonie.configureKotlinAndroid
import fr.labomg.biophonie.libs
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.android.library")
                apply("org.jetbrains.kotlin.android")
                apply("biophonie.android.lint")
            }

            extensions.configure<LibraryExtension> {
                defaultConfig.targetSdk = libs.findVersion("sdk").get().toString().toInt()
                configureKotlinAndroid(this)
            }

            configure<DetektExtension> {
                config.setFrom(
                    files(
                        "$rootDir/config/detekt/detekt.yml",
                        "$rootDir/config/detekt/compose.yml"
                    )
                )
            }

            dependencies {
                "implementation"(libs.findLibrary("timber").get())
                "detektPlugins"(libs.findLibrary("detekt.compose.rules").get())
            }
        }
    }
}
