package services.afroforge.graviton

import org.bukkit.plugin.java.JavaPlugin
import services.afroforge.graviton.command.CommandManager
import services.afroforge.graviton.emitter.EmitterManager
import services.afroforge.graviton.render.DisplayParticleRenderer

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

        logger.info("Graviton v${description.version} enabled with display-entity renderer.")
    }

    override fun onDisable() {
        if (::emitterManager.isInitialized) {
            emitterManager.stop()
        }
        logger.info("Graviton disabled.")
    }
}
