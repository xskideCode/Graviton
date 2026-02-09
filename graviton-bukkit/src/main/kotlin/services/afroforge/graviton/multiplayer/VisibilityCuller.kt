@file:Suppress("MagicNumber")

package services.afroforge.graviton.multiplayer

import org.bukkit.Location
import org.bukkit.entity.Player

/**
 * Distance-based visibility level for particle rendering.
 */
enum class VisibilityLevel(
    val densityMultiplier: Double,
    val updateSkip: Int,
) {
    /** Full quality: 0-16 blocks */
    FULL(1.0, 1),

    /** Reduced quality: 16-32 blocks */
    REDUCED(0.5, 2),

    /** Minimal quality: 32-64 blocks */
    MINIMAL(0.25, 4),

    /** Hidden: 64+ blocks (no packets sent) */
    HIDDEN(0.0, Int.MAX_VALUE),
}

/**
 * Per-player visibility culling based on distance.
 */
class VisibilityCuller(
    private val config: CullingConfig = CullingConfig(),
) {
    /**
     * Calculate visibility level for a player viewing a particle location.
     */
    fun getVisibilityLevel(
        player: Player,
        particleLocation: Location,
    ): VisibilityLevel {
        if (player.world != particleLocation.world) return VisibilityLevel.HIDDEN

        val distance = player.location.distance(particleLocation)

        return when {
            distance < config.fullRangeBlocks -> VisibilityLevel.FULL
            distance < config.reducedRangeBlocks -> VisibilityLevel.REDUCED
            distance < config.minimalRangeBlocks -> VisibilityLevel.MINIMAL
            else -> VisibilityLevel.HIDDEN
        }
    }

    /**
     * Filter viewers by visibility, returning only those who should see the particle.
     */
    fun filterViewers(
        particleLocation: Location,
        potentialViewers: Collection<Player>,
    ): Map<Player, VisibilityLevel> =
        potentialViewers
            .map { it to getVisibilityLevel(it, particleLocation) }
            .filter { it.second != VisibilityLevel.HIDDEN }
            .toMap()

    /**
     * Check if a particle should be spawned for this player based on density.
     */
    fun shouldSpawnForPlayer(
        player: Player,
        particleLocation: Location,
        randomValue: Double,
    ): Boolean {
        val level = getVisibilityLevel(player, particleLocation)
        return randomValue < level.densityMultiplier
    }
}

/**
 * Configuration for visibility culling distances.
 */
data class CullingConfig(
    /** Distance in blocks for full quality (100% density) */
    val fullRangeBlocks: Int = 16,
    /** Distance in blocks for reduced quality (50% density) */
    val reducedRangeBlocks: Int = 32,
    /** Distance in blocks for minimal quality (25% density) */
    val minimalRangeBlocks: Int = 64,
)
