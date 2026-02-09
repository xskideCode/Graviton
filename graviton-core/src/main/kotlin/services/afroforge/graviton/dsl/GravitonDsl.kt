@file:Suppress("TooManyFunctions", "MagicNumber")

package services.afroforge.graviton.dsl

import services.afroforge.graviton.api.BillboardMode
import services.afroforge.graviton.api.Effect
import services.afroforge.graviton.api.EffectMetadata
import services.afroforge.graviton.api.EmitterConfig
import services.afroforge.graviton.api.EmitterShape
import services.afroforge.graviton.api.EmitterType
import services.afroforge.graviton.api.ParticleConfig
import services.afroforge.graviton.data.Color
import services.afroforge.graviton.data.ColorRange
import services.afroforge.graviton.data.Gradient
import services.afroforge.graviton.data.Keyframe
import services.afroforge.graviton.data.ValueGradient
import services.afroforge.graviton.data.ValueRange
import services.afroforge.graviton.math.Vector3

/**
 * DSL marker to prevent misuse of nested builders.
 */
@DslMarker
annotation class GravitonDsl

/**
 * Create a particle effect with a type-safe DSL.
 */
@GravitonDsl
fun effect(
    id: String,
    block: EffectBuilder.() -> Unit,
): Effect {
    val builder = EffectBuilder(id)
    builder.block()
    return builder.build()
}

/**
 * Effect builder.
 */
@GravitonDsl
class EffectBuilder(private val id: String) {
    private var emitterConfig: EmitterConfig = EmitterConfig()
    private var particleConfig: ParticleConfig = ParticleConfig()
    private var metadata: EffectMetadata = EffectMetadata()

    /**
     * Configure the emitter.
     */
    fun emitter(block: EmitterConfigBuilder.() -> Unit) {
        val builder = EmitterConfigBuilder()
        builder.block()
        emitterConfig = builder.build()
    }

    /**
     * Configure particle behavior.
     */
    fun particle(block: ParticleConfigBuilder.() -> Unit) {
        val builder = ParticleConfigBuilder()
        builder.block()
        particleConfig = builder.build()
    }

    /**
     * Configure effect metadata.
     */
    fun metadata(block: EffectMetadataBuilder.() -> Unit) {
        val builder = EffectMetadataBuilder()
        builder.block()
        metadata = builder.build()
    }

    fun build(): Effect =
        Effect(
            id = id,
            emitter = emitterConfig,
            particle = particleConfig,
            metadata = metadata,
        )
}

/**
 * Emitter configuration builder.
 */
@GravitonDsl
class EmitterConfigBuilder {
    var type: EmitterType = EmitterType.CONTINUOUS
    var rate: Double = 10.0
    var burstCount: Int = 100
    var shape: EmitterShape = EmitterShape.Point
    var maxParticles: Int = 1000
    var tickRate: Int = 20

    fun build(): EmitterConfig =
        EmitterConfig(
            type = type,
            rate = rate,
            burstCount = burstCount,
            shape = shape,
            maxParticles = maxParticles,
            tickRate = tickRate,
        )
}

/**
 * Particle configuration builder.
 */
@GravitonDsl
class ParticleConfigBuilder {
    var lifetime: ValueRange<Double> = ValueRange.constant(1.0)
    var color: ColorRange = ColorRange.constant(Color.WHITE)
    var scale: ValueGradient = ValueGradient.constant(1.0)
    var velocity: ValueRange<Vector3> = ValueRange.constant(Vector3.ZERO)
    var gravity: Vector3 = ParticleConfig.EARTH_GRAVITY
    var drag: Double = 0.0
    var rotation: ValueRange<Double> = ValueRange.constant(0.0)
    var billboard: BillboardMode = BillboardMode.CENTER

    fun build(): ParticleConfig =
        ParticleConfig(
            lifetime = lifetime,
            color = color,
            scale = scale,
            velocity = velocity,
            gravity = gravity,
            drag = drag,
            rotation = rotation,
            billboard = billboard,
        )
}

/**
 * Effect metadata builder.
 */
@GravitonDsl
class EffectMetadataBuilder {
    var name: String = ""
    var description: String = ""
    var author: String = ""
    var tags: Set<String> = emptySet()

    fun build(): EffectMetadata =
        EffectMetadata(
            name = name,
            description = description,
            author = author,
            tags = tags,
        )
}

// ============================================================
// DSL Helpers and Extensions
// ============================================================

/**
 * Infix helper for creating keyframes: `value at time`.
 * Example: `Color.RED at 0.0` creates Keyframe(time=0.0, value=Color.RED)
 */
infix fun <T> T.at(time: Double): Keyframe<T> = Keyframe(time, this)

/**
 * Create a color gradient from keyframes.
 * Example: `colorGradient(Color.RED at 0.0, Color.BLUE at 1.0)`
 */
fun colorGradient(vararg keyframes: Keyframe<Color>): ColorRange = ColorRange.animated(Gradient(*keyframes))

/**
 * Create a value gradient from keyframes.
 * Example: `valueGradient(1.0 at 0.0, 0.0 at 1.0)` (fade out scale)
 */
fun valueGradient(vararg keyframes: Keyframe<Double>): ValueGradient = ValueGradient(*keyframes)

/**
 * Create a uniform random range.
 * Renamed from `to` to avoid conflict with Kotlin's range operator.
 * Example: `0.5 rangeTo 1.5` creates ValueRange.Uniform(0.5, 1.5)
 */
infix fun Double.rangeTo(max: Double): ValueRange<Double> = ValueRange.uniform(this, max)

/**
 * Create a uniform integer random range.
 * Example: `1 rangeTo 10` creates ValueRange.UniformInt(1, 10)
 */
infix fun Int.rangeTo(max: Int): ValueRange<Int> = ValueRange.uniformInt(this, max)

/**
 * Syntactic sugar for lifetime values.
 * Example: `lifetime = 2.sec` (clearer than just `2.0`)
 */
val Int.sec: Double
    get() = this.toDouble()

val Double.sec: Double
    get() = this

/**
 * Syntactic sugar for emission rate.
 * Example: `rate = 20.pps` (particles per second)
 */
val Int.pps: Double
    get() = this.toDouble()

val Double.pps: Double
    get() = this

/**
 * Syntactic sugar for degrees to radians conversion.
 * Example: `rotation = ValueRange.constant(90.deg)` (90 degrees per second)
 */
val Int.deg: Double
    get() = Math.toRadians(this.toDouble())

val Double.deg: Double
    get() = Math.toRadians(this)
