@file:Suppress("MagicNumber")

package services.afroforge.graviton.api

import services.afroforge.graviton.math.Vector3

/**
 * Configuration for particle emitter behavior.
 */
data class EmitterConfig(
    /** Emitter type */
    val type: EmitterType = EmitterType.CONTINUOUS,
    /** Emission rate (particles per second for CONTINUOUS) */
    val rate: Double = 10.0,
    /** Burst count (for BURST type) */
    val burstCount: Int = 100,
    /** Shape from which particles spawn */
    val shape: EmitterShape = EmitterShape.Point,
    /** Maximum active particles */
    val maxParticles: Int = 1000,
    /** Tick rate (calculations per second) */
    val tickRate: Int = 20,
    /** Duration in seconds (0 = infinite) */
    val duration: Double = 0.0,
    /** Movement threshold for TRAIL type (in blocks) */
    val movementThreshold: Double = 0.1,
) {
    init {
        require(rate > 0.0) { "Rate must be positive" }
        require(burstCount > 0) { "Burst count must be positive" }
        require(maxParticles > 0) { "Max particles must be positive" }
        require(tickRate > 0) { "Tick rate must be positive" }
        require(duration >= 0.0) { "Duration must be non-negative" }
        require(movementThreshold > 0.0) { "Movement threshold must be positive" }
    }
}

/**
 * Emitter type.
 */
enum class EmitterType {
    /** Continuous emission over time */
    CONTINUOUS,

    /** Single burst */
    BURST,

    /** Emit on movement */
    TRAIL,
}

/**
 * Shape from which particles emit.
 * All shapes validate their dimensions at construction time.
 */
sealed class EmitterShape {
    /** Single point */
    data object Point : EmitterShape()

    /** Sphere surface */
    data class Sphere(val radius: Double) : EmitterShape() {
        init {
            require(radius > 0.0) { "Sphere radius must be positive" }
        }
    }

    /** Sphere volume */
    data class SphereFilled(val radius: Double) : EmitterShape() {
        init {
            require(radius > 0.0) { "Sphere radius must be positive" }
        }
    }

    /** Circle on a plane */
    data class Circle(
        val radius: Double,
        val normal: Vector3 = Vector3.UNIT_Y,
    ) : EmitterShape() {
        init {
            require(radius > 0.0) { "Circle radius must be positive" }
        }
    }

    /** Box volume */
    data class Box(
        val width: Double,
        val height: Double,
        val depth: Double,
    ) : EmitterShape() {
        init {
            require(width > 0.0) { "Box width must be positive" }
            require(height > 0.0) { "Box height must be positive" }
            require(depth > 0.0) { "Box depth must be positive" }
        }
    }

    /** Line between two points */
    data class Line(
        val start: Vector3 = Vector3.ZERO,
        val end: Vector3 = Vector3.UNIT_Y,
    ) : EmitterShape() {
        init {
            require(start != end) { "Line start and end must be different" }
        }
    }

    /** Cone emission (useful for fire, jets) */
    data class Cone(
        val radius: Double,
        val height: Double,
        val angle: Double = 45.0,
    ) : EmitterShape() {
        init {
            require(radius > 0.0) { "Cone radius must be positive" }
            require(height > 0.0) { "Cone height must be positive" }
            require(angle in 0.0..90.0) { "Cone angle must be between 0 and 90 degrees" }
        }
    }
}
