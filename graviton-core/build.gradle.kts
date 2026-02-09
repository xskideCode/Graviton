plugins {
    alias(libs.plugins.kotlin.jvm)
    kotlin("kapt")
}

dependencies {
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.mockk)
    // JMH Benchmarking
    testImplementation(libs.jmh.core)
    testImplementation(libs.jmh.generator)
}

// Ensure JMH annotation processor runs
kapt {
    arguments {
        arg("jmh.generator", "default")
    }
}
