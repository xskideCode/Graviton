@file:Suppress("MagicNumber", "CyclomaticComplexMethod", "SwallowedException", "ReturnCount", "LongMethod")

package services.afroforge.graviton.command.sub

import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import services.afroforge.graviton.api.Constants
import services.afroforge.graviton.api.EmitterConfig
import services.afroforge.graviton.api.EmitterShape
import services.afroforge.graviton.api.EmitterType
import services.afroforge.graviton.api.ParticleConfig
import services.afroforge.graviton.api.RenderMode
import services.afroforge.graviton.command.SubCommand
import services.afroforge.graviton.data.Color
import services.afroforge.graviton.data.ColorRange
import services.afroforge.graviton.data.ValueRange
import services.afroforge.graviton.emitter.EmitterManager
import services.afroforge.graviton.math.Vector3
import java.util.Locale

/**
 * Command to spawn a configurable particle emitter.
 *
 * Usage:
 * /graviton spawn <type> <shape> [rate] [duration]
 */
class SpawnCommand(
    private val emitterManager: EmitterManager,
) : SubCommand {
    override fun execute(
        sender: CommandSender,
        args: Array<out String>,
    ) {
        if (sender !is Player) {
            sender.sendMessage("§cOnly players can use this command.")
            return
        }

        if (args.size < 2) {
            sender.sendMessage("§cUsage: /graviton spawn <type> <shape> [rate] [duration]")
            return
        }

        val typeStr = args[0].uppercase(Locale.ROOT)
        val shapeStr = args[1].uppercase(Locale.ROOT)
        val rate = args.getOrNull(2)?.toDoubleOrNull() ?: Constants.DEFAULT_EMITTER_RATE
        val durationInput = args.getOrNull(3)?.toDoubleOrNull()

        if (durationInput != null && durationInput < 0) {
            sender.sendMessage("§cDuration must be non-negative (0 = infinite).")
            return
        }

        val duration = durationInput ?: Constants.DEFAULT_EMITTER_DURATION_SECONDS

        val type =
            try {
                EmitterType.valueOf(typeStr)
            } catch (e: IllegalArgumentException) {
                sender.sendMessage("§cInvalid emitter type: $typeStr. Valid: ${EmitterType.entries.joinToString(", ")}")
                return
            }

        // Shape parsing logic
        val shape =
            when (shapeStr) {
                "POINT" -> EmitterShape.Point
                "SPHERE" -> EmitterShape.Sphere(1.0)
                "SPHERE_FILLED" -> EmitterShape.SphereFilled(1.0)
                "CIRCLE" -> EmitterShape.Circle(1.0)
                "BOX" -> EmitterShape.Box(1.0, 1.0, 1.0)
                "LINE" -> EmitterShape.Line(Vector3(0.0, 2.0, 0.0))
                "CONE" -> EmitterShape.Cone(1.0, 2.0)
                else -> {
                    sender.sendMessage("§cInvalid shape: $shapeStr. Valid: POINT, SPHERE, BOX, LINE, CIRCLE, CONE")
                    return
                }
            }

        val config =
            EmitterConfig(
                type = type,
                rate = rate,
                duration = duration,
                shape = shape,
                burstCount = 20,
                movementThreshold = 0.5,
            )

        // Default particle config (Green Redstone/Magma for visibility)
        // converting bukkit color to graviton color (Teal: 0, 128, 128 -> 0.0, 0.5, 0.5)
        val teal = Color(0.0, 0.5, 0.5, 1.0)

        // Default to TextDisplay for "pixel" look
        // Default to empty char for background color trick
        val particleConfig =
            ParticleConfig(
                color = ColorRange.constant(teal),
                lifetime = ValueRange.uniform(1.0, 2.0),
                velocity = ValueRange.constant(Vector3.ZERO),
                renderMode = RenderMode.TEXT,
                texture = " ",
                radialVelocity = if (type == EmitterType.BURST || shape is EmitterShape.Sphere) 5.0 else 0.0,
            )

        // For Trail type, attach to player. For others, use location.
        val id =
            if (type == EmitterType.TRAIL) {
                emitterManager.createEmitter(sender, config, particleConfig)
            } else {
                emitterManager.createEmitter(sender.location, config, particleConfig)
            }

        sender.sendMessage("§aSpawned emitter $id ($type, $shapeStr)")
    }

    override fun tabComplete(
        sender: CommandSender,
        args: Array<out String>,
    ): List<String> =
        when (args.size) {
            1 -> EmitterType.entries.map { it.name.lowercase() }
            2 -> listOf("point", "sphere", "sphere_filled", "circle", "box", "line", "cone")
            // Rate
            3 -> listOf("10", "20", "50")
            // Duration
            4 -> listOf("5", "10", "0")
            else -> emptyList()
        }
}
