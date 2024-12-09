
import com.autonomousapps.DependencyAnalysisSubExtension
import com.ncorti.ktfmt.gradle.KtfmtExtension
import com.ncorti.ktfmt.gradle.tasks.KtfmtCheckTask
import io.gitlab.arturbosch.detekt.DetektCreateBaselineTask
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.register

class LintConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.ncorti.ktfmt.gradle")
                apply("io.gitlab.arturbosch.detekt")
                apply("com.autonomousapps.dependency-analysis")
                apply("com.squareup.sort-dependencies")
            }

            configure<DependencyAnalysisSubExtension> {
                issues {
                    onAny {
                        severity("fail")
                        exclude("com.jakewharton.timber:timber", "com.google.dagger:hilt-android")
                    }
                }
            }

            configure<KtfmtExtension> { kotlinLangStyle() }

            tasks.register<KtfmtCheckTask>("ktfmtPreCommit") {
                source = project.fileTree(rootDir)
                include("**/*.kt")
            }

            configure<DetektExtension> {
                baseline = file("${rootProject.projectDir}/config/detekt/baseline.xml")
            }

            tasks.register<DetektCreateBaselineTask>("detektGenerateBaseline") {
                description = "Overrides current baseline."
                buildUponDefaultConfig.set(true)
                ignoreFailures.set(true)
                parallel.set(true)
                setSource(files(rootDir))
                config.setFrom(files("$rootDir/config/detekt/detekt.yml"))
                baseline.set(file("$rootDir/config/detekt/baseline.xml"))
                include("**/*.kt")
                include("**/*.kts")
                exclude("**/resources/**")
                exclude("**/build/**")
            }
        }
    }
}
