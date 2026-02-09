@file:Suppress("MagicNumber")

package services.afroforge.graviton.emitter

import org.bukkit.Location
import services.afroforge.graviton.api.EmitterShape
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

/**
 * Utility functions for EmitterShape to generate random spawn points.
 */
object ShapeUtils {
    /**
     * Get a random point within the given shape, offset from the center location.
     */
    fun EmitterShape.getRandomPoint(
        center: Location,
        random: Random = Random,
    ): Location =
        when (this) {
            is EmitterShape.Point -> center.clone()

            is EmitterShape.Sphere -> {
                // Random point on sphere surface
                val theta = random.nextDouble() * 2 * PI
                val phi = kotlin.math.acos(2 * random.nextDouble() - 1)
                center.clone().add(
                    radius * sin(phi) * cos(theta),
                    radius * sin(phi) * sin(theta),
                    radius * cos(phi),
                )
            }

            is EmitterShape.SphereFilled -> {
                // Random point inside sphere
                val r = radius * kotlin.math.cbrt(random.nextDouble())
                val theta = random.nextDouble() * 2 * PI
                val phi = kotlin.math.acos(2 * random.nextDouble() - 1)
                center.clone().add(
                    r * sin(phi) * cos(theta),
                    r * sin(phi) * sin(theta),
                    r * cos(phi),
                )
            }

            is EmitterShape.Circle -> {
                // Random point on circle (XZ plane by default)
                val angle = random.nextDouble() * 2 * PI
                val r = radius * sqrt(random.nextDouble())
                center.clone().add(r * cos(angle), 0.0, r * sin(angle))
            }

            is EmitterShape.Box -> {
                // Random point inside box
                center.clone().add(
                    (random.nextDouble() - 0.5) * width,
                    (random.nextDouble() - 0.5) * height,
                    (random.nextDouble() - 0.5) * depth,
                )
            }

            is EmitterShape.Line -> {
                // Random point along line
                val t = random.nextDouble()
                center.clone().add(
                    start.x + t * (end.x - start.x),
                    start.y + t * (end.y - start.y),
                    start.z + t * (end.z - start.z),
                )
            }

            is EmitterShape.Cone -> {
                // Random point in cone
                val t = random.nextDouble()
                val currentRadius = radius * t
                val currentAngle = random.nextDouble() * 2 * PI
                val r = currentRadius * sqrt(random.nextDouble())
                center.clone().add(
                    r * cos(currentAngle),
                    t * height,
                    r * sin(currentAngle),
                )
            }
        }
}
