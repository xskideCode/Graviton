@file:Suppress("MagicNumber")

package services.afroforge.graviton.emitter

import org.bukkit.Location
import org.bukkit.entity.Entity
import org.bukkit.plugin.Plugin
import services.afroforge.graviton.api.EmitterConfig
import services.afroforge.graviton.api.ParticleConfig
import services.afroforge.graviton.platform.ScheduledTask
import services.afroforge.graviton.platform.Scheduler
import services.afroforge.graviton.render.ParticleRenderer
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * Manages all active emitters and their tick loop.
 */
class EmitterManager(
    private val plugin: Plugin,
    private val renderer: ParticleRenderer,
    private val ticksPerSecond: Int = 20,
    private val scheduler: Scheduler = Scheduler.create(plugin),
) {
    private val emitters = ConcurrentHashMap<UUID, Emitter>()
    private var tickTask: ScheduledTask? = null
    private var lastTickTime = System.currentTimeMillis()

    /** Number of active emitters */
    val emitterCount: Int get() = emitters.size

    /** Total number of active particles across all emitters */
    val totalParticleCount: Int get() = emitters.values.sumOf { it.particleCount }

    /**
     * Start the emitter tick loop.
     */
    fun start() {
        if (tickTask != null) return

        lastTickTime = System.currentTimeMillis()
        val tickInterval = (20 / ticksPerSecond).coerceAtLeast(1).toLong()

        tickTask =
            scheduler.runTaskTimer(
                location =
                    checkNotNull(plugin.server.worlds.firstOrNull()?.spawnLocation) {
                        "No worlds loaded"
                    },
                delayTicks = 0L,
                periodTicks = tickInterval,
            ) {
                tick()
            }
    }

    /**
     * Stop the tick loop and all emitters.
     */
    fun stop() {
        tickTask?.cancel()
        tickTask = null
        emitters.values.forEach { it.stop() }
        emitters.clear()
        ParticlePool.clear()
    }

    /**
     * Create an emitter at a fixed location.
     */
    fun createEmitter(
        location: Location,
        emitterConfig: EmitterConfig,
        particleConfig: ParticleConfig,
    ): UUID {
        val emitter =
            LocationEmitter(
                location = location,
                emitterConfig = emitterConfig,
                particleConfig = particleConfig,
                renderer = renderer,
            )
        emitters[emitter.id] = emitter
        return emitter.id
    }

    /**
     * Create an emitter attached to an entity.
     */
    fun createEmitter(
        entity: Entity,
        emitterConfig: EmitterConfig,
        particleConfig: ParticleConfig,
    ): UUID {
        val emitter =
            EntityEmitter(
                entity = entity,
                emitterConfig = emitterConfig,
                particleConfig = particleConfig,
                renderer = renderer,
            )
        emitters[emitter.id] = emitter
        return emitter.id
    }

    /**
     * Stop and remove an emitter.
     */
    fun removeEmitter(id: UUID): Boolean {
        val emitter = emitters.remove(id) ?: return false
        emitter.stop()
        return true
    }

    /**
     * Get an emitter by ID.
     */
    fun getEmitter(id: UUID): Emitter? = emitters[id]

    /**
     * Check if an emitter exists.
     */
    fun hasEmitter(id: UUID): Boolean = emitters.containsKey(id)

    /**
     * Tick all active emitters.
     */
    @Suppress("TooGenericExceptionCaught")
    private fun tick() {
        val now = System.currentTimeMillis()
        val deltaMs = now - lastTickTime
        lastTickTime = now

        // Remove inactive emitters
        emitters.entries.removeIf { (_, emitter) ->
            if (!emitter.isActive) {
                emitter.stop()
                true
            } else {
                false
            }
        }

        // Tick all active emitters
        emitters.values.forEach { emitter ->
            try {
                emitter.tick(deltaMs)
            } catch (e: Exception) {
                plugin.logger.warning("Error ticking emitter ${emitter.id}: ${e.message}")
            }
        }
    }
}
