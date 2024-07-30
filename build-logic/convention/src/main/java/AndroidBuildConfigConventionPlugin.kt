import com.android.build.api.dsl.ApplicationExtension
import com.android.build.gradle.LibraryExtension
import fr.labomg.biophonie.configureBuildConfig
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class AndroidBuildConfigConventionPlugin: Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            when {
                pluginManager.hasPlugin("com.android.application") ->
                    extensions.configure<ApplicationExtension> { configureBuildConfig(this) }
                pluginManager.hasPlugin("com.android.library") ->
                    extensions.configure<LibraryExtension> { configureBuildConfig(this) }
            }
        }
    }

}