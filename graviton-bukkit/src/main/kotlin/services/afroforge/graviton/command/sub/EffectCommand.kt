@file:Suppress("MagicNumber")

package services.afroforge.graviton.command.sub

import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import services.afroforge.graviton.api.BillboardMode
import services.afroforge.graviton.api.EmitterConfig
import services.afroforge.graviton.api.EmitterShape
import services.afroforge.graviton.api.EmitterType
import services.afroforge.graviton.api.ParticleConfig
import services.afroforge.graviton.api.RenderMode
import services.afroforge.graviton.command.SubCommand
import services.afroforge.graviton.data.Color
import services.afroforge.graviton.data.ColorRange
import services.afroforge.graviton.data.Gradient
import services.afroforge.graviton.data.Keyframe
import services.afroforge.graviton.data.ValueGradient
import services.afroforge.graviton.data.ValueRange
import services.afroforge.graviton.emitter.EmitterManager
import services.afroforge.graviton.math.Vector3
import java.util.Locale

/**
 * Command to spawn preset particle effects.
 *
 * Usage:
 * /graviton effect <fire|explosion|void|star|cat|fire2>
 */
class EffectCommand(
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

        if (args.isEmpty()) {
            sender.sendMessage("§cUsage: /graviton effect <fire|explosion|void|star|cat>")
        } else {
            val effectName = args[0].lowercase(Locale.ROOT)
            val config = getEffectConfig(effectName)

            if (config != null) {
                val (emitterConfig, particleConfig) = config
                val id = emitterManager.createEmitter(sender.location, emitterConfig, particleConfig)
                sender.sendMessage("§aSpawned effect '$effectName' (ID: $id)")
            } else {
                sender.sendMessage("§cUnknown effect: $effectName")
            }
        }
    }

    private fun getEffectConfig(name: String): Pair<EmitterConfig, ParticleConfig>? {
        return when (name) {
            "fire" -> createFireEffect()
            "fire2" -> createFire2Effect()
            "explosion" -> createExplosionEffect()
            "void" -> createVoidEffect()
            "star" -> createStarEffect()
            "cat" -> createCatEffect()
            else -> null
        }
    }

    override fun tabComplete(
        sender: CommandSender,
        args: Array<out String>,
    ): List<String> =
        if (args.size == 1) {
            listOf("fire", "fire2", "explosion", "void", "star", "cat")
        } else {
            emptyList()
        }

    private fun createFireEffect(): Pair<EmitterConfig, ParticleConfig> {
        val emitter =
            EmitterConfig(
                type = EmitterType.CONTINUOUS,
                rate = 50.0,
                duration = 10.0,
                // Base of fire
                shape = EmitterShape.Circle(0.5, Vector3.UNIT_Y),
            )

        val fireGradient =
            Gradient(
                // Yellow
                Keyframe(0.0, Color(1.0, 0.9, 0.0, 1.0)),
                // Orange
                Keyframe(0.3, Color(1.0, 0.5, 0.0, 1.0)),
                // Red
                Keyframe(0.6, Color(1.0, 0.0, 0.0, 0.8)),
                // Smoke
                Keyframe(0.8, Color(0.2, 0.2, 0.2, 0.5)),
                // Fade out
                Keyframe(1.0, Color(0.0, 0.0, 0.0, 0.0)),
            )

        val scaleGradient =
            ValueGradient(
                Keyframe(0.0, 0.5),
                Keyframe(1.0, 0.1),
            )

        val particle =
            ParticleConfig(
                lifetime = ValueRange.uniform(1.0, 1.5),
                color = ColorRange.animated(fireGradient),
                scale = scaleGradient,
                velocity =
                    ValueRange.uniformVector(
                        // Min
                        Vector3(-0.3, 1.0, -0.3),
                        // Max
                        Vector3(0.3, 3.0, 0.3),
                    ),
                // Text box trick
                renderMode = RenderMode.TEXT,
                // Empty char
                texture = " ",
                billboard = BillboardMode.CENTER,
            )

        return emitter to particle
    }

    private fun createExplosionEffect(): Pair<EmitterConfig, ParticleConfig> {
        val emitter =
            EmitterConfig(
                type = EmitterType.BURST,
                burstCount = 50,
                // Short life
                duration = 2.0,
                // Tiny point source
                shape = EmitterShape.Sphere(0.2),
            )

        val gradient =
            Gradient(
                Keyframe(0.0, Color.WHITE),
                Keyframe(0.2, Color(1.0, 1.0, 0.0, 1.0)),
                Keyframe(0.5, Color(1.0, 0.0, 0.0, 1.0)),
                Keyframe(1.0, Color(0.1, 0.1, 0.1, 0.0)),
            )

        val particle =
            ParticleConfig(
                lifetime = ValueRange.uniform(0.5, 1.0),
                color = ColorRange.animated(gradient),
                scale = ValueGradient.constant(0.8),
                // Explosion force!
                radialVelocity = 8.0,
                // Slow down air resistance
                drag = 0.5,
                renderMode = RenderMode.TEXT,
                texture = " ",
            )

        return emitter to particle
    }

    private fun createVoidEffect(): Pair<EmitterConfig, ParticleConfig> {
        val emitter =
            EmitterConfig(
                type = EmitterType.CONTINUOUS,
                rate = 20.0,
                duration = 10.0,
                shape = EmitterShape.SphereFilled(2.0),
            )

        val particle =
            ParticleConfig(
                lifetime = ValueRange.uniform(2.0, 4.0),
                // Dark purple
                color = ColorRange.constant(Color(0.1, 0.0, 0.2, 0.8)),
                scale = ValueGradient.constant(0.4),
                // Float
                gravity = Vector3.ZERO,
                renderMode = RenderMode.TEXT,
                texture = " ",
            )
        return emitter to particle
    }

    private fun createStarEffect(): Pair<EmitterConfig, ParticleConfig> {
        val emitter =
            EmitterConfig(
                type = EmitterType.BURST,
                burstCount = 100,
                // Or Sphere
                shape = EmitterShape.Point,
            )

        val particle =
            ParticleConfig(
                lifetime = ValueRange.uniform(1.0, 2.0),
                color = ColorRange.constant(Color(1.0, 1.0, 1.0, 1.0)),
                scale = ValueGradient.constant(0.2),
                // Low gravity
                gravity = Vector3(0.0, -1.0, 0.0),
                radialVelocity = 5.0,
                renderMode = RenderMode.TEXT,
                // Star char
                texture = "★",
            )
        return emitter to particle
    }

    private fun createCatEffect(): Pair<EmitterConfig, ParticleConfig> {
        val emitter =
            EmitterConfig(
                type = EmitterType.CONTINUOUS,
                // Slow rate
                rate = 1.0,
                duration = 10.0,
                shape = EmitterShape.Point,
            )

        val particle =
            ParticleConfig(
                lifetime = ValueRange.constant(5.0),
                // Normal color
                color = ColorRange.constant(Color.WHITE),
                scale = ValueGradient.constant(1.0),
                // Very slow fall
                gravity = Vector3(0.0, -0.1, 0.0),
                renderMode = RenderMode.TEXT,
                // USER: This is where you would put the character mapping to your cat image
                // standard approach is using a specific unicode character that your resource pack replaces
                // for now, we'll use a placeholder or common convention like negative space or a specific char
                // Example: Private Use Area char often used for custom icons
                texture = "\uE002",
                billboard = BillboardMode.CENTER,
                rotation = ValueRange.constant(0.0),
            )
        return emitter to particle
    }

    private fun createFire2Effect(): Pair<EmitterConfig, ParticleConfig> {
        val emitter =
            EmitterConfig(
                type = EmitterType.CONTINUOUS,
                rate = 40.0,
                duration = 10.0,
                // Base ring for the flame
                shape = EmitterShape.Circle(0.5, Vector3.UNIT_Y),
            )

        val fireGradient =
            Gradient(
                // Yellow
                Keyframe(0.0, Color(1.0, 1.0, 0.0, 1.0)),
                // Orange
                Keyframe(0.2, Color(1.0, 0.5, 0.0, 1.0)),
                // Red
                Keyframe(0.5, Color(1.0, 0.0, 0.0, 0.8)),
                // Smoke
                Keyframe(0.8, Color(0.2, 0.2, 0.2, 0.5)),
                // Fade
                Keyframe(1.0, Color(0.0, 0.0, 0.0, 0.0)),
            )

        val particle =
            ParticleConfig(
                lifetime = ValueRange.uniform(1.0, 1.5),
                color = ColorRange.animated(fireGradient),
                scale =
                    ValueGradient(
                        Keyframe(0.0, 1.0),
                        // Shrink to nothing
                        Keyframe(1.0, 0.0),
                    ),
                // Upward motion
                velocity = ValueRange.constant(Vector3(0.0, 1.5, 0.0)),
                // Move inward to create taper (Cone shape)
                radialVelocity = -0.5,
                // No gravity
                gravity = Vector3.ZERO,
                renderMode = RenderMode.TEXT,
                texture = " ",
            )

        return emitter to particle
    }
}
