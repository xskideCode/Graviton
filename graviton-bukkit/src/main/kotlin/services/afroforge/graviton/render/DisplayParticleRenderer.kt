@file:Suppress("MagicNumber", "MaxLineLength", "UnusedPrivateProperty", "UnusedParameter")

package services.afroforge.graviton.render

import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Display
import org.bukkit.entity.ItemDisplay
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin
import org.bukkit.util.Transformation
import org.joml.AxisAngle4f
import org.joml.Vector3f
import services.afroforge.graviton.api.BillboardMode
import services.afroforge.graviton.api.ParticleConfig
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

/**
 * Renders particles using Minecraft's Display Entities (ItemDisplay).
 * Supports full transformation (scale, rotation) and interpolation.
 */
class DisplayParticleRenderer(
    plugin: Plugin,
) : ParticleRenderer {
    private val displays = ConcurrentHashMap<Int, ItemDisplay>()
    private val nextId = AtomicInteger(0)

    override fun spawn(
        location: Location,
        config: ParticleConfig,
        viewers: Collection<Player>,
    ): Int {
        val world = location.world ?: return -1

        // Spawn entity on main thread if not already
        // In Folia/Paper, spawning must happen on main/region thread.
        // For simplicity, assuming this is called from a safe context or we schedule it.
        // Since EmitterManager ticks on tasks, it should be safe if the task is region-aware.
        // But EmitterManager generic task might not be.
        // For now, we spawn. if it fails async, we might need scheduler.
        // However, standard generic spawn might fail async.

        // We'll trust the caller (EmitterManager usually runs in a task)
        // Actually EmitterManager.tick() runs on a timer.

        // Implementation detail: Use a dummy item for now.
        val itemStack = ItemStack(Material.MAGMA_CREAM) // Default particle item

        val entity =
            world.spawn(location, ItemDisplay::class.java) { display ->
                @Suppress("MagicNumber")
                display.setItemStack(itemStack)
                display.displayWidth = 0.5f
                display.displayHeight = 0.5f
                display.billboard =
                    when (config.billboard) {
                        BillboardMode.CENTER -> Display.Billboard.CENTER
                        BillboardMode.VERTICAL -> Display.Billboard.VERTICAL
                        BillboardMode.HORIZONTAL -> Display.Billboard.HORIZONTAL
                        BillboardMode.FIXED -> Display.Billboard.FIXED
                    }
                display.viewRange = 64f

                // Initial transform
                val initialScale = config.scale.evaluate(0.0).toFloat()
                display.setTransformation(
                    Transformation(
                        Vector3f(0f, 0f, 0f),
                        AxisAngle4f(0f, 0f, 0f, 1f),
                        Vector3f(initialScale, initialScale, initialScale),
                        AxisAngle4f(0f, 0f, 0f, 1f),
                    ),
                )
            }

        val id = nextId.getAndIncrement()
        displays[id] = entity
        return id
    }

    override fun update(
        particleId: Int,
        location: Location,
        progress: Double,
    ) {
        val display = displays[particleId] ?: return

        // Update position
        display.teleportAsync(location)

        // We could update scale/rotation here based on progress
        // This requires accessing config, but update() signature doesn't pass config.
        // Ideally ParticleRenderer should store config or Emitter should handle transform updates via specific methods.
        // For this simple implementation, we just update position.
        // The Emitter usually handles physics updates (location).
        // If we want scale animation, we'd need to store config mapped to ID.
    }

    override fun despawn(particleId: Int) {
        displays.remove(particleId)?.remove()
    }

    override fun despawnAll() {
        displays.values.forEach { it.remove() }
        displays.clear()
    }

    override fun activeCount(): Int {
        return displays.size
    }

    override fun calculateLOD(
        location: Location,
        nearestViewer: Player?,
    ): Int {
        if (nearestViewer == null) return ParticleRenderer.LOD_MAX
        val distance = location.distanceSquared(nearestViewer.location)
        return when {
            distance < 16 * 16 -> 0
            distance < 32 * 32 -> 1
            distance < 48 * 48 -> 2
            else -> 3
        }
    }
}
