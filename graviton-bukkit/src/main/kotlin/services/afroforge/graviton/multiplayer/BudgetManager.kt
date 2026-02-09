@file:Suppress("MagicNumber")

package services.afroforge.graviton.multiplayer

import org.bukkit.Location
import org.bukkit.entity.Player
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

/**
 * Strategy for handling exceeded particle budgets.
 */
enum class BudgetExceededStrategy {
    /** Keep particles closest to each player */
    PRIORITIZE_NEAREST,

    /** Keep most recently spawned particles */
    PRIORITIZE_NEWEST,

    /** Switch over-budget particles to native spawnParticle() */
    FALLBACK_NATIVE,
}

/**
 * Configuration for particle budget limits.
 */
data class BudgetConfig(
    /** Maximum particles visible to any single player */
    val maxParticlesPerPlayer: Int = 200,
    /** Maximum particles server-wide */
    val maxParticlesGlobal: Int = 2000,
    /** How to handle exceeded budget */
    val exceededStrategy: BudgetExceededStrategy = BudgetExceededStrategy.PRIORITIZE_NEAREST,
)

/**
 * Tracks particle counts and enforces budgets.
 */
class BudgetManager(
    private val config: BudgetConfig = BudgetConfig(),
) {
    private val globalCount = AtomicInteger(0)
    private val perPlayerCount = ConcurrentHashMap<Player, AtomicInteger>()

    /** Current global particle count */
    val currentGlobalCount: Int get() = globalCount.get()

    /**
     * Check if we can spawn a new particle for the given viewers.
     * @return true if within budget, false if budget exceeded
     */
    fun canSpawn(viewers: Collection<Player>): Boolean {
        if (globalCount.get() >= config.maxParticlesGlobal) return false

        return viewers.all { player ->
            val count = perPlayerCount.getOrPut(player) { AtomicInteger(0) }
            count.get() < config.maxParticlesPerPlayer
        }
    }

    /**
     * Record that a particle was spawned for the given viewers.
     */
    fun recordSpawn(viewers: Collection<Player>) {
        globalCount.incrementAndGet()
        viewers.forEach { player ->
            perPlayerCount.getOrPut(player) { AtomicInteger(0) }.incrementAndGet()
        }
    }

    /**
     * Record that a particle was despawned for the given viewers.
     */
    fun recordDespawn(viewers: Collection<Player>) {
        globalCount.decrementAndGet()
        viewers.forEach { player ->
            perPlayerCount[player]?.decrementAndGet()
        }
    }

    /**
     * Get current particle count for a player.
     */
    fun getPlayerCount(player: Player): Int = perPlayerCount[player]?.get() ?: 0

    /**
     * Get remaining budget for a player.
     */
    fun getRemainingBudget(player: Player): Int = config.maxParticlesPerPlayer - getPlayerCount(player)

    /**
     * Get remaining global budget.
     */
    fun getRemainingGlobalBudget(): Int = config.maxParticlesGlobal - globalCount.get()

    /**
     * Clean up tracking for a player (call on disconnect).
     */
    fun removePlayer(player: Player) {
        perPlayerCount.remove(player)
    }

    /**
     * Reset all counts (call on plugin disable).
     */
    fun reset() {
        globalCount.set(0)
        perPlayerCount.clear()
    }

    /**
     * Get particles to cull based on strategy.
     * @param particles List of (particleId, location, viewers, spawnTime)
     * @param player The player to optimize for
     * @return List of particle IDs that should be culled
     */
    fun getParticlesToCull(
        particles: List<ParticleInfo>,
        player: Player,
    ): List<Int> {
        val playerCount = getPlayerCount(player)
        val excessCount = playerCount - config.maxParticlesPerPlayer

        if (excessCount <= 0) return emptyList()

        val playerParticles = particles.filter { player in it.viewers }

        return when (config.exceededStrategy) {
            BudgetExceededStrategy.PRIORITIZE_NEAREST -> {
                playerParticles
                    .sortedByDescending { it.location.distance(player.location) }
                    .take(excessCount)
                    .map { it.id }
            }
            BudgetExceededStrategy.PRIORITIZE_NEWEST -> {
                playerParticles
                    .sortedBy { it.spawnTime }
                    .take(excessCount)
                    .map { it.id }
            }
            BudgetExceededStrategy.FALLBACK_NATIVE -> {
                emptyList()
            }
        }
    }
}

/**
 * Info about a particle for culling decisions.
 */
data class ParticleInfo(
    val id: Int,
    val location: Location,
    val viewers: List<Player>,
    val spawnTime: Long,
)
