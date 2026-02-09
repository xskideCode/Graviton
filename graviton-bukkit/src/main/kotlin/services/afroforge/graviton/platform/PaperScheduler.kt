@file:Suppress("MagicNumber")

package services.afroforge.graviton.platform

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Entity
import org.bukkit.plugin.Plugin
import org.bukkit.scheduler.BukkitTask

/**
 * Standard Paper/Spigot scheduler implementation.
 * Uses the traditional Bukkit scheduler API.
 */
class PaperScheduler(private val plugin: Plugin) : Scheduler {
    private val tasks = mutableListOf<BukkitScheduledTask>()

    override fun runTask(
        location: Location,
        task: Runnable,
    ): ScheduledTask {
        val bukkitTask = Bukkit.getScheduler().runTask(plugin, task)
        return trackTask(bukkitTask)
    }

    override fun runTaskLater(
        location: Location,
        delayTicks: Long,
        task: Runnable,
    ): ScheduledTask {
        val bukkitTask = Bukkit.getScheduler().runTaskLater(plugin, task, delayTicks)
        return trackTask(bukkitTask)
    }

    override fun runTaskTimer(
        location: Location,
        delayTicks: Long,
        periodTicks: Long,
        task: Runnable,
    ): ScheduledTask {
        val bukkitTask = Bukkit.getScheduler().runTaskTimer(plugin, task, delayTicks, periodTicks)
        return trackTask(bukkitTask)
    }

    override fun runEntityTask(
        entity: Entity,
        task: Runnable,
    ): ScheduledTask {
        // On Paper, entity tasks run on main thread like any other task
        val bukkitTask = Bukkit.getScheduler().runTask(plugin, task)
        return trackTask(bukkitTask)
    }

    override fun runTaskAsync(task: Runnable): ScheduledTask {
        val bukkitTask = Bukkit.getScheduler().runTaskAsynchronously(plugin, task)
        return trackTask(bukkitTask)
    }

    override fun cancelAll() {
        synchronized(tasks) {
            tasks.forEach { it.cancel() }
            tasks.clear()
        }
    }

    override fun isMainThread(): Boolean {
        return Bukkit.isPrimaryThread()
    }

    private fun trackTask(bukkitTask: BukkitTask): BukkitScheduledTask {
        val task = BukkitScheduledTask(bukkitTask)
        synchronized(tasks) {
            tasks.add(task)
        }
        return task
    }
}

/**
 * Wrapper for BukkitTask implementing ScheduledTask interface.
 */
class BukkitScheduledTask(private val bukkitTask: BukkitTask) : ScheduledTask {
    override fun cancel() {
        bukkitTask.cancel()
    }

    override fun isCancelled(): Boolean {
        return bukkitTask.isCancelled
    }
}
