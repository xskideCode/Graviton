@file:Suppress("MagicNumber", "CyclomaticComplexMethod", "SwallowedException", "ReturnCount", "LongMethod")

package services.afroforge.graviton.command.sub

import org.bukkit.Color
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import services.afroforge.graviton.api.EmitterConfig
import services.afroforge.graviton.api.EmitterShape
import services.afroforge.graviton.api.EmitterType
import services.afroforge.graviton.api.ParticleConfig
import services.afroforge.graviton.command.SubCommand
import services.afroforge.graviton.data.ColorRange
import services.afroforge.graviton.data.ValueRange
import services.afroforge.graviton.emitter.EmitterManager
import services.afroforge.graviton.math.Vector3
import java.util.Locale

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
        val rate = args.getOrNull(2)?.toDoubleOrNull() ?: 10.0
        val duration = args.getOrNull(3)?.toDoubleOrNull() ?: 0.0

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
                duration = if (duration == 0.0) 10.0 else duration, // Default to 10s if not specified
                shape = shape,
                burstCount = 20,
                movementThreshold = 0.5,
            )

        // Default particle config (Green Redstone/Magma for visibility)
        // converting bukkit color to graviton color (Teal: 0, 128, 128 -> 0.0, 0.5, 0.5)
        val teal = services.afroforge.graviton.data.Color(0.0, 0.5, 0.5, 1.0)

        val particleConfig =
            ParticleConfig(
                color = ColorRange.constant(teal),
                lifetime = ValueRange.uniform(1.0, 2.0),
                // scale defaults to 1.0
                velocity = ValueRange.constant(Vector3.ZERO),
                // speed is handled by velocity magnitude usually, or we can use random velocity
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
            3 -> listOf("10", "20", "50") // Rate
            4 -> listOf("5", "10", "0") // Duration
            else -> emptyList()
        }
}
