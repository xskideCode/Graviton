package services.afroforge.graviton

import org.bukkit.plugin.java.JavaPlugin
import services.afroforge.graviton.command.CommandManager
import services.afroforge.graviton.emitter.EmitterManager
import services.afroforge.graviton.render.DisplayParticleRenderer

/**
 * Main entry point for the Graviton particle plugin.
 * Handles lifecycle, command registration, and the ticker loop.
 */
class GravitonPlugin : JavaPlugin() {
    private lateinit var emitterManager: EmitterManager
    private lateinit var commandManager: CommandManager
    private lateinit var renderer: DisplayParticleRenderer

    override fun onEnable() {
        // Initialize Core Components
        renderer = DisplayParticleRenderer(this)

        emitterManager = EmitterManager(this, renderer)
        emitterManager.start()

        commandManager = CommandManager(this, emitterManager)

        // Register Commands
        getCommand("graviton")?.setExecutor(commandManager)
        getCommand("graviton")?.tabCompleter = commandManager

        // Register Listeners
        server.pluginManager.registerEvents(
            services.afroforge.graviton.listener.AnvilKillListener(emitterManager),
            this,
        )

        logger.info("Graviton v${description.version} enabled with display-entity renderer.")
    }

    override fun onDisable() {
        if (::emitterManager.isInitialized) {
            emitterManager.stop()
        }
        logger.info("Graviton disabled.")
    }
}
