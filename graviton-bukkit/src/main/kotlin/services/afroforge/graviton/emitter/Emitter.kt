@file:Suppress("MagicNumber")

package services.afroforge.graviton.emitter

import org.bukkit.Location
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import services.afroforge.graviton.api.EmitterConfig
import services.afroforge.graviton.api.EmitterType
import services.afroforge.graviton.api.ParticleConfig
import services.afroforge.graviton.emitter.ShapeUtils.getRandomPoint
import services.afroforge.graviton.math.Vector3
import services.afroforge.graviton.render.ParticleRenderer
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlin.random.Random

/**
 * State of an active particle managed by an emitter.
 */
data class ActiveParticle(
    var id: Int = -1,
    var config: ParticleConfig,
    var spawnTime: Long = 0,
    var lifetimeMs: Long = 0,
    var location: Location,
    var velocityX: Double = 0.0,
    var velocityY: Double = 0.0,
    var velocityZ: Double = 0.0,
)

/**
 * Base class for particle emitters.
 * Emitters spawn and manage particles over time.
 */
abstract class Emitter(
    val id: UUID = UUID.randomUUID(),
    val emitterConfig: EmitterConfig,
    val particleConfig: ParticleConfig,
    protected val renderer: ParticleRenderer,
) {
    /** Current location of the emitter */
    abstract val location: Location

    /** Players who should see particles from this emitter */
    open val viewers: Collection<Player> get() = location.world?.players ?: emptyList()

    /** Active particles spawned by this emitter */
    protected val particles = ConcurrentHashMap<Int, ActiveParticle>()

    /** Whether this emitter is currently active */
    var isActive: Boolean = true
        protected set

    /** Time when this emitter was created */
    val createdAt: Long = System.currentTimeMillis()

    /** Number of active particles */
    val particleCount: Int get() = particles.size

    /**
     * Called every tick to update the emitter state.
     * @param deltaMs Time since last tick in milliseconds
     */
    abstract fun tick(deltaMs: Long)

    /**
     * Stop the emitter and despawn all particles.
     */
    open fun stop() {
        isActive = false
        particles.keys.forEach { id ->
            val particle = particles.remove(id)
            if (particle != null) {
                ParticlePool.release(particle)
            }
            renderer.despawn(id)
        }
        particles.clear()
    }

    /**
     * Spawn a new particle at the given location.
     */
    protected fun spawnParticle(spawnLocation: Location): Int? {
        val lifetime = particleConfig.lifetime.sample(Random)
        val lifetimeMs = (lifetime * 1000).toLong()

        val particleId = renderer.spawn(spawnLocation, particleConfig, viewers)
        if (particleId < 0) return null

        // Calculate initial velocity from config
        val baseVelocity = particleConfig.velocity.sample(Random)
        var velocityX = baseVelocity.x
        var velocityY = baseVelocity.y
        var velocityZ = baseVelocity.z

        // Apply radial velocity if configured
        if (particleConfig.radialVelocity != 0.0) {
            // Vector from center to spawn location
            val center = location
            var dx = spawnLocation.x - center.x
            var dy = spawnLocation.y - center.y
            var dz = spawnLocation.z - center.z

            val lengthSq = dx * dx + dy * dy + dz * dz
            if (lengthSq > 0.0001) {
                val length = kotlin.math.sqrt(lengthSq)
                dx /= length
                dy /= length
                dz /= length

                velocityX += dx * particleConfig.radialVelocity
                velocityY += dy * particleConfig.radialVelocity
                velocityZ += dz * particleConfig.radialVelocity
            } else {
                // If at center (e.g. Point shape), radial velocity acts as random spherical burst
                // Or we can ignore it. For now, random direction.
                val rVec = Vector3.random(Random).normalize()
                velocityX += rVec.x * particleConfig.radialVelocity
                velocityY += rVec.y * particleConfig.radialVelocity
                velocityZ += rVec.z * particleConfig.radialVelocity
            }
        }

        particles[particleId] =
            ParticlePool.acquire(
                id = particleId,
                config = particleConfig,
                spawnTime = System.currentTimeMillis(),
                lifetimeMs = lifetimeMs,
                location = spawnLocation,
                velocityX = velocityX,
                velocityY = velocityY,
                velocityZ = velocityZ,
            )

        return particleId
    }

    /**
     * Update all active particles (physics, lifetime, etc.)
     */
    protected fun updateParticles(deltaMs: Long) {
        val now = System.currentTimeMillis()
        val deltaSeconds = deltaMs / 1000.0
        val toRemove = mutableListOf<Int>()

        val iterator = particles.entries.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            val id = entry.key
            val particle = entry.value

            val age = now - particle.spawnTime
            val progress = (age.toDouble() / particle.lifetimeMs).coerceIn(0.0, 1.0)

            // Check if particle expired
            if (age >= particle.lifetimeMs) {
                toRemove.add(id)
                continue
            }

            // Apply gravity
            particle.velocityY += particleConfig.gravity.y * deltaSeconds

            // Apply drag
            val drag = 1.0 - (particleConfig.drag * deltaSeconds)
            particle.velocityX *= drag
            particle.velocityY *= drag
            particle.velocityZ *= drag

            // Update position
            particle.location.add(
                particle.velocityX * deltaSeconds,
                particle.velocityY * deltaSeconds,
                particle.velocityZ * deltaSeconds,
            )

            // Calculate visual properties
            val color = particleConfig.color.sample(progress, Random)
            val scale = particleConfig.scale.evaluate(progress)

            // Update renderer
            renderer.update(id, particle.location, color, scale)
        }

        // Remove expired particles
        toRemove.forEach { id ->
            particles.remove(id)?.let { particle ->
                ParticlePool.release(particle)
            }
            renderer.despawn(id)
        }
    }

    /**
     * Get a spawn location based on emitter shape.
     */
    protected fun getShapeSpawnLocation(): Location = emitterConfig.shape.getRandomPoint(location, Random)
}

