package com.lainovic.tomtom.straycat

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.lainovic.tomtom.straycat.infrastructure.service.SimulationService
import com.lainovic.tomtom.straycat.domain.simulation.SimulationState
import com.lainovic.tomtom.straycat.ui.simulation.SimulationPlayerViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class SimulationViewModelTest {

    private lateinit var application: Application
    private lateinit var viewModel: SimulationPlayerViewModel

    @Before
    fun setup() {
        application = ApplicationProvider.getApplicationContext()
        viewModel = SimulationPlayerViewModel(application)
    }

    @Test
    fun `initial state is Idle`() {
        assertEquals(SimulationState.Idle, viewModel.simulationState.value)
    }

    @Test
    fun `startStopSimulation from Idle starts simulation and transitions to Running`() = runTest {
        // Given: Initial Idle state
        assertEquals(SimulationState.Idle, viewModel.simulationState.value)

        // When: startStopSimulation is called
        viewModel.startStop()

        // Then: State transitions to Running
        assertEquals(SimulationState.Running, viewModel.simulationState.value)

        // And: Service is started with START action
        val shadowApplication = shadowOf(application)
        val startedService = shadowApplication.nextStartedService
        assertEquals(SimulationService::class.java.name, startedService.component?.className)
        assertEquals(SimulationService.ACTION_START, startedService.action)
    }

    @Test
    fun `startStopSimulation from Running stops simulation and transitions to Stopped`() = runTest {
        // Given: Running state
        viewModel.startStop() // Start first
        assertEquals(SimulationState.Running, viewModel.simulationState.value)
        shadowOf(application).clearStartedServices()

        // When: startStopSimulation is called again
        viewModel.startStop()

        // Then: State transitions to Stopped
        assertEquals(SimulationState.Stopped, viewModel.simulationState.value)

        // And: Service is started with STOP action
        val shadowApplication = shadowOf(application)
        val startedService = shadowApplication.nextStartedService
        assertEquals(SimulationService.ACTION_STOP, startedService.action)
    }

    @Test
    fun `startStopSimulation from Paused stops simulation and transitions to Stopped`() = runTest {
        // Given: Paused state
        viewModel.startStop() // Start
        viewModel.pauseOrResume() // Pause
        assertEquals(SimulationState.Paused, viewModel.simulationState.value)
        shadowOf(application).clearStartedServices()

        // When: startStopSimulation is called
        viewModel.startStop()

        // Then: State transitions to Stopped
        assertEquals(SimulationState.Stopped, viewModel.simulationState.value)

        // And: Service is started with STOP action
        val shadowApplication = shadowOf(application)
        val startedService = shadowApplication.nextStartedService
        assertEquals(SimulationService.ACTION_STOP, startedService.action)
    }

    @Test
    fun `startStopSimulation from Stopped starts simulation and transitions to Running`() = runTest {
        // Given: Stopped state
        viewModel.startStop() // Start
        viewModel.startStop() // Stop
        assertEquals(SimulationState.Stopped, viewModel.simulationState.value)
        shadowOf(application).clearStartedServices()

        // When: startStopSimulation is called again
        viewModel.startStop()

        // Then: State transitions to Running
        assertEquals(SimulationState.Running, viewModel.simulationState.value)

        // And: Service is started with START action
        val shadowApplication = shadowOf(application)
        val startedService = shadowApplication.nextStartedService
        assertEquals(SimulationService.ACTION_START, startedService.action)
    }

    @Test
    fun `pauseResumeSimulation from Running pauses simulation and transitions to Paused`() = runTest {
        // Given: Running state
        viewModel.startStop()
        assertEquals(SimulationState.Running, viewModel.simulationState.value)
        shadowOf(application).clearStartedServices()

        // When: pauseResumeSimulation is called
        viewModel.pauseOrResume()

        // Then: State transitions to Paused
        assertEquals(SimulationState.Paused, viewModel.simulationState.value)

        // And: Service is started with PAUSE action
        val shadowApplication = shadowOf(application)
        val startedService = shadowApplication.nextStartedService
        assertEquals(SimulationService.ACTION_PAUSE, startedService.action)
    }

    @Test
    fun `pauseResumeSimulation from Paused resumes simulation and transitions to Running`() = runTest {
        // Given: Paused state
        viewModel.startStop() // Start
        viewModel.pauseOrResume() // Pause
        assertEquals(SimulationState.Paused, viewModel.simulationState.value)
        shadowOf(application).clearStartedServices()

        // When: pauseResumeSimulation is called again
        viewModel.pauseOrResume()

        // Then: State transitions to Running
        assertEquals(SimulationState.Running, viewModel.simulationState.value)

        // And: Service is started with RESUME action
        val shadowApplication = shadowOf(application)
        val startedService = shadowApplication.nextStartedService
        assertEquals(SimulationService.ACTION_RESUME, startedService.action)
    }

    @Test
    fun `pauseResumeSimulation from Idle does not change state`() = runTest {
        // Given: Idle state
        assertEquals(SimulationState.Idle, viewModel.simulationState.value)

        // When: pauseResumeSimulation is called
        viewModel.pauseOrResume()

        // Then: State remains Idle
        assertEquals(SimulationState.Idle, viewModel.simulationState.value)

        // And: No service is started
        val shadowApplication = shadowOf(application)
        val startedService = shadowApplication.nextStartedService
        assertEquals(null, startedService)
    }

    @Test
    fun `pauseResumeSimulation from Stopped does not change state`() = runTest {
        // Given: Stopped state
        viewModel.startStop() // Start
        viewModel.startStop() // Stop
        assertEquals(SimulationState.Stopped, viewModel.simulationState.value)
        shadowOf(application).clearStartedServices()

        // When: pauseResumeSimulation is called
        viewModel.pauseOrResume()

        // Then: State remains Stopped
        assertEquals(SimulationState.Stopped, viewModel.simulationState.value)

        // And: No service is started
        val shadowApplication = shadowOf(application)
        val startedService = shadowApplication.nextStartedService
        assertEquals(null, startedService)
    }

    @Test
    fun `complete flow - start, pause, resume, stop transitions correctly`() = runTest {
        // Start
        viewModel.startStop()
        assertEquals(SimulationState.Running, viewModel.simulationState.value)

        // Pause
        viewModel.pauseOrResume()
        assertEquals(SimulationState.Paused, viewModel.simulationState.value)

        // Resume
        viewModel.pauseOrResume()
        assertEquals(SimulationState.Running, viewModel.simulationState.value)

        // Stop
        viewModel.startStop()
        assertEquals(SimulationState.Stopped, viewModel.simulationState.value)
    }

    @Test
    fun `multiple start-stop cycles work correctly`() = runTest {
        // First cycle
        viewModel.startStop()
        assertEquals(SimulationState.Running, viewModel.simulationState.value)
        viewModel.startStop()
        assertEquals(SimulationState.Stopped, viewModel.simulationState.value)

        // Second cycle
        viewModel.startStop()
        assertEquals(SimulationState.Running, viewModel.simulationState.value)
        viewModel.startStop()
        assertEquals(SimulationState.Stopped, viewModel.simulationState.value)

        // Third cycle
        viewModel.startStop()
        assertEquals(SimulationState.Running, viewModel.simulationState.value)
    }
}

