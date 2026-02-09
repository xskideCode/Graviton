package services.afroforge.graviton.command.sub

import org.bukkit.command.CommandSender
import services.afroforge.graviton.command.SubCommand
import services.afroforge.graviton.emitter.EmitterManager

class ListCommand(
    private val emitterManager: EmitterManager,
) : SubCommand {
    override fun execute(
        sender: CommandSender,
        args: Array<out String>,
    ) {
        sender.sendMessage("§6Active Emitters: §e${emitterManager.emitterCount}")
        sender.sendMessage("§6Total Particles: §e${emitterManager.totalParticleCount}")
        // Detailed list could be added here if needed
    }
}
