import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `kotlin-dsl`
}

group = "fr.labomg.biophonie.buildlogic"

// Configure the build-logic plugins to target JDK 17
java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
    }
}

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.ktfmt.gradlePlugin)
}

tasks {
    validatePlugins {
        enableStricterValidation = true
        failOnWarning = true
    }
}

gradlePlugin {
    plugins {
        register("androidApplication") {
            id = "biophonie.android.application"
            implementationClass = "AndroidApplicationConventionPlugin"
        }
        register("androidBuildConfig") {
            id = "biophonie.android.buildconfig"
            implementationClass = "AndroidBuildConfigConventionPlugin"
        }
        register("androidDataBinding") {
            id = "biophonie.android.databinding"
            implementationClass = "AndroidDataBindingConventionPlugin"
        }
        register("androidFeature") {
            id = "biophonie.android.feature"
            implementationClass = "AndroidFeatureConventionPlugin"
        }
        register("androidHilt") {
            id = "biophonie.android.hilt"
            implementationClass = "AndroidHiltConventionPlugin"
        }
        register("androidLibrary") {
            id = "biophonie.android.library"
            implementationClass = "AndroidLibraryConventionPlugin"
        }
        register("androidLint") {
            id = "biophonie.android.lint"
            implementationClass = "AndroidLintConventionPlugin"
        }
    }
}