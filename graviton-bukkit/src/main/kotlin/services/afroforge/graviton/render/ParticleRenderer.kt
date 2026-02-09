@file:Suppress("MagicNumber")

package services.afroforge.graviton.render

import org.bukkit.Location
import org.bukkit.entity.Player
import services.afroforge.graviton.api.ParticleConfig

/**
 * Interface for rendering particles to players.
 * Implementations handle different rendering strategies (display entities, native particles, etc.)
 */
interface ParticleRenderer {
    /**
     * Spawn a particle at the given location.
     * @param location World location for the particle
     * @param config Particle configuration (color, scale, etc.)
     * @param viewers Players who should see this particle (empty = all nearby)
     * @return Particle ID for tracking, or -1 if spawn failed
     */
    fun spawn(
        location: Location,
        config: ParticleConfig,
        viewers: Collection<Player> = emptyList(),
    ): Int

    /**
     * Update an existing particle's position and properties.
     * @param particleId ID returned from spawn()
     * @param location New location
     * @param progress Normalized lifetime progress [0, 1] for gradient sampling
     */
    fun update(
        particleId: Int,
        location: Location,
        progress: Double,
    )

    /**
     * Despawn a particle.
     * @param particleId ID returned from spawn()
     */
    fun despawn(particleId: Int)

    /**
     * Despawn all particles managed by this renderer.
     */
    fun despawnAll()

    /**
     * Get the current number of active particles.
     */
    fun activeCount(): Int

    /**
     * Check if LOD (Level of Detail) should reduce quality for a location.
     * @param location Particle location
     * @param nearestViewer Closest player to this location
     * @return LOD level (0 = full quality, higher = reduced quality)
     */
    fun calculateLOD(
        location: Location,
        nearestViewer: Player?,
    ): Int

    companion object {
        /** Maximum LOD level (lowest quality) */
        const val LOD_MAX = 3

        /** Distance thresholds for LOD levels (in blocks) */
        val LOD_THRESHOLDS = intArrayOf(16, 32, 48, 64)
    }
}

/**
 * LOD configuration for distance-based quality reduction.
 */
data class LODConfig(
    /** Enable LOD system */
    val enabled: Boolean = true,
    /** Distance thresholds for each LOD level (in blocks) */
    val thresholds: IntArray = intArrayOf(16, 32, 48, 64),
    /** Skip update frequency at each LOD level (1 = every tick, 2 = every other tick, etc.) */
    val updateSkip: IntArray = intArrayOf(1, 2, 4, 8),
    /** Scale multiplier at each LOD level */
    val scaleMultiplier: FloatArray = floatArrayOf(1.0f, 0.8f, 0.6f, 0.4f),
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is LODConfig) return false
        return enabled == other.enabled &&
            thresholds.contentEquals(other.thresholds) &&
            updateSkip.contentEquals(other.updateSkip) &&
            scaleMultiplier.contentEquals(other.scaleMultiplier)
    }

    override fun hashCode(): Int {
        var result = enabled.hashCode()
        result = 31 * result + thresholds.contentHashCode()
        result = 31 * result + updateSkip.contentHashCode()
        result = 31 * result + scaleMultiplier.contentHashCode()
        return result
    }
}
