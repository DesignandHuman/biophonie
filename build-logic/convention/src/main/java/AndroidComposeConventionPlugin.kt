
import com.android.build.gradle.LibraryExtension
import fr.labomg.biophonie.configureAndroidCompose
import fr.labomg.biophonie.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class AndroidComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.android.library")
                apply("org.jetbrains.kotlin.android")
                apply("org.jetbrains.kotlin.plugin.compose")
            }

            extensions.configure<LibraryExtension> {
                configureAndroidCompose(this)
            }

            dependencies {
                val composeBom = platform(libs.findLibrary("androidx.compose.bom").get())
                "androidTestImplementation"(composeBom)
                "api"(composeBom)
                "implementation"(composeBom)

                "implementation"(libs.findLibrary("androidx-compose-ui-preview").get())
                "debugImplementation"(libs.findLibrary("androidx-compose-ui-tooling").get())
            }
        }
    }
}
