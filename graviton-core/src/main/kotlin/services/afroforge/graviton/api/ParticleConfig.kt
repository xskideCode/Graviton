@file:Suppress("MagicNumber")

package services.afroforge.graviton.api

import services.afroforge.graviton.data.Color
import services.afroforge.graviton.data.ColorRange
import services.afroforge.graviton.data.ValueGradient
import services.afroforge.graviton.data.ValueRange
import services.afroforge.graviton.math.Vector3

/**
 * Configuration for individual particle behavior.
 */
data class ParticleConfig(
    /** Particle lifetime in seconds */
    val lifetime: ValueRange<Double> = ValueRange.constant(Constants.DEFAULT_PARTICLE_LIFETIME),
    /** Particle color over lifetime */
    val color: ColorRange = ColorRange.constant(Color.WHITE),
    /** Scale over lifetime (default: constant 1.0) */
    val scale: ValueGradient = ValueGradient.constant(1.0),
    /** Initial velocity */
    val velocity: ValueRange<Vector3> = ValueRange.constant(Vector3.ZERO),
    /** Gravity acceleration (default: Earth gravity) */
    val gravity: Vector3 = EARTH_GRAVITY,
    /** Air resistance coefficient [0, 1] */
    val drag: Double = Constants.DEFAULT_PARTICLE_DRAG,
    /** Rotation over lifetime (radians/second) */
    val rotation: ValueRange<Double> = ValueRange.constant(0.0),
    /** Billboard mode */
    val billboard: BillboardMode = BillboardMode.CENTER,
    /** Rendering mode (ITEM, TEXT, BLOCK) */
    val renderMode: RenderMode = RenderMode.TEXT,
    /** Texture string (Character for TextDisplay, Material name for Item/Block) */
    val texture: String? = null,
    /** Radial velocity (speed away from center) */
    val radialVelocity: Double = Constants.DEFAULT_PARTICLE_RADIAL_VELOCITY,
) {
    init {
        require(drag in 0.0..1.0) { "Drag must be between 0 and 1" }
    }

    companion object {
        /** Standard Earth gravity vector */
        val EARTH_GRAVITY = Vector3(0.0, -9.8, 0.0)

        /** No gravity (for space/floating effects) */
        val NO_GRAVITY = Vector3.ZERO
    }
}

/**
 * Billboard rendering mode.
 */
enum class BillboardMode {
    /** Face camera center */
    CENTER,

    /** Face camera with fixed vertical axis */
    VERTICAL,

    /** Face camera with fixed horizontal axis */
    HORIZONTAL,

    /** No billboarding */
    FIXED,
}

/**
 * Rendering mode for the particle.
 */
enum class RenderMode {
    /** Item Display (3D items/blocks) */
    ITEM,

    /** Block Display (3D blocks) */
    BLOCK,

    /** Text Display (2D text/images via resource pack) */
    TEXT,
}
