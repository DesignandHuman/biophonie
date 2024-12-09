plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.androidx.navigationSafeArgs) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.dependency.analysis)
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.dropshots) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.kapt) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.ktfmt) apply false
    alias(libs.plugins.module.graph) apply false
    alias(libs.plugins.sort.dependencies) apply false
}

tasks.register<Delete>("clean") { delete(rootProject.layout.buildDirectory) }

tasks.register<Copy>("installGitHooks") {
    description = "Copies the git hooks from /pre-commit to the .git folder."
    group = "git hooks"
    from("$rootDir/scripts/pre-commit", "$rootDir/scripts/pre-push")
    into("$rootDir/.git/hooks/")
    filePermissions {
        user {
            read = true
            execute = true
        }
        other.execute = false
    }
}

tasks.register("modules") {
    rootProject.subprojects.forEach {
        if (it.tasks.findByPath("ktfmtPreCommit") != null)
            println(it.displayName.removePrefix("project ':").removeSuffix("'"))
    }
}

afterEvaluate { tasks.getByPath(":app:preBuild").dependsOn(tasks.getByName("installGitHooks")) }
