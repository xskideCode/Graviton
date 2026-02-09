package services.afroforge.graviton.platform

import org.bukkit.Location
import org.bukkit.entity.Entity

/**
 * Platform-agnostic scheduler interface.
 * Abstracts scheduling differences between Paper and Folia.
 */
interface Scheduler {
    /**
     * Run a task on the next tick.
     * On Folia, this runs on the region thread for the given location.
     */
    fun runTask(
        location: Location,
        task: Runnable,
    ): ScheduledTask

    /**
     * Run a task after a delay.
     * @param delayTicks Delay in server ticks (20 ticks = 1 second)
     */
    fun runTaskLater(
        location: Location,
        delayTicks: Long,
        task: Runnable,
    ): ScheduledTask

    /**
     * Run a repeating task.
     * @param delayTicks Initial delay before first execution
     * @param periodTicks Period between executions
     */
    fun runTaskTimer(
        location: Location,
        delayTicks: Long,
        periodTicks: Long,
        task: Runnable,
    ): ScheduledTask

    /**
     * Run a task tied to an entity.
     * On Folia, this runs on the entity's region thread.
     */
    fun runEntityTask(
        entity: Entity,
        task: Runnable,
    ): ScheduledTask

    /**
     * Run a task asynchronously (off the main thread).
     * Use for I/O operations, NOT for Bukkit API calls.
     */
    fun runTaskAsync(task: Runnable): ScheduledTask

    /**
     * Cancel all tasks scheduled by this scheduler.
     */
    fun cancelAll()

    /**
     * Check if the current thread is the main server thread.
     */
    fun isMainThread(): Boolean

    companion object {
        /**
         * Detect and create the appropriate scheduler for the server.
         */
        fun create(plugin: org.bukkit.plugin.Plugin): Scheduler {
            return if (isFolia()) {
                FoliaScheduler(plugin)
            } else {
                PaperScheduler(plugin)
            }
        }

        /**
         * Check if the server is running Folia.
         */
        fun isFolia(): Boolean {
            return runCatching {
                Class.forName("io.papermc.paper.threadedregions.RegionizedServer")
            }.isSuccess
        }
    }
}

/**
 * Handle for a scheduled task, allowing cancellation.
 */
interface ScheduledTask {
    /**
     * Cancel this task.
     */
    fun cancel()

    /**
     * Check if this task has been cancelled.
     */
    fun isCancelled(): Boolean
}