/**
 * Emitter attached to a fixed location.
 */
class LocationEmitter(
    override val location: Location,
    emitterConfig: EmitterConfig,
    particleConfig: ParticleConfig,
    renderer: ParticleRenderer,
) : Emitter(
        emitterConfig = emitterConfig,
        particleConfig = particleConfig,
        renderer = renderer,
    ) {
    private var lastSpawnTime = 0L
    private val spawnIntervalMs = (1000.0 / emitterConfig.rate).toLong()
    private var hasEmittedBurst = false

    override fun tick(deltaMs: Long) {
        if (!isActive) return

        // Check duration
        val elapsed = System.currentTimeMillis() - createdAt
        if (emitterConfig.duration > 0 && elapsed >= (emitterConfig.duration * 1000)) {
            stop()
            return
        }

        // Handle different emitter types
        when (emitterConfig.type) {
            EmitterType.CONTINUOUS -> tickConstant()
            EmitterType.BURST -> tickBurst()
            EmitterType.TRAIL -> {} // N/A for location
        }

        // Update existing particles
        updateParticles(deltaMs)
    }

    private fun tickConstant() {
        val now = System.currentTimeMillis()
        if (now - lastSpawnTime >= spawnIntervalMs) {
            spawnParticle(getShapeSpawnLocation())
            lastSpawnTime = now
        }
    }

    private fun tickBurst() {
        if (!hasEmittedBurst) {
            repeat(emitterConfig.burstCount) {
                spawnParticle(getShapeSpawnLocation())
            }
            hasEmittedBurst = true
        }
    }
}

/**
 * Emitter attached to a moving entity.
 */
class EntityEmitter(
    val entity: Entity,
    emitterConfig: EmitterConfig,
    particleConfig: ParticleConfig,
    renderer: ParticleRenderer,
) : Emitter(
        emitterConfig = emitterConfig,
        particleConfig = particleConfig,
        renderer = renderer,
    ) {
    override val location: Location get() = entity.location

    override val viewers: Collection<Player>
        get() = if (entity is Player) listOf(entity) else entity.world.players

    private var lastSpawnTime = 0L
    private val spawnIntervalMs = (1000.0 / emitterConfig.rate).toLong()
    private var hasEmittedBurst = false

    private var lastX: Double? = null
    private var lastY: Double? = null
    private var lastZ: Double? = null

    override fun tick(deltaMs: Long) {
        if (!isActive || !entity.isValid) {
            stop()
            return
        }

        // Check duration
        val elapsed = System.currentTimeMillis() - createdAt
        if (emitterConfig.duration > 0 && elapsed >= (emitterConfig.duration * 1000)) {
            stop()
            return
        }

        // Handle different emitter types
        when (emitterConfig.type) {
            EmitterType.CONTINUOUS -> tickConstant()
            EmitterType.BURST -> tickBurst()
            EmitterType.TRAIL -> tickMovement()
        }

        // Update existing particles
        updateParticles(deltaMs)

        if (lastX == null) {
            lastX = location.x
            lastY = location.y
            lastZ = location.z
        } else {
            lastX = location.x
            lastY = location.y
            lastZ = location.z
        }
    }

    private fun tickConstant() {
        val now = System.currentTimeMillis()
        if (now - lastSpawnTime >= spawnIntervalMs) {
            spawnParticle(getShapeSpawnLocation())
            lastSpawnTime = now
        }
    }

    private fun tickBurst() {
        if (!hasEmittedBurst) {
            repeat(emitterConfig.burstCount) {
                spawnParticle(getShapeSpawnLocation())
            }
            hasEmittedBurst = true
        }
    }

    private fun tickMovement() {
        val lx = lastX
        val ly = lastY
        val lz = lastZ

        if (lx == null || ly == null || lz == null) return

        val dx = location.x - lx
        val dy = location.y - ly
        val dz = location.z - lz

        val distanceSq = dx * dx + dy * dy + dz * dz

        if (distanceSq >= emitterConfig.movementThreshold * emitterConfig.movementThreshold) {
            spawnParticle(getShapeSpawnLocation())
        }
    }
}
