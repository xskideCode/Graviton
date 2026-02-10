package services.afroforge.graviton.benchmark

import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.BenchmarkMode
import org.openjdk.jmh.annotations.Mode
import org.openjdk.jmh.annotations.OutputTimeUnit
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.State
import services.afroforge.graviton.math.MutableVector3
import services.afroforge.graviton.math.Vector3
import java.util.concurrent.TimeUnit

@State(Scope.Thread)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
open class Vector3Benchmark {
    private val v1 = MutableVector3(1.0, 2.0, 3.0)
    private val v2 = Vector3(4.0, 5.0, 6.0)

    @Benchmark
    fun testAdd(): MutableVector3 {
        return v1.add(v2)
    }

    @Benchmark
    fun testMultiply(): MutableVector3 {
        return v1.multiply(2.5)
    }

    @Benchmark
    fun testNormalize(): MutableVector3 {
        return v1.normalize()
    }
}
