@file:Suppress("TooManyFunctions")

package services.afroforge.graviton.math

import kotlin.math.sqrt

/**
 * Immutable 3D vector.
 * Uses Double precision for world coordinates.
 */
data class Vector3(
    val x: Double,
    val y: Double,
    val z: Double,
) {
    // Vector operations
    operator fun plus(other: Vector3) = Vector3(x + other.x, y + other.y, z + other.z)

    operator fun minus(other: Vector3) = Vector3(x - other.x, y - other.y, z - other.z)

    operator fun times(scalar: Double) = Vector3(x * scalar, y * scalar, z * scalar)

    operator fun div(scalar: Double) = Vector3(x / scalar, y / scalar, z / scalar)

    operator fun unaryMinus() = Vector3(-x, -y, -z)

    fun dot(other: Vector3): Double = x * other.x + y * other.y + z * other.z

    fun cross(other: Vector3) =
        Vector3(
            y * other.z - z * other.y,
            z * other.x - x * other.z,
            x * other.y - y * other.x,
        )

    fun length(): Double = sqrt(x * x + y * y + z * z)

    fun lengthSquared(): Double = x * x + y * y + z * z

    fun normalize(): Vector3 {
        val len = length()
        return if (len > 0.0) this / len else ZERO
    }

    fun distanceTo(other: Vector3): Double = (this - other).length()

    fun distanceSquaredTo(other: Vector3): Double = (this - other).lengthSquared()

    override fun toString() = "Vector3($x, $y, $z)"

    companion object {
        val ZERO = Vector3(0.0, 0.0, 0.0)
        val ONE = Vector3(1.0, 1.0, 1.0)
        val UNIT_X = Vector3(1.0, 0.0, 0.0)
        val UNIT_Y = Vector3(0.0, 1.0, 0.0)
        val UNIT_Z = Vector3(0.0, 0.0, 1.0)
    }
}

/**
 * Mutable 3D vector for performance-critical hot paths.
 * Reuse instances to avoid allocations.
 */
data class MutableVector3(
    var x: Double = 0.0,
    var y: Double = 0.0,
    var z: Double = 0.0,
) {
    fun set(
        x: Double,
        y: Double,
        z: Double,
    ): MutableVector3 {
        this.x = x
        this.y = y
        this.z = z
        return this
    }

    fun set(other: Vector3): MutableVector3 = set(other.x, other.y, other.z)

    fun set(other: MutableVector3): MutableVector3 = set(other.x, other.y, other.z)

    fun add(other: Vector3): MutableVector3 = set(x + other.x, y + other.y, z + other.z)

    fun subtract(other: Vector3): MutableVector3 = set(x - other.x, y - other.y, z - other.z)

    fun multiply(scalar: Double): MutableVector3 = set(x * scalar, y * scalar, z * scalar)

    fun divide(scalar: Double): MutableVector3 = set(x / scalar, y / scalar, z / scalar)

    fun dot(other: Vector3): Double = x * other.x + y * other.y + z * other.z

    fun cross(other: Vector3): MutableVector3 =
        set(
            y * other.z - z * other.y,
            z * other.x - x * other.z,
            x * other.y - y * other.x,
        )

    fun length(): Double = sqrt(x * x + y * y + z * z)

    fun lengthSquared(): Double = x * x + y * y + z * z

    fun normalize(): MutableVector3 {
        val len = length()
        return if (len > 0.0) divide(len) else this
    }

    fun toImmutable() = Vector3(x, y, z)

    fun copy() = MutableVector3(x, y, z)

    // Overloads and missing methods
    fun add(other: MutableVector3): MutableVector3 = set(x + other.x, y + other.y, z + other.z)

    fun subtract(other: MutableVector3): MutableVector3 = set(x - other.x, y - other.y, z - other.z)

    fun distanceTo(other: Vector3): Double {
        val dx = x - other.x
        val dy = y - other.y
        val dz = z - other.z
        return sqrt(dx * dx + dy * dy + dz * dz)
    }

    fun distanceSquaredTo(other: Vector3): Double {
        val dx = x - other.x
        val dy = y - other.y
        val dz = z - other.z
        return dx * dx + dy * dy + dz * dz
    }

    fun distanceTo(other: MutableVector3): Double {
        val dx = x - other.x
        val dy = y - other.y
        val dz = z - other.z
        return sqrt(dx * dx + dy * dy + dz * dz)
    }

    fun distanceSquaredTo(other: MutableVector3): Double {
        val dx = x - other.x
        val dy = y - other.y
        val dz = z - other.z
        return dx * dx + dy * dy + dz * dz
    }
}
