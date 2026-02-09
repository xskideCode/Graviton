package services.afroforge.graviton.emitter

import io.mockk.*
import io.mockk.impl.annotations.MockK
import org.bukkit.Location
import org.bukkit.plugin.Plugin
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import services.afroforge.graviton.api.EmitterConfig
import services.afroforge.graviton.api.EmitterType
import services.afroforge.graviton.api.ParticleConfig
import services.afroforge.graviton.platform.ScheduledTask
import services.afroforge.graviton.platform.Scheduler
import services.afroforge.graviton.render.ParticleRenderer
import java.util.UUID

class EmitterManagerTest {

    @MockK
    lateinit var plugin: Plugin

    @MockK
    lateinit var renderer: ParticleRenderer

    @MockK
    lateinit var scheduler: Scheduler

    @MockK
    lateinit var task: ScheduledTask

    private lateinit var manager: EmitterManager

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        manager = EmitterManager(plugin, renderer, scheduler = scheduler)
    }

    @Test
    fun `test start schedules task`() {
        // Mock server.worlds for start()
        val mockWorld = mockk<org.bukkit.World>()
        val mockLocation = mockk<Location>()
        every { plugin.server.worlds } returns listOf(mockWorld)
        every { mockWorld.spawnLocation } returns mockLocation

        every { scheduler.runTaskTimer(any(), any(), any(), any()) } returns task

        manager.start()

        verify { scheduler.runTaskTimer(any<Location>(), 0L, any(), any()) }
    }

    @Test
    fun `test stop cancels task`() {
        val mockWorld = mockk<org.bukkit.World>()
        val mockLocation = mockk<Location>()
        every { plugin.server.worlds } returns listOf(mockWorld)
        every { mockWorld.spawnLocation } returns mockLocation

        every { scheduler.runTaskTimer(any(), any(), any(), any()) } returns task
        every { task.cancel() } just Runs

        manager.start()
        manager.stop()

        verify { task.cancel() }
    }

    @Test
    fun `test createEmitter adds to manager`() {
        val emitterConfig = mockk<EmitterConfig>(relaxed = true) {
            every { type } returns EmitterType.CONTINUOUS
        }
        val particleConfig = mockk<ParticleConfig>(relaxed = true)
        val location = mockk<Location>(relaxed = true)

        val id = manager.createEmitter(location, emitterConfig, particleConfig)

        assertNotNull(id)
        assertEquals(1, manager.emitterCount)
    }

    @Test
    fun `test tick updates emitters`() {
        // Mock server.worlds for start()
        val mockWorld = mockk<org.bukkit.World>()
        val mockLocation = mockk<Location>()
        every { plugin.server.worlds } returns listOf(mockWorld)
        every { mockWorld.spawnLocation } returns mockLocation

        val slot = slot<Runnable>()
        every { scheduler.runTaskTimer(any(), any(), any(), capture(slot)) } returns task

        manager.start()
        val runnable = slot.captured
        assertNotNull(runnable)

        val emitterConfig = mockk<EmitterConfig>(relaxed = true) {
            every { type } returns EmitterType.CONTINUOUS
            every { tickRate } returns 20
        }
        val particleConfig = mockk<ParticleConfig>(relaxed = true)
        val location = mockk<Location>(relaxed = true)
        manager.createEmitter(location, emitterConfig, particleConfig)
        
        // Mock plugin logger for error handling path if exception
        every { plugin.logger } returns mockk(relaxed = true)

        // Run the tick
        runnable.run()
    }
}
