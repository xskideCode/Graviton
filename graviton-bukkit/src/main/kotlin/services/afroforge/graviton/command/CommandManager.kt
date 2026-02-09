@file:Suppress("ReturnCount", "CyclomaticComplexMethod")

package services.afroforge.graviton.command

import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.plugin.Plugin
import services.afroforge.graviton.command.sub.DebugCommand
import services.afroforge.graviton.command.sub.KillAllCommand
import services.afroforge.graviton.command.sub.ListCommand
import services.afroforge.graviton.command.sub.SpawnCommand
import services.afroforge.graviton.emitter.EmitterManager
import java.util.Locale

/**
 * Main command executor for /graviton.
 */
class CommandManager(
    private val plugin: Plugin,
    private val emitterManager: EmitterManager,
) : CommandExecutor,
    TabCompleter {
    private val subCommands =
        mapOf(
            "spawn" to SpawnCommand(emitterManager),
            "list" to ListCommand(emitterManager),
            "killall" to KillAllCommand(emitterManager),
            "debug" to DebugCommand(plugin, emitterManager),
        )

    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>,
    ): Boolean {
        if (args.isEmpty()) {
            sender.sendMessage("§6Graviton Particles §7v${plugin.description.version}")
            sender.sendMessage("§7Usage: /graviton <spawn|list|killall|debug>")
            return true
        }

        val subLabel = args[0].lowercase(Locale.ROOT)
        val subCommand = subCommands[subLabel]

        if (subCommand == null) {
            sender.sendMessage("§cUnknown subcommand: $subLabel")
            return true
        }

        // Shift args
        val subArgs = args.copyOfRange(1, args.size)
        subCommand.execute(sender, subArgs)
        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>,
    ): List<String>? {
        if (args.isEmpty()) return emptyList()

        if (args.size == 1) {
            val input = args[0].lowercase(Locale.ROOT)
            return subCommands.keys.filter { it.startsWith(input) }
        }

        val subLabel = args[0].lowercase(Locale.ROOT)
        val subCommand = subCommands[subLabel] ?: return emptyList()

        // Shift args
        val subArgs = args.copyOfRange(1, args.size)
        return subCommand.tabComplete(sender, subArgs)
    }
}

/**
 * Interface for sub-commands.
 */
interface SubCommand {
    fun execute(
        sender: CommandSender,
        args: Array<out String>,
    )

    fun tabComplete(
        sender: CommandSender,
        args: Array<out String>,
    ): List<String> = emptyList()
}
