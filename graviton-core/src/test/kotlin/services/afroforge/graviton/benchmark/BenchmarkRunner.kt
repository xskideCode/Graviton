package services.afroforge.graviton.benchmark

import org.openjdk.jmh.runner.Runner
import org.openjdk.jmh.runner.options.OptionsBuilder

object BenchmarkRunner {
    @JvmStatic
    fun main(args: Array<String>) {
        val opt = OptionsBuilder()
            .include(Vector3Benchmark::class.java.simpleName)
            .forks(1)
            .warmupIterations(1)
            .measurementIterations(3)
            .build()

        Runner(opt).run()
    }
}
