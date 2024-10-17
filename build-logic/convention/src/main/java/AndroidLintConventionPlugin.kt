
import com.ncorti.ktfmt.gradle.KtfmtExtension
import com.ncorti.ktfmt.gradle.tasks.KtfmtCheckTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.register

class AndroidLintConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.ncorti.ktfmt.gradle")
                apply("io.gitlab.arturbosch.detekt")
                apply("com.autonomousapps.dependency-analysis")
                apply("com.squareup.sort-dependencies")
            }

            configure<KtfmtExtension> { kotlinLangStyle() }

            tasks.register<KtfmtCheckTask>("ktfmtPreCommit") {
                source = project.fileTree(rootDir)
                include("**/*.kt")
            }
        }
    }
}
