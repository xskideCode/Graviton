package services.afroforge.graviton.emitter

import org.bukkit.Location
import services.afroforge.graviton.api.ParticleConfig
import services.afroforge.graviton.util.ObjectPool

/**
 * Global pool for ActiveParticle instances to minimize garbage collection.
 */
object ParticlePool {
    private const val MAX_POOL_SIZE = 10000

    // We need a dummy location and config for initialization
    private val dummyLocation by lazy { Location(null, 0.0, 0.0, 0.0) }
    private val dummyConfig by lazy { ParticleConfig() }

    private val pool =
        ObjectPool(
            factory = {
                ActiveParticle(
                    config = dummyConfig,
                    location = dummyLocation.clone(),
                )
            },
            reset = { particle ->
                particle.id = -1
                // We don't reset other fields here because they get overwritten on acquire
                // optimizing performance by avoiding double assignment
            },
            maxSize = MAX_POOL_SIZE,
        )

    /**
     * Acquire an ActiveParticle from the pool.
     */
    @Suppress("LongParameterList")
    fun acquire(
        id: Int,
        config: ParticleConfig,
        spawnTime: Long,
        lifetimeMs: Long,
        location: Location,
        velocityX: Double,
        velocityY: Double,
        velocityZ: Double,
    ): ActiveParticle {
        val particle = pool.acquire()
        particle.id = id
        particle.config = config
        particle.spawnTime = spawnTime
        particle.lifetimeMs = lifetimeMs

        // Update location (reuse mutable object if possible to avoid allocation)
        if (particle.location.world != location.world) {
            particle.location = location.clone()
        } else {
            particle.location.setX(location.x)
            particle.location.setY(location.y)
            particle.location.setZ(location.z)
            particle.location.yaw = location.yaw
            particle.location.pitch = location.pitch
        }

        particle.velocityX = velocityX
        particle.velocityY = velocityY
        particle.velocityZ = velocityZ

        return particle
    }

    /**
     * Release an ActiveParticle back to the pool.
     */
    fun release(particle: ActiveParticle) {
        pool.release(particle)
    }

    /**
     * Clear the pool.
     */
    fun clear() {
        pool.clear()
    }

    /**
     * Get current pool size (for debug).
     */
    fun size() = pool.size()
}
