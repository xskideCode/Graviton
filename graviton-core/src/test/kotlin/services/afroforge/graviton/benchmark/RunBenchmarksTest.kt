package services.afroforge.graviton.benchmark

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIfSystemProperty

class RunBenchmarksTest {
    @Test
    @EnabledIfSystemProperty(named = "benchmark", matches = "true")
    fun runBenchmarks() {
        BenchmarkRunner.main(emptyArray())
    }
}
