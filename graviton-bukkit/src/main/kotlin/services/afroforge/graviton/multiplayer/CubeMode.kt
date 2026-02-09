@file:Suppress("MagicNumber")

package services.afroforge.graviton.multiplayer

/**
 * Rendering mode for 3D particle cubes.
 * Affects entity count and visual quality.
 */
enum class CubeMode(
    /** Number of display entities used per particle */
    val entityCount: Int,
    /** Description of visual appearance */
    val description: String,
) {
    /**
     * Single entity, always faces player.
     * Best for high particle counts.
     */
    BILLBOARD(1, "Always faces player"),

    /**
     * Two entities: front and back faces.
     * Good balance of quality and performance.
     */
    DUAL_FACE(2, "Front and back faces"),

    /**
     * Six entities: full 3D cube.
     * Best for low-count decorative particles.
     */
    FULL_CUBE(6, "True 3D cube with all faces"),
}

/**
 * Configuration for cube rendering.
 */
data class CubeConfig(
    /** Default cube mode for particles */
    val defaultMode: CubeMode = CubeMode.BILLBOARD,
    /** Auto-downgrade mode when budget is tight */
    val autoDowngrade: Boolean = true,
    /** Budget threshold (%) to trigger downgrade */
    val downgradeThreshold: Double = 0.75,
)
