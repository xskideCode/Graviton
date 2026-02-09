plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.shadow)
}

dependencies {
    implementation(project(":graviton-core"))
    compileOnly(libs.paper.api)
    implementation(libs.adventure.api)
    implementation(libs.adventure.text.minimessage)
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.mockk)
    testImplementation(libs.paper.api)
}

tasks.processResources {
    val props = mapOf("version" to version)
    inputs.properties(props)
    filteringCharset = "UTF-8"
    filesMatching("plugin.yml") {
        expand(props)
    }
}

tasks.shadowJar {
    archiveClassifier.set("")
    relocate("kotlin", "services.afroforge.graviton.libs.kotlin")
}

tasks.build {
    dependsOn(tasks.shadowJar)
}
