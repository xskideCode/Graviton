@file:Suppress("MagicNumber")

package services.afroforge.graviton.platform

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Entity
import org.bukkit.plugin.Plugin
import io.papermc.paper.threadedregions.scheduler.ScheduledTask as FoliaTask

/**
 * Folia-compatible scheduler using region-aware scheduling.
 * Tasks are scheduled on the appropriate region thread for the given location/entity.
 */
class FoliaScheduler(private val plugin: Plugin) : Scheduler {
    private val tasks = mutableListOf<FoliaScheduledTask>()

    override fun runTask(
        location: Location,
        task: Runnable,
    ): ScheduledTask {
        val foliaTask = Bukkit.getRegionScheduler().run(plugin, location, { task.run() })
        return trackTask(foliaTask)
    }

    override fun runTaskLater(
        location: Location,
        delayTicks: Long,
        task: Runnable,
    ): ScheduledTask {
        val foliaTask =
            Bukkit.getRegionScheduler().runDelayed(
                plugin,
                location,
                { task.run() },
                delayTicks.coerceAtLeast(1),
            )
        return trackTask(foliaTask)
    }

    override fun runTaskTimer(
        location: Location,
        delayTicks: Long,
        periodTicks: Long,
        task: Runnable,
    ): ScheduledTask {
        val foliaTask =
            Bukkit.getRegionScheduler().runAtFixedRate(
                plugin,
                location,
                { task.run() },
                delayTicks.coerceAtLeast(1),
                periodTicks.coerceAtLeast(1),
            )
        return trackTask(foliaTask)
    }

    override fun runEntityTask(
        entity: Entity,
        task: Runnable,
    ): ScheduledTask {
        val foliaTask =
            checkNotNull(entity.scheduler.run(plugin, { task.run() }, null)) {
                "Entity scheduler returned null - entity may be invalid"
            }
        return trackTask(foliaTask)
    }

    override fun runTaskAsync(task: Runnable): ScheduledTask {
        val foliaTask = Bukkit.getAsyncScheduler().runNow(plugin) { task.run() }
        return trackTask(foliaTask)
    }

    override fun cancelAll() {
        synchronized(tasks) {
            tasks.forEach { it.cancel() }
            tasks.clear()
        }
    }

    override fun isMainThread(): Boolean {
        // Folia doesn't have a single main thread - check if we're on any tick thread
        return !Bukkit.isOwnedByCurrentRegion(Bukkit.getWorlds().first().spawnLocation)
    }

    private fun trackTask(foliaTask: FoliaTask): FoliaScheduledTask {
        val task = FoliaScheduledTask(foliaTask)
        synchronized(tasks) {
            tasks.add(task)
        }
        return task
    }
}

/**
 * Wrapper for Folia's ScheduledTask implementing our ScheduledTask interface.
 */
class FoliaScheduledTask(private val foliaTask: FoliaTask) : ScheduledTask {
    override fun cancel() {
        foliaTask.cancel()
    }

    override fun isCancelled(): Boolean {
        return foliaTask.isCancelled
    }
}
