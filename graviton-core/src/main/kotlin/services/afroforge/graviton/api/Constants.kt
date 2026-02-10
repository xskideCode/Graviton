package services.afroforge.graviton.api

/**
 * Domain-specific constants for Graviton.
 * These values define default behaviors for emitters and particles.
 */
object Constants {
    // Emitter Defaults
    const val DEFAULT_EMITTER_RATE = 10.0
    const val DEFAULT_BURST_COUNT = 100
    const val DEFAULT_MAX_PARTICLES = 1000
    const val DEFAULT_TICK_RATE = 20
    const val DEFAULT_EMITTER_DURATION_SECONDS = 10.0
    const val DEFAULT_MOVEMENT_THRESHOLD = 0.1

    // Particle Defaults
    const val DEFAULT_PARTICLE_LIFETIME = 1.0
    const val DEFAULT_PARTICLE_DRAG = 0.0
    const val DEFAULT_PARTICLE_RADIAL_VELOCITY = 0.0

    // Render Defaults
    const val TEXT_DISPLAY_WIDTH = 0.5f
    const val TEXT_DISPLAY_HEIGHT = 0.5f
    const val TEXT_DISPLAY_VIEW_RANGE = 64f
}
