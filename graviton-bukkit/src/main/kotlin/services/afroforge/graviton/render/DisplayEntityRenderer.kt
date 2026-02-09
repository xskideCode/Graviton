@file:Suppress("MagicNumber")

package services.afroforge.graviton.render

import org.bukkit.Location
import org.bukkit.entity.Display
import org.bukkit.entity.Player
import org.bukkit.util.Transformation
import org.joml.Quaternionf
import org.joml.Vector3f
import services.afroforge.graviton.api.BillboardMode
import services.afroforge.graviton.api.ParticleConfig
import services.afroforge.graviton.data.Color
import services.afroforge.graviton.platform.DisplayConfig
import services.afroforge.graviton.platform.PacketAdapter
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import kotlin.random.Random

/**
 * Renderer using Display Entities for high-quality particles.
 * Display entities support custom textures, colors, and smooth interpolation.
 */
class DisplayEntityRenderer(
    private val packetAdapter: PacketAdapter,
    private val lodConfig: LODConfig = LODConfig(),
) : ParticleRenderer {
    private val activeParticles = ConcurrentHashMap<Int, ParticleState>()
    private val idCounter = AtomicInteger(0)

    override fun spawn(
        location: Location,
        config: ParticleConfig,
        viewers: Collection<Player>,
    ): Int {
        val id = idCounter.incrementAndGet()

        // Sample initial color
        val initialColor = config.color.sample(0.0, Random)

        // Create display config (2-tick interpolation, full brightness)
        val displayConfig =
            DisplayConfig(
                billboard = toBukkitBillboard(config.billboard),
                viewRange = 1.0f,
                interpolationDuration = 2,
                brightness = Display.Brightness(15, 15),
            )

        // Spawn as text display with block character for solid appearance
        val entityId =
            packetAdapter.spawnTextDisplay(
                location = location,
                viewers = viewers,
                text = "█",
                config = displayConfig,
            )

        // Track particle state
        activeParticles[id] =
            ParticleState(
                entityId = entityId,
                config = config,
                spawnTime = System.currentTimeMillis(),
                lastUpdate = System.currentTimeMillis(),
                viewers = viewers.toList(),
                currentColor = initialColor,
            )

        return id
    }

    override fun update(
        particleId: Int,
        location: Location,
        progress: Double,
    ) {
        val state = activeParticles[particleId] ?: return

        // Sample color at current progress
        val color = state.config.color.sample(progress, Random)

        // Sample scale at current progress
        val scale = state.config.scale.evaluate(progress).toFloat()

        // Calculate LOD
        val nearestViewer = findNearestViewer(location, state.viewers)
        val lod = calculateLOD(location, nearestViewer)

        // Apply LOD scale reduction
        val lodScale =
            if (lodConfig.enabled && lod < lodConfig.scaleMultiplier.size) {
                lodConfig.scaleMultiplier[lod]
            } else {
                1.0f
            }

        val finalScale = scale * lodScale

        // Create transformation with new scale
        val transformation =
            Transformation(
                Vector3f(0f, 0f, 0f),
                Quaternionf(),
                Vector3f(finalScale, finalScale, finalScale),
                Quaternionf(),
            )

        packetAdapter.updateTransformation(
            entityId = state.entityId,
            viewers = state.viewers,
            transformation = transformation,
        )

        // Update state
        activeParticles[particleId] =
            state.copy(
                lastUpdate = System.currentTimeMillis(),
                currentColor = color,
            )
    }

    override fun despawn(particleId: Int) {
        val state = activeParticles.remove(particleId) ?: return
        packetAdapter.despawnAll(state.entityId)
    }

    override fun despawnAll() {
        activeParticles.values.forEach { state ->
            packetAdapter.despawnAll(state.entityId)
        }
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
            // Fall back to nearest player in world
            return location.world?.players?.minByOrNull { it.location.distance(location) }
        }
        return viewers.minByOrNull { it.location.distance(location) }
    }

    private fun toBukkitBillboard(mode: BillboardMode): Display.Billboard =
        when (mode) {
            BillboardMode.CENTER -> Display.Billboard.CENTER
            BillboardMode.VERTICAL -> Display.Billboard.VERTICAL
            BillboardMode.HORIZONTAL -> Display.Billboard.HORIZONTAL
            BillboardMode.FIXED -> Display.Billboard.FIXED
        }

    /**
     * Internal state for tracking a spawned particle.
     */
    private data class ParticleState(
        val entityId: Int,
        val config: ParticleConfig,
        val spawnTime: Long,
        val lastUpdate: Long,
        val viewers: List<Player>,
        val currentColor: Color,
    )
}
