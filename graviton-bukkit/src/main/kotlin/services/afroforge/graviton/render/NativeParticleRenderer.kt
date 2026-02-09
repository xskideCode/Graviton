@file:Suppress("MagicNumber")

package services.afroforge.graviton.render

import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.entity.Player
import services.afroforge.graviton.api.ParticleConfig
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import kotlin.random.Random

/**
 * Renderer using Minecraft's native particle system.
 * Lightweight but limited - no persistence, scale, or rotation control.
 * Best for simple effects like dust, flames, or sparkles.
 */
class NativeParticleRenderer(
    private val particleType: Particle = Particle.DUST,
    private val lodConfig: LODConfig = LODConfig(),
) : ParticleRenderer {
    private val activeParticles = ConcurrentHashMap<Int, NativeParticleState>()
    private val idCounter = AtomicInteger(0)

    override fun spawn(
        location: Location,
        config: ParticleConfig,
        viewers: Collection<Player>,
    ): Int {
        val id = idCounter.incrementAndGet()

        // Sample initial color
        val color = config.color.sample(0.0, Random)
        val scale = config.scale.evaluate(0.0).toFloat()

        // Create dust options with color
        val dustOptions =
            Particle.DustOptions(
                org.bukkit.Color.fromRGB(
                    (color.r * 255).toInt().coerceIn(0, 255),
                    (color.g * 255).toInt().coerceIn(0, 255),
                    (color.b * 255).toInt().coerceIn(0, 255),
                ),
                scale.coerceIn(0.1f, 4.0f),
            )

        // Spawn particle
        if (viewers.isEmpty()) {
            location.world?.spawnParticle(
                particleType,
                location,
                1,
                0.0,
                0.0,
                0.0,
                0.0,
                dustOptions,
            )
        } else {
            viewers.forEach { player ->
                player.spawnParticle(
                    particleType,
                    location,
                    1,
                    0.0,
                    0.0,
                    0.0,
                    0.0,
                    dustOptions,
                )
            }
        }

        // Track state
        activeParticles[id] =
            NativeParticleState(
                config = config,
                viewers = viewers.toList(),
                lastLocation = location,
            )

        return id
    }

    override fun update(
        particleId: Int,
        location: Location,
        progress: Double,
    ) {
        val state = activeParticles[particleId] ?: return

        // Sample current color and scale
        val color = state.config.color.sample(progress, Random)
        val scale = state.config.scale.evaluate(progress).toFloat()

        // Calculate LOD
        val nearestViewer = findNearestViewer(location, state.viewers)
        val lod = calculateLOD(location, nearestViewer)

        // Skip updates based on LOD
        if (lodConfig.enabled && lod < lodConfig.updateSkip.size) {
            val skipRate = lodConfig.updateSkip[lod]
            if (state.updateCount % skipRate != 0) {
                activeParticles[particleId] = state.copy(updateCount = state.updateCount + 1)
                return
            }
        }

        // Apply LOD scale
        val lodScale =
            if (lodConfig.enabled && lod < lodConfig.scaleMultiplier.size) {
                lodConfig.scaleMultiplier[lod]
            } else {
                1.0f
            }

        val dustOptions =
            Particle.DustOptions(
                org.bukkit.Color.fromRGB(
                    (color.r * 255).toInt().coerceIn(0, 255),
                    (color.g * 255).toInt().coerceIn(0, 255),
                    (color.b * 255).toInt().coerceIn(0, 255),
                ),
                (scale * lodScale).coerceIn(0.1f, 4.0f),
            )

        // Spawn new particle at updated location
        if (state.viewers.isEmpty()) {
            location.world?.spawnParticle(
                particleType,
                location,
                1,
                0.0,
                0.0,
                0.0,
                0.0,
                dustOptions,
            )
        } else {
            state.viewers.forEach { player ->
                player.spawnParticle(
                    particleType,
                    location,
                    1,
                    0.0,
                    0.0,
                    0.0,
                    0.0,
                    dustOptions,
                )
            }
        }

        // Update state
        activeParticles[particleId] =
            state.copy(
                lastLocation = location,
                updateCount = state.updateCount + 1,
            )
    }

    override fun despawn(particleId: Int) {
        // Native particles auto-despawn, just remove tracking
        activeParticles.remove(particleId)
    }

    override fun despawnAll() {
        activeParticles.clear()
    }

    override fun activeCount(): Int = activeParticles.size

    @Suppress("ReturnCount")
    override fun calculateLOD(
        location: Location,
        nearestViewer: Player?,
    ): Int {
        if (!lodConfig.enabled || nearestViewer == null) return 0

        val distance = location.distance(nearestViewer.location)

        for (i in lodConfig.thresholds.indices) {
            if (distance < lodConfig.thresholds[i]) {
                return i
            }
        }

        return ParticleRenderer.LOD_MAX
    }

    private fun findNearestViewer(
        location: Location,
        viewers: Collection<Player>,
    ): Player? {
        if (viewers.isEmpty()) {
            return location.world?.players?.minByOrNull { it.location.distance(location) }
        }
        return viewers.minByOrNull { it.location.distance(location) }
    }

    /**
     * Internal state for tracking a native particle.
     */
    private data class NativeParticleState(
        val config: ParticleConfig,
        val viewers: List<Player>,
        val lastLocation: Location,
        val updateCount: Int = 0,
    )
}
