package services.afroforge.graviton.math

/**
 * Transform represents position, rotation, and scale in 3D space.
 * Immutable by default for thread safety.
 */
data class Transform(
    val position: Vector3 = Vector3.ZERO,
    val rotation: Quaternion = Quaternion.IDENTITY,
    val scale: Vector3 = Vector3.ONE,
) {
    fun translate(offset: Vector3) = copy(position = position + offset)

    fun rotate(quat: Quaternion) = copy(rotation = rotation * quat)

    fun scaleBy(factor: Double) = copy(scale = scale * factor)

    fun scaleBy(factor: Vector3) = copy(scale = Vector3(scale.x * factor.x, scale.y * factor.y, scale.z * factor.z))

    /**
     * Transform a point from local space to world space.
     */
    fun transformPoint(localPoint: Vector3): Vector3 {
        val scaled = Vector3(localPoint.x * scale.x, localPoint.y * scale.y, localPoint.z * scale.z)
        val rotated = rotation.rotate(scaled)
        return position + rotated
    }

    /**
     * Transform a direction vector (ignores position).
     */
    fun transformDirection(localDirection: Vector3): Vector3 {
        return rotation.rotate(localDirection)
    }

    /**
     * Interpolate between two transforms.
     */
    fun lerp(
        other: Transform,
        t: Double,
    ) = Transform(
        position =
            Vector3(
                position.x + (other.position.x - position.x) * t,
                position.y + (other.position.y - position.y) * t,
                position.z + (other.position.z - position.z) * t,
            ),
        rotation = Quaternion.slerp(rotation, other.rotation, t),
        scale =
            Vector3(
                scale.x + (other.scale.x - scale.x) * t,
                scale.y + (other.scale.y - scale.y) * t,
                scale.z + (other.scale.z - scale.z) * t,
            ),
    )

    companion object {
        val IDENTITY = Transform()
    }
}
