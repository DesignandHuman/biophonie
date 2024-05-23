import com.ncorti.ktfmt.gradle.tasks.KtfmtCheckTask

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.kapt) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.ktfmt)
}

ktfmt { kotlinLangStyle() }

tasks.register<Delete>("clean") { delete(rootProject.layout.buildDirectory) }

tasks.register<Copy>("installGitHooks") {
    description = "Copies the git hooks from /pre-commit to the .git folder."
    group = "git hooks"
    from("$rootDir/scripts/pre-commit")
    into("$rootDir/.git/hooks/")
    filePermissions {
        user {
            read = true
            execute = true
        }
        other.execute = false
    }
}

tasks.register<KtfmtCheckTask>("ktfmtPreCommit") {
    source = project.fileTree(rootDir)
    include("**/*.kt")
    include("**/*.kts")
}

afterEvaluate { tasks.getByPath(":app:preBuild").dependsOn(tasks.getByName("installGitHooks")) }
