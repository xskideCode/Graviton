@file:Suppress("WildcardImport", "MagicNumber")

package services.afroforge.graviton.math

import kotlin.math.*

/**
 * Quaternion for 3D rotation (immutable).
 * Stored as (w, x, y, z) where w is the scalar part.
 */
data class Quaternion(
    val w: Double = 1.0,
    val x: Double = 0.0,
    val y: Double = 0.0,
    val z: Double = 0.0,
) {
    operator fun times(other: Quaternion) =
        Quaternion(
            w * other.w - x * other.x - y * other.y - z * other.z,
            w * other.x + x * other.w + y * other.z - z * other.y,
            w * other.y - x * other.z + y * other.w + z * other.x,
            w * other.z + x * other.y - y * other.x + z * other.w,
        )

    fun conjugate() = Quaternion(w, -x, -y, -z)

    fun length() = sqrt(w * w + x * x + y * y + z * z)

    fun normalize(): Quaternion {
        val len = length()
        return if (len > 0.0) Quaternion(w / len, x / len, y / len, z / len) else IDENTITY
    }

    fun rotate(v: Vector3): Vector3 {
        val qv = Quaternion(0.0, v.x, v.y, v.z)
        val result = this * qv * conjugate()
        return Vector3(result.x, result.y, result.z)
    }

    fun toEulerAngles(): Triple<Double, Double, Double> {
        // Pitch (x-axis rotation)
        val sinPitch = 2.0 * (w * x + y * z)
        val cosPitch = 1.0 - 2.0 * (x * x + y * y)
        val pitch = atan2(sinPitch, cosPitch)

        // Yaw (y-axis rotation)
        val sinYaw = 2.0 * (w * y - z * x)
        val yaw =
            if (abs(sinYaw) >= 1.0) {
                (Math.PI / 2.0) * sign(sinYaw) // Use 90 degrees if out of range
            } else {
                asin(sinYaw)
            }

        // Roll (z-axis rotation)
        val sinRoll = 2.0 * (w * z + x * y)
        val cosRoll = 1.0 - 2.0 * (y * y + z * z)
        val roll = atan2(sinRoll, cosRoll)

        return Triple(pitch, yaw, roll)
    }

    companion object {
        val IDENTITY = Quaternion(1.0, 0.0, 0.0, 0.0)

        fun fromAxisAngle(
            axis: Vector3,
            angle: Double,
        ): Quaternion {
            val halfAngle = angle / 2.0
            val s = sin(halfAngle)
            val normalized = axis.normalize()
            return Quaternion(
                cos(halfAngle),
                normalized.x * s,
                normalized.y * s,
                normalized.z * s,
            )
        }

        fun fromEulerAngles(
            pitch: Double,
            yaw: Double,
            roll: Double,
        ): Quaternion {
            val cy = cos(yaw * 0.5)
            val sy = sin(yaw * 0.5)
            val cp = cos(pitch * 0.5)
            val sp = sin(pitch * 0.5)
            val cr = cos(roll * 0.5)
            val sr = sin(roll * 0.5)

            return Quaternion(
                cr * cp * cy + sr * sp * sy,
                sr * cp * cy - cr * sp * sy,
                cr * sp * cy + sr * cp * sy,
                cr * cp * sy - sr * sp * cy,
            )
        }

        // Spherical linear interpolation
        fun slerp(
            start: Quaternion,
            end: Quaternion,
            t: Double,
        ): Quaternion {
            var dot = start.w * end.w + start.x * end.x + start.y * end.y + start.z * end.z

            var endCorrected = end
            if (dot < 0.0) {
                endCorrected = Quaternion(-end.w, -end.x, -end.y, -end.z)
                dot = -dot
            }

            return if (dot > 0.9995) {
                // Linear interpolation for very close quaternions
                Quaternion(
                    start.w + t * (endCorrected.w - start.w),
                    start.x + t * (endCorrected.x - start.x),
                    start.y + t * (endCorrected.y - start.y),
                    start.z + t * (endCorrected.z - start.z),
                ).normalize()
            } else {
                val theta = acos(dot)
                val sinTheta = sin(theta)
                val wa = sin((1.0 - t) * theta) / sinTheta
                val wb = sin(t * theta) / sinTheta

                Quaternion(
                    start.w * wa + endCorrected.w * wb,
                    start.x * wa + endCorrected.x * wb,
                    start.y * wa + endCorrected.y * wb,
                    start.z * wa + endCorrected.z * wb,
                )
            }
        }
    }
}
