import com.android.build.api.dsl.ApplicationExtension
import fr.labomg.biophonie.configureKotlinAndroid
import fr.labomg.biophonie.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager){
                apply("com.android.application")
                apply("org.jetbrains.kotlin.android")
                apply("com.jraska.module.graph.assertion")
                apply("biophonie.android.lint")
            }

            extensions.configure<ApplicationExtension> {
                configureKotlinAndroid(this)
                defaultConfig.targetSdk = libs.findVersion("sdk").get().toString().toInt()
            }

            dependencies {
                "implementation"(libs.findLibrary("timber").get())
            }
        }
    }
}