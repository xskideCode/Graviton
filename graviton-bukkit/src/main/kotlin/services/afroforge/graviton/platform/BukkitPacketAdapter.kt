@file:Suppress("MagicNumber")

package services.afroforge.graviton.platform

import org.bukkit.Location
import org.bukkit.entity.BlockDisplay
import org.bukkit.entity.Display
import org.bukkit.entity.ItemDisplay
import org.bukkit.entity.Player
import org.bukkit.entity.TextDisplay
import org.bukkit.util.Transformation
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

/**
 * Bukkit API implementation of PacketAdapter.
 * Uses the Paper Display Entity API (1.19.4+).
 */
class BukkitPacketAdapter(private val plugin: org.bukkit.plugin.Plugin) : PacketAdapter {
    private val entityIdCounter = AtomicInteger(Int.MIN_VALUE)
    private val trackedEntities = ConcurrentHashMap<Int, Display>()

    override fun spawnTextDisplay(
        location: Location,
        viewers: Collection<Player>,
        text: String,
        config: DisplayConfig,
    ): Int {
        val world = location.world ?: throw IllegalArgumentException("Location must have a world")
        val entity =
            world.spawn(location, TextDisplay::class.java) { display ->
                display.text(net.kyori.adventure.text.Component.text(text))
                applyConfig(display, config)
            }
        return trackEntity(entity)
    }

    override fun spawnItemDisplay(
        location: Location,
        viewers: Collection<Player>,
        item: org.bukkit.inventory.ItemStack,
        config: DisplayConfig,
    ): Int {
        val world = location.world ?: throw IllegalArgumentException("Location must have a world")
        val entity =
            world.spawn(location, ItemDisplay::class.java) { display ->
                display.setItemStack(item)
                applyConfig(display, config)
            }
        return trackEntity(entity)
    }

    override fun spawnBlockDisplay(
        location: Location,
        viewers: Collection<Player>,
        blockData: org.bukkit.block.data.BlockData,
        config: DisplayConfig,
    ): Int {
        val world = location.world ?: throw IllegalArgumentException("Location must have a world")
        val entity =
            world.spawn(location, BlockDisplay::class.java) { display ->
                display.block = blockData
                applyConfig(display, config)
            }
        return trackEntity(entity)
    }

    override fun updateTransformation(
        entityId: Int,
        viewers: Collection<Player>,
        transformation: Transformation,
    ) {
        val entity = trackedEntities[entityId] ?: return
        entity.transformation = transformation
    }

    override fun updateBrightness(
        entityId: Int,
        viewers: Collection<Player>,
        brightness: Display.Brightness,
    ) {
        val entity = trackedEntities[entityId] ?: return
        entity.brightness = brightness
    }

    override fun despawn(
        entityId: Int,
        viewers: Collection<Player>,
    ) {
        val entity = trackedEntities.remove(entityId) ?: return
        // Hide from specific viewers (if per-player visibility is needed)
        viewers.forEach { player ->
            player.hideEntity(plugin, entity)
        }
    }

    override fun despawnAll(entityId: Int) {
        val entity = trackedEntities.remove(entityId) ?: return
        entity.remove()
    }

    private fun applyConfig(
        display: Display,
        config: DisplayConfig,
    ) {
        display.billboard = config.billboard
        display.viewRange = config.viewRange
        display.shadowRadius = config.shadowRadius
        display.shadowStrength = config.shadowStrength
        display.transformation = config.transformation
        display.interpolationDuration = config.interpolationDuration
        config.glowColor?.let {
            display.isGlowing = true
            // Note: Glow color requires team assignment in vanilla
        }
        config.brightness?.let { display.brightness = it }
    }

    private fun trackEntity(entity: Display): Int {
        val id = entityIdCounter.incrementAndGet()
        trackedEntities[id] = entity
        return id
    }
}
