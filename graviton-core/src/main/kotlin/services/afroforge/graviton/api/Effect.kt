package services.afroforge.graviton.api

/**
 * Complete particle effect combining emitter and particle configuration.
 */
data class Effect(
    /** Unique effect identifier */
    val id: String,
    /** Emitter configuration */
    val emitter: EmitterConfig,
    /** Particle configuration */
    val particle: ParticleConfig,
    /** Effect metadata */
    val metadata: EffectMetadata = EffectMetadata(),
) {
    init {
        require(id.isNotBlank()) { "Effect ID cannot be blank" }
    }
}

/**
 * Effect metadata for categorization and search.
 */
data class EffectMetadata(
    /** Display name */
    val name: String = "",
    /** Description */
    val description: String = "",
    /** Author */
    val author: String = "",
    /** Tags for categorization */
    val tags: Set<String> = emptySet(),
)
