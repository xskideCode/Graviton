@file:Suppress("WildcardImport", "MagicNumber", "TooManyFunctions")

package services.afroforge.graviton.math

import kotlin.math.*

/**
 * Interpolation utilities for smooth animations.
 */
object Interpolation {
    /**
     * Linear interpolation.
     */
    fun lerp(
        start: Double,
        end: Double,
        t: Double,
    ): Double = start + (end - start) * t

    /**
     * Cubic Bezier curve interpolation.
     * @param p0 Start point
     * @param p1 First control point
     * @param p2 Second control point
     * @param p3 End point
     * @param t Time parameter [0, 1]
     */
    fun bezier(
        p0: Double,
        p1: Double,
        p2: Double,
        p3: Double,
        t: Double,
    ): Double {
        val oneMinusT = 1.0 - t
        val oneMinusT2 = oneMinusT * oneMinusT
        val oneMinusT3 = oneMinusT2 * oneMinusT
        val t2 = t * t
        val t3 = t2 * t

        return oneMinusT3 * p0 +
            3.0 * oneMinusT2 * t * p1 +
            3.0 * oneMinusT * t2 * p2 +
            t3 * p3
    }

    /**
     * Cubic Bezier for Vector3.
     */
    fun bezier(
        p0: Vector3,
        p1: Vector3,
        p2: Vector3,
        p3: Vector3,
        t: Double,
    ) = Vector3(
        bezier(p0.x, p1.x, p2.x, p3.x, t),
        bezier(p0.y, p1.y, p2.y, p3.y, t),
        bezier(p0.z, p1.z, p2.z, p3.z, t),
    )

    /**
     * Hermite spline interpolation.
     */
    fun hermite(
        start: Double,
        end: Double,
        tangent1: Double,
        tangent2: Double,
        t: Double,
    ): Double {
        val t2 = t * t
        val t3 = t2 * t

        val h1 = 2.0 * t3 - 3.0 * t2 + 1.0
        val h2 = -2.0 * t3 + 3.0 * t2
        val h3 = t3 - 2.0 * t2 + t
        val h4 = t3 - t2

        return h1 * start + h2 * end + h3 * tangent1 + h4 * tangent2
    }

    /**
     * Catmull-Rom spline (smooth curve through points).
     */
    fun catmullRom(
        p0: Double,
        p1: Double,
        p2: Double,
        p3: Double,
        t: Double,
    ): Double {
        val t2 = t * t
        val t3 = t2 * t

        return 0.5 * (
            2.0 * p1 +
                (-p0 + p2) * t +
                (2.0 * p0 - 5.0 * p1 + 4.0 * p2 - p3) * t2 +
                (-p0 + 3.0 * p1 - 3.0 * p2 + p3) * t3
        )
    }

    /**
     * Catmull-Rom for Vector3.
     */
    fun catmullRom(
        p0: Vector3,
        p1: Vector3,
        p2: Vector3,
        p3: Vector3,
        t: Double,
    ) = Vector3(
        catmullRom(p0.x, p1.x, p2.x, p3.x, t),
        catmullRom(p0.y, p1.y, p2.y, p3.y, t),
        catmullRom(p0.z, p1.z, p2.z, p3.z, t),
    )
}

/**
 * Easing functions for animation timing.
 * All functions map [0, 1] -> [0, 1] with different curves.
 */
object Easing {
    // Linear
    fun linear(t: Double) = t

    // Quadratic
    fun easeInQuad(t: Double) = t * t

    fun easeOutQuad(t: Double) = t * (2.0 - t)

    fun easeInOutQuad(t: Double) = if (t < 0.5) 2.0 * t * t else -1.0 + (4.0 - 2.0 * t) * t

    // Cubic
    fun easeInCubic(t: Double) = t * t * t

    fun easeOutCubic(t: Double) = (t - 1.0).let { it * it * it + 1.0 }

    fun easeInOutCubic(t: Double) = if (t < 0.5) 4.0 * t * t * t else (t - 1.0).let { 1.0 + it * (2.0 * it).pow(2.0) }

    // Quartic
    fun easeInQuart(t: Double) = t * t * t * t

    fun easeOutQuart(t: Double) = (t - 1.0).let { 1.0 - it * it * it * it }

    fun easeInOutQuart(t: Double) = if (t < 0.5) 8.0 * t.pow(4.0) else (t - 1.0).let { 1.0 - 8.0 * it.pow(4.0) }

    // Quintic
    fun easeInQuint(t: Double) = t.pow(5.0)

    fun easeOutQuint(t: Double) = (t - 1.0).let { 1.0 + it.pow(5.0) }

    fun easeInOutQuint(t: Double) = if (t < 0.5) 16.0 * t.pow(5.0) else (t - 1.0).let { 1.0 + 16.0 * it.pow(5.0) }

    // Sine
    fun easeInSine(t: Double) = 1.0 - cos(t * PI / 2.0)

    fun easeOutSine(t: Double) = sin(t * PI / 2.0)

    fun easeInOutSine(t: Double) = -(cos(PI * t) - 1.0) / 2.0

    // Exponential
    fun easeInExpo(t: Double) = if (t == 0.0) 0.0 else 2.0.pow(10.0 * (t - 1.0))

    fun easeOutExpo(t: Double) = if (t == 1.0) 1.0 else 1.0 - 2.0.pow(-10.0 * t)

    fun easeInOutExpo(t: Double) =
        when {
            t == 0.0 -> 0.0
            t == 1.0 -> 1.0
            t < 0.5 -> 2.0.pow(20.0 * t - 10.0) / 2.0
            else -> (2.0 - 2.0.pow(-20.0 * t + 10.0)) / 2.0
        }

    // Circular
    fun easeInCirc(t: Double) = 1.0 - sqrt(1.0 - t * t)

    fun easeOutCirc(t: Double) = sqrt(1.0 - (t - 1.0) * (t - 1.0))

    fun easeInOutCirc(t: Double) =
        if (t < 0.5) {
            (1.0 - sqrt(1.0 - (2.0 * t).pow(2.0))) / 2.0
        } else {
            (sqrt(1.0 - (-2.0 * t + 2.0).pow(2.0)) + 1.0) / 2.0
        }

    // Elastic
    fun easeInElastic(t: Double): Double {
        val c4 = (2.0 * PI) / 3.0
        return when {
            t == 0.0 -> 0.0
            t == 1.0 -> 1.0
            else -> -2.0.pow(10.0 * t - 10.0) * sin((t * 10.0 - 10.75) * c4)
        }
    }

    fun easeOutElastic(t: Double): Double {
        val c4 = (2.0 * PI) / 3.0
        return when {
            t == 0.0 -> 0.0
            t == 1.0 -> 1.0
            else -> 2.0.pow(-10.0 * t) * sin((t * 10.0 - 0.75) * c4) + 1.0
        }
    }

    // Bounce
    fun easeOutBounce(t: Double): Double {
        val n1 = 7.5625
        val d1 = 2.75

        return when {
            t < 1.0 / d1 -> n1 * t * t
            t < 2.0 / d1 -> n1 * (t - 1.5 / d1) * (t - 1.5 / d1) + 0.75
            t < 2.5 / d1 -> n1 * (t - 2.25 / d1) * (t - 2.25 / d1) + 0.9375
            else -> n1 * (t - 2.625 / d1) * (t - 2.625 / d1) + 0.984375
        }
    }

    fun easeInBounce(t: Double) = 1.0 - easeOutBounce(1.0 - t)
}
