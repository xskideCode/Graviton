@file:Suppress("MaxLineLength", "MagicNumber")

package services.afroforge.graviton.command.sub

import net.kyori.adventure.text.Component
import org.bukkit.command.CommandSender
import org.bukkit.plugin.Plugin
import org.bukkit.scheduler.BukkitTask
import services.afroforge.graviton.command.SubCommand
import services.afroforge.graviton.emitter.EmitterManager

class DebugCommand(
    private val plugin: Plugin,
    private val emitterManager: EmitterManager,
) : SubCommand {
    private var isDebugging = false
    private var debugTask: BukkitTask? = null

    override fun execute(
        sender: CommandSender,
        args: Array<out String>,
    ) {
        isDebugging = !isDebugging

        if (isDebugging) {
            sender.sendMessage("§aDebug mode enabled.")
            startDebugTask()
        } else {
            sender.sendMessage("§cDebug mode disabled.")
            stopDebugTask()
        }
    }

    private fun startDebugTask() {
        debugTask?.cancel()
        debugTask =
            org.bukkit.Bukkit.getScheduler().runTaskTimer(
                plugin,
                Runnable {
                    if (!isDebugging) {
                        stopDebugTask()
                        return@Runnable
                    }
                    val msg =
                        "§eEmitters: ${emitterManager.emitterCount} | Particles: ${emitterManager.totalParticleCount}"
                    plugin.server.onlinePlayers.forEach { player ->
                        if (player.hasPermission("graviton.debug")) {
                            player.sendActionBar(Component.text(msg))
                        }
                    }
                },
                0L,
                20L,
            )
    }

    private fun stopDebugTask() {
        debugTask?.cancel()
        debugTask = null
    }
}
