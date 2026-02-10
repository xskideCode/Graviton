@file:Suppress("MagicNumber")

package services.afroforge.graviton.listener

import org.bukkit.Material
import org.bukkit.entity.FallingBlock
import org.bukkit.entity.LivingEntity
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDeathEvent
import services.afroforge.graviton.api.BillboardMode
import services.afroforge.graviton.api.EmitterConfig
import services.afroforge.graviton.api.EmitterShape
import services.afroforge.graviton.api.EmitterType
import services.afroforge.graviton.api.ParticleConfig
import services.afroforge.graviton.api.RenderMode
import services.afroforge.graviton.data.Color
import services.afroforge.graviton.data.ColorRange
import services.afroforge.graviton.data.Keyframe
import services.afroforge.graviton.data.ValueGradient
import services.afroforge.graviton.data.ValueRange
import services.afroforge.graviton.emitter.EmitterManager
import services.afroforge.graviton.math.Vector3

class AnvilKillListener(
    private val emitterManager: EmitterManager,
) : Listener {
    @EventHandler
    fun onEntityDeath(event: EntityDeathEvent) {
        val entity = event.entity
        val lastDamageCause = entity.lastDamageCause

        // Check if killed by falling block (Anvil)
        if (lastDamageCause is EntityDamageByEntityEvent) {
            val damager = lastDamageCause.damager
            if (damager is FallingBlock && damager.blockData.material == Material.ANVIL) {
                triggerBonkEffect(entity)
            }
        }
    }

    private fun triggerBonkEffect(entity: LivingEntity) {
        val location = entity.eyeLocation.add(0.0, 0.5, 0.0)

        // Play Custom Sound
        entity.world.playSound(entity.location, "custom.bonk", 1.0f, 1.0f)

        // Spawn Impact Particles (Crit)
        entity.world.spawnParticle(org.bukkit.Particle.CRIT, location, 20, 0.5, 0.5, 0.5, 0.1)

        // 1. BONK Text Emitter
        val bonkconfig =
            EmitterConfig(
                type = EmitterType.BURST,
                burstCount = 1,
                duration = 1.0,
                shape = EmitterShape.Point,
            )

        val bonkParticle =
            ParticleConfig(
                lifetime = ValueRange.constant(1.0),
                color = ColorRange.constant(Color(1.0, 1.0, 1.0, 1.0)),
                // Fades out quickly
                scale =
                    ValueGradient(
                        Keyframe(0.0, 1.0),
                        Keyframe(0.8, 1.0),
                        Keyframe(1.0, 0.0),
                    ),
                // Slight upward
                velocity = ValueRange.constant(Vector3(0.0, 1.0, 0.0)),
                renderMode = RenderMode.TEXT,
                // BONK char
                texture = "\uE003",
                billboard = BillboardMode.CENTER,
            )

        emitterManager.createEmitter(location, bonkconfig, bonkParticle)

        // 2. Star Burst Emitter
        val starConfig =
            EmitterConfig(
                type = EmitterType.BURST,
                burstCount = 8,
                duration = 1.0,
                shape = EmitterShape.Point,
            )

        val starParticle =
            ParticleConfig(
                lifetime = ValueRange.uniform(0.5, 1.0),
                color = ColorRange.constant(Color.WHITE),
                scale =
                    ValueGradient(
                        // Start full size
                        Keyframe(0.0, 1.0),
                        // Shrink
                        Keyframe(1.0, 0.0),
                    ),
                // Outward burst
                radialVelocity = 3.0,
                // Upward bias
                velocity = ValueRange.constant(Vector3(0.0, 2.0, 0.0)),
                renderMode = RenderMode.TEXT,
                // Star char
                texture = "\uE004",
                billboard = BillboardMode.CENTER,
            )

        emitterManager.createEmitter(location, starConfig, starParticle)
    }
}
