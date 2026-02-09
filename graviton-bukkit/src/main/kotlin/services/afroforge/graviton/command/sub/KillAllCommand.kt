package services.afroforge.graviton.command.sub

import org.bukkit.command.CommandSender
import services.afroforge.graviton.command.SubCommand
import services.afroforge.graviton.emitter.EmitterManager

class KillAllCommand(
    private val emitterManager: EmitterManager,
) : SubCommand {
    override fun execute(
        sender: CommandSender,
        args: Array<out String>,
    ) {
        val count = emitterManager.emitterCount
        emitterManager.stop() // Stops all emitters
        // Restart the manager loop effectively (or ensure it can be restarted)
        // EmitterManager.stop() kills the task. We need to start it again if we want to spawn more.
        // Wait, EmitterManager.stop() clears everything.
        // EmitterManager.start() is needed to resume ticking.
        emitterManager.start()

        sender.sendMessage("§aRemoved $count active emitters.")
    }
}
