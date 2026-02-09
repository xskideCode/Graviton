package services.afroforge.graviton.platform

import org.bukkit.Location
import org.bukkit.entity.Display
import org.bukkit.entity.Player
import org.bukkit.util.Transformation
import org.joml.Quaternionf
import org.joml.Vector3f

/**
 * Adapter for managing display entities and sending packets.
 * Abstracts the underlying NMS/Paper API for spawning visual elements.
 */
interface PacketAdapter {
    /**
     * Spawn a text display entity visible to specific players.
     * @return Entity ID for tracking
     */
    fun spawnTextDisplay(
        location: Location,
        viewers: Collection<Player>,
        text: String,
        config: DisplayConfig = DisplayConfig(),
    ): Int

    /**
     * Spawn an item display entity.
     * @return Entity ID for tracking
     */
    fun spawnItemDisplay(
        location: Location,
        viewers: Collection<Player>,
        item: org.bukkit.inventory.ItemStack,
        config: DisplayConfig = DisplayConfig(),
    ): Int

    /**
     * Spawn a block display entity.
     * @return Entity ID for tracking
     */
    fun spawnBlockDisplay(
        location: Location,
        viewers: Collection<Player>,
        blockData: org.bukkit.block.data.BlockData,
        config: DisplayConfig = DisplayConfig(),
    ): Int

    /**
     * Update an existing display entity's transformation.
     */
    fun updateTransformation(
        entityId: Int,
        viewers: Collection<Player>,
        transformation: Transformation,
    )

    /**
     * Update display entity brightness.
     */
    fun updateBrightness(
        entityId: Int,
        viewers: Collection<Player>,
        brightness: Display.Brightness,
    )

    /**
     * Despawn a display entity for specific viewers.
     */
    fun despawn(
        entityId: Int,
        viewers: Collection<Player>,
    )

    /**
     * Despawn a display entity for all viewers.
     */
    fun despawnAll(entityId: Int)

    companion object {
        /**
         * Create the appropriate packet adapter for the server.
         */
        fun create(plugin: org.bukkit.plugin.Plugin): PacketAdapter = BukkitPacketAdapter(plugin)
    }
}

/**
 * Configuration for display entities.
 */
data class DisplayConfig(
    /** Billboard mode */
    val billboard: Display.Billboard = Display.Billboard.CENTER,
    /** View range multiplier (default 1.0 = 64 blocks) */
    val viewRange: Float = 1.0f,
    /** Shadow radius (0 = no shadow) */
    val shadowRadius: Float = 0.0f,
    /** Shadow strength (0-1) */
    val shadowStrength: Float = 1.0f,
    /** Initial transformation */
    val transformation: Transformation =
        Transformation(
            Vector3f(0f, 0f, 0f),
            Quaternionf(),
            Vector3f(1f, 1f, 1f),
            Quaternionf(),
        ),
    /** Interpolation duration in ticks for smooth movement */
    val interpolationDuration: Int = 0,
    /** Glow color (null = no glow) */
    val glowColor: org.bukkit.Color? = null,
    /** Custom brightness (null = use world lighting) */
    val brightness: Display.Brightness? = null,
)
