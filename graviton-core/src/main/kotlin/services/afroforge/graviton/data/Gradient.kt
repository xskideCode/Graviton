@file:Suppress("MagicNumber", "ReturnCount")

package services.afroforge.graviton.data

import services.afroforge.graviton.math.Easing
import services.afroforge.graviton.math.Interpolation

/**
 * Represents a single keyframe in an animation timeline.
 * @param time Normalized time [0, 1] in the animation
 * @param value The value at this keyframe
 */
data class Keyframe<T>(
    val time: Double,
    val value: T,
) : Comparable<Keyframe<T>> {
    override fun compareTo(other: Keyframe<T>): Int = time.compareTo(other.time)
}

/**
 * Color represented as RGBA with Double precision.
 */
data class Color(
    val r: Double = 1.0,
    val g: Double = 1.0,
    val b: Double = 1.0,
    val a: Double = 1.0,
) {
    /**
     * Linear interpolation between colors.
     */
    fun lerp(
        other: Color,
        t: Double,
    ) = Color(
        Interpolation.lerp(r, other.r, t),
        Interpolation.lerp(g, other.g, t),
        Interpolation.lerp(b, other.b, t),
        Interpolation.lerp(a, other.a, t),
    )

    /**
     * Convert to packed ARGB integer (Minecraft format).
     */
    fun toARGB(): Int {
        val ai = (a * 255).toInt().coerceIn(0, 255)
        val ri = (r * 255).toInt().coerceIn(0, 255)
        val gi = (g * 255).toInt().coerceIn(0, 255)
        val bi = (b * 255).toInt().coerceIn(0, 255)
        return (ai shl 24) or (ri shl 16) or (gi shl 8) or bi
    }

    companion object {
        val WHITE = Color(1.0, 1.0, 1.0, 1.0)
        val BLACK = Color(0.0, 0.0, 0.0, 1.0)
        val RED = Color(1.0, 0.0, 0.0, 1.0)
        val GREEN = Color(0.0, 1.0, 0.0, 1.0)
        val BLUE = Color(0.0, 0.0, 1.0, 1.0)
        val TRANSPARENT = Color(0.0, 0.0, 0.0, 0.0)

        /**
         * Create color from hex string (e.g., "#FF5733" or "#FF5733AA").
         */
        fun fromHex(hex: String): Color {
            val cleaned = hex.removePrefix("#")
            val r = cleaned.substring(0, 2).toInt(16) / 255.0
            val g = cleaned.substring(2, 4).toInt(16) / 255.0
            val b = cleaned.substring(4, 6).toInt(16) / 255.0
            val a =
                if (cleaned.length >= 8) {
                    cleaned.substring(6, 8).toInt(16) / 255.0
                } else {
                    1.0
                }
            return Color(r, g, b, a)
        }

        /**
         * Create from packed ARGB integer.
         */
        fun fromARGB(argb: Int) =
            Color(
                r = ((argb shr 16) and 0xFF) / 255.0,
                g = ((argb shr 8) and 0xFF) / 255.0,
                b = (argb and 0xFF) / 255.0,
                a = ((argb shr 24) and 0xFF) / 255.0,
            )
    }
}

/**
 * Gradient that interpolates between color keyframes.
 */
class Gradient(keyframes: List<Keyframe<Color>>) {
    private val keyframes = keyframes.sorted()

    constructor(vararg keyframes: Keyframe<Color>) : this(keyframes.toList())

    /**
     * Evaluate the gradient at a specific time.
     * @param time Normalized time [0, 1]
     * @param easingFn Optional easing function to apply
     */
    fun evaluate(
        time: Double,
        easingFn: (Double) -> Double = Easing::linear,
    ): Color {
        if (keyframes.isEmpty()) return Color.WHITE
        if (keyframes.size == 1) return keyframes[0].value

        val t = easingFn(time.coerceIn(0.0, 1.0))

        // Find surrounding keyframes
        val nextIndex = keyframes.indexOfFirst { it.time >= t }

        return when {
            nextIndex == -1 -> keyframes.last().value // After last keyframe
            nextIndex == 0 -> keyframes.first().value // Before first keyframe
            else -> {
                val prev = keyframes[nextIndex - 1]
                val next = keyframes[nextIndex]
                val localT = (t - prev.time) / (next.time - prev.time)
                prev.value.lerp(next.value, localT)
            }
        }
    }

    companion object {
        /**
         * Create a simple two-color gradient.
         */
        fun between(
            start: Color,
            end: Color,
        ) = Gradient(
            Keyframe(0.0, start),
            Keyframe(1.0, end),
        )

        /**
         * Fade from color to transparent.
         */
        fun fadeOut(color: Color) =
            Gradient(
                Keyframe(0.0, color),
                Keyframe(1.0, color.copy(a = 0.0)),
            )
    }
}

/**
 * Gradient for scalar values (size, speed, etc.).
 */
class ValueGradient(keyframes: List<Keyframe<Double>>) {
    private val keyframes = keyframes.sorted()

    constructor(vararg keyframes: Keyframe<Double>) : this(keyframes.toList())

    fun evaluate(
        time: Double,
        easingFn: (Double) -> Double = Easing::linear,
    ): Double {
        if (keyframes.isEmpty()) return 0.0
        if (keyframes.size == 1) return keyframes[0].value

        val t = easingFn(time.coerceIn(0.0, 1.0))

        val nextIndex = keyframes.indexOfFirst { it.time >= t }

        return when {
            nextIndex == -1 -> keyframes.last().value
            nextIndex == 0 -> keyframes.first().value
            else -> {
                val prev = keyframes[nextIndex - 1]
                val next = keyframes[nextIndex]
                val localT = (t - prev.time) / (next.time - prev.time)
                Interpolation.lerp(prev.value, next.value, localT)
            }
        }
    }

    companion object {
        /**
         * Linear interpolation from start to end.
         */
        fun linear(
            start: Double,
            end: Double,
        ) = ValueGradient(
            Keyframe(0.0, start),
            Keyframe(1.0, end),
        )

        /**
         * Constant value.
         */
        fun constant(value: Double) = ValueGradient(Keyframe(0.0, value))
    }
}
