import com.android.build.gradle.LibraryExtension
import fr.labomg.biophonie.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class AndroidFeatureConventionPlugin: Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("biophonie.android.library.compose")
                apply("biophonie.android.hilt")
                apply("biophonie.android.databinding")
            }

            extensions.configure<LibraryExtension> {
                defaultConfig {
                    testInstrumentationRunner = "fr.labomg.biophonie.core.testing.BiophonieTestRunner"
                }
                testOptions.animationsDisabled = true
            }

            dependencies {
                "androidTestImplementation"(project(":core:testing"))

                "implementation"(libs.findLibrary("androidx.hiltNavigation.compose").get())
                "implementation"(libs.findLibrary("androidx.lifecycle.viewmodel.compose").get())
                "implementation"(libs.findLibrary("androidx.lifecycle.runtime.compose").get())

                "implementation"(libs.findLibrary("timber").get())
                "implementation"(libs.findLibrary("androidx.fragmentKtx").get())
                "implementation"(libs.findBundle("lifecycle").get())
            }
        }
    }

}