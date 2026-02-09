plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.detekt)
    alias(libs.plugins.ktlint)
}

allprojects {
    group = "services.afroforge"
    version = "0.1.0-SNAPSHOT"

    repositories {
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/")
    }
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "io.gitlab.arturbosch.detekt")
    apply(plugin = "org.jlleitschuh.gradle.ktlint")

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
            freeCompilerArgs.add("-Xjvm-default=all")
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
    
    ktlint {
        verbose.set(true)
        android.set(false)
        outputToConsole.set(true)
        ignoreFailures.set(false)
        enableExperimentalRules.set(false)
    }
    
    detekt {
        buildUponDefaultConfig = true
        allRules = false // Enable standard rules
        config.setFrom(rootProject.files("config/detekt/detekt.yml"))
    }
}
