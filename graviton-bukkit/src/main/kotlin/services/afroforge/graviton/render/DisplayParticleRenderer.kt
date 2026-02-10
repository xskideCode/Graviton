@file:Suppress("MagicNumber", "MaxLineLength", "UnusedPrivateProperty", "UnusedParameter")

package services.afroforge.graviton.render

import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.BlockDisplay
import org.bukkit.entity.Display
import org.bukkit.entity.ItemDisplay
import org.bukkit.entity.Player
import org.bukkit.entity.TextDisplay
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin
import org.bukkit.util.Transformation
import org.joml.AxisAngle4f
import org.joml.Vector3f
import services.afroforge.graviton.api.BillboardMode
import services.afroforge.graviton.api.ParticleConfig
import services.afroforge.graviton.api.RenderMode
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import kotlin.random.Random

/**
 * Renders particles using Minecraft's Display Entities.
 * Supports Item, Block, and Text displays with full transformation and color control.
 */
class DisplayParticleRenderer(
    plugin: Plugin,
) : ParticleRenderer {
    private val displays = ConcurrentHashMap<Int, Display>()
    private val nextId = AtomicInteger(0)

    /**
     * Spawns a new particle display entity.
     * @return The unique ID of the spawned particle.
     */
    override fun spawn(
        location: Location,
        config: ParticleConfig,
        viewers: Collection<Player>,
    ): Int {
        val world = location.world ?: return -1

        val entityClass =
            when (config.renderMode) {
                RenderMode.ITEM -> ItemDisplay::class.java
                RenderMode.BLOCK -> BlockDisplay::class.java
                RenderMode.TEXT -> TextDisplay::class.java
            }

        val entity =
            world.spawn(location, entityClass) { display ->
                display.displayWidth = 0.5f
                display.displayHeight = 0.5f
                display.viewRange = 64f

                // Billboard
                display.billboard =
                    when (config.billboard) {
                        BillboardMode.CENTER -> Display.Billboard.CENTER
                        BillboardMode.VERTICAL -> Display.Billboard.VERTICAL
                        BillboardMode.HORIZONTAL -> Display.Billboard.HORIZONTAL
                        BillboardMode.FIXED -> Display.Billboard.FIXED
                    }

                // Initial Scale
                val initialScale = config.scale.evaluate(0.0).toFloat()
                display.setTransformation(
                    Transformation(
                        Vector3f(0f, 0f, 0f),
                        AxisAngle4f(0f, 0f, 0f, 1f),
                        Vector3f(initialScale, initialScale, initialScale),
                        AxisAngle4f(0f, 0f, 0f, 1f),
                    ),
                )

                // Mode-specific setup
                when (display) {
                    is ItemDisplay -> setupItemDisplay(display, config)
                    is BlockDisplay -> setupBlockDisplay(display, config)
                    is TextDisplay -> setupTextDisplay(display, config, initialScale)
                }
            }

        val id = nextId.getAndIncrement()
        displays[id] = entity
        return id
    }

    private fun setupItemDisplay(
        display: ItemDisplay,
        config: ParticleConfig,
    ) {
        val mat = config.texture?.let { Material.getMaterial(it) } ?: Material.MAGMA_CREAM
        display.setItemStack(ItemStack(mat))
    }

    private fun setupBlockDisplay(
        display: BlockDisplay,
        config: ParticleConfig,
    ) {
        val mat = config.texture?.let { Material.getMaterial(it) } ?: Material.RED_WOOL
        display.block = mat.createBlockData()
    }

    private fun setupTextDisplay(
        display: TextDisplay,
        config: ParticleConfig,
        initialScale: Float,
    ) {
        val texture = config.texture ?: " "
        display.text = texture

        if (texture == " ") {
            setupPixelMode(display, config, initialScale)
        } else {
            // Texture Mode: Transparent background
            display.backgroundColor = org.bukkit.Color.fromARGB(0, 0, 0, 0)
            // Scale is uniform
        }
    }

    private fun setupPixelMode(
        display: TextDisplay,
        config: ParticleConfig,
        initialScale: Float,
    ) {
        // Pixel Mode: Use background color, squash to square
        val initialColor = config.color.sample(0.0, Random)
        display.backgroundColor =
            org.bukkit.Color.fromARGB(
                (initialColor.a * 255).toInt().coerceIn(0, 255),
                (initialColor.r * 255).toInt().coerceIn(0, 255),
                (initialColor.g * 255).toInt().coerceIn(0, 255),
                (initialColor.b * 255).toInt().coerceIn(0, 255),
            )

        // Apply Anisotropic Scale for Square Pixel
        val s = initialScale
        display.setTransformation(
            Transformation(
                Vector3f(0f, 0f, 0f),
                AxisAngle4f(0f, 0f, 0f, 1f),
                // Stretch X to make square
                Vector3f(s * 2.0f, s, s),
                AxisAngle4f(0f, 0f, 0f, 1f),
            ),
        )
    }

    /**
     * Updates an existing particle's properties (position, color, scale).
     */
    override fun update(
        particleId: Int,
        location: Location,
        color: services.afroforge.graviton.data.Color?,
        scale: Double?,
    ) {
        val display = displays[particleId] ?: return

        // Update position
        display.teleportAsync(location)

        // Determine mode based on text content (heuristic)
        val isTextDisplay = display is TextDisplay
        val isPixelMode = isTextDisplay && (display as TextDisplay).text == " "

        // Update Scale
        if (scale != null) {
            val s = scale.toFloat()
            val scaleX = if (isPixelMode) s * 2.0f else s

            val transform = display.transformation
            display.setTransformation(
                Transformation(
                    transform.translation,
                    transform.leftRotation,
                    Vector3f(scaleX, s, s),
                    transform.rightRotation,
                ),
            )
        }

        // Update Color (TextDisplay only)
        if (color != null && isTextDisplay) {
            val textDisplay = display as TextDisplay
            if (isPixelMode) {
                // Update Background for Pixel
                textDisplay.backgroundColor =
                    org.bukkit.Color.fromARGB(
                        (color.a * 255).toInt().coerceIn(0, 255),
                        (color.r * 255).toInt().coerceIn(0, 255),
                        (color.g * 255).toInt().coerceIn(0, 255),
                        (color.b * 255).toInt().coerceIn(0, 255),
                    )
            } else {
                // Update Transparency for Texture?
                // Usually we don't change texture color dynamically unless we use text opacity.
                // For now, ensure background stays transparent just in case.
                textDisplay.backgroundColor = org.bukkit.Color.fromARGB(0, 0, 0, 0)

                // If we wanted to fade the IMAGE, we'd adjust textOpacity:
                textDisplay.textOpacity = (color.a * 255).toInt().coerceIn(0, 255).toByte()
            }
        }
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
