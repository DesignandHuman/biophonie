import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class AndroidDataBindingConventionPlugin: Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            when {
                pluginManager.hasPlugin("com.android.application") ->
                    configure<ApplicationExtension> {
                        buildFeatures {
                            dataBinding = true
                            viewBinding = true
                        }
                    }

                pluginManager.hasPlugin("com.android.library") ->
                    configure<com.android.build.api.dsl.LibraryExtension> {
                        buildFeatures {
                            dataBinding = true
                            viewBinding = true
                        }
                    }
            }
        }
    }
}