package com.lainovic.tomtom.straycat

import android.app.Application
import androidx.test.core.app.ApplicationProvider
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
    private lateinit var viewModel: SimulationViewModel

    @Before
    fun setup() {
        application = ApplicationProvider.getApplicationContext()
        viewModel = SimulationViewModel(application)
    }

    @Test
    fun `initial state is Idle`() {
        assertEquals(LocationServiceState.Idle, viewModel.state.value)
    }

    @Test
    fun `startStopSimulation from Idle starts simulation and transitions to Running`() = runTest {
        // Given: Initial Idle state
        assertEquals(LocationServiceState.Idle, viewModel.state.value)

        // When: startStopSimulation is called
        viewModel.startStop()

        // Then: State transitions to Running
        assertEquals(LocationServiceState.Running, viewModel.state.value)

        // And: Service is started with START action
        val shadowApplication = shadowOf(application)
        val startedService = shadowApplication.nextStartedService
        assertEquals(LocationService::class.java.name, startedService.component?.className)
        assertEquals(LocationService.ACTION_START, startedService.action)
    }

    @Test
    fun `startStopSimulation from Running stops simulation and transitions to Stopped`() = runTest {
        // Given: Running state
        viewModel.startStop() // Start first
        assertEquals(LocationServiceState.Running, viewModel.state.value)
        shadowOf(application).clearStartedServices()

        // When: startStopSimulation is called again
        viewModel.startStop()

        // Then: State transitions to Stopped
        assertEquals(LocationServiceState.Stopped, viewModel.state.value)

        // And: Service is started with STOP action
        val shadowApplication = shadowOf(application)
        val startedService = shadowApplication.nextStartedService
        assertEquals(LocationService.ACTION_STOP, startedService.action)
    }

    @Test
    fun `startStopSimulation from Paused stops simulation and transitions to Stopped`() = runTest {
        // Given: Paused state
        viewModel.startStop() // Start
        viewModel.pauseResume() // Pause
        assertEquals(LocationServiceState.Paused, viewModel.state.value)
        shadowOf(application).clearStartedServices()

        // When: startStopSimulation is called
        viewModel.startStop()

        // Then: State transitions to Stopped
        assertEquals(LocationServiceState.Stopped, viewModel.state.value)

        // And: Service is started with STOP action
        val shadowApplication = shadowOf(application)
        val startedService = shadowApplication.nextStartedService
        assertEquals(LocationService.ACTION_STOP, startedService.action)
    }

    @Test
    fun `startStopSimulation from Stopped starts simulation and transitions to Running`() = runTest {
        // Given: Stopped state
        viewModel.startStop() // Start
        viewModel.startStop() // Stop
        assertEquals(LocationServiceState.Stopped, viewModel.state.value)
        shadowOf(application).clearStartedServices()

        // When: startStopSimulation is called again
        viewModel.startStop()

        // Then: State transitions to Running
        assertEquals(LocationServiceState.Running, viewModel.state.value)

        // And: Service is started with START action
        val shadowApplication = shadowOf(application)
        val startedService = shadowApplication.nextStartedService
        assertEquals(LocationService.ACTION_START, startedService.action)
    }

    @Test
    fun `pauseResumeSimulation from Running pauses simulation and transitions to Paused`() = runTest {
        // Given: Running state
        viewModel.startStop()
        assertEquals(LocationServiceState.Running, viewModel.state.value)
        shadowOf(application).clearStartedServices()

        // When: pauseResumeSimulation is called
        viewModel.pauseResume()

        // Then: State transitions to Paused
        assertEquals(LocationServiceState.Paused, viewModel.state.value)

        // And: Service is started with PAUSE action
        val shadowApplication = shadowOf(application)
        val startedService = shadowApplication.nextStartedService
        assertEquals(LocationService.ACTION_PAUSE, startedService.action)
    }

    @Test
    fun `pauseResumeSimulation from Paused resumes simulation and transitions to Running`() = runTest {
        // Given: Paused state
        viewModel.startStop() // Start
        viewModel.pauseResume() // Pause
        assertEquals(LocationServiceState.Paused, viewModel.state.value)
        shadowOf(application).clearStartedServices()

        // When: pauseResumeSimulation is called again
        viewModel.pauseResume()

        // Then: State transitions to Running
        assertEquals(LocationServiceState.Running, viewModel.state.value)

        // And: Service is started with RESUME action
        val shadowApplication = shadowOf(application)
        val startedService = shadowApplication.nextStartedService
        assertEquals(LocationService.ACTION_RESUME, startedService.action)
    }

    @Test
    fun `pauseResumeSimulation from Idle does not change state`() = runTest {
        // Given: Idle state
        assertEquals(LocationServiceState.Idle, viewModel.state.value)

        // When: pauseResumeSimulation is called
        viewModel.pauseResume()

        // Then: State remains Idle
        assertEquals(LocationServiceState.Idle, viewModel.state.value)

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
        assertEquals(LocationServiceState.Stopped, viewModel.state.value)
        shadowOf(application).clearStartedServices()

        // When: pauseResumeSimulation is called
        viewModel.pauseResume()

        // Then: State remains Stopped
        assertEquals(LocationServiceState.Stopped, viewModel.state.value)

        // And: No service is started
        val shadowApplication = shadowOf(application)
        val startedService = shadowApplication.nextStartedService
        assertEquals(null, startedService)
    }

    @Test
    fun `complete flow - start, pause, resume, stop transitions correctly`() = runTest {
        // Start
        viewModel.startStop()
        assertEquals(LocationServiceState.Running, viewModel.state.value)

        // Pause
        viewModel.pauseResume()
        assertEquals(LocationServiceState.Paused, viewModel.state.value)

        // Resume
        viewModel.pauseResume()
        assertEquals(LocationServiceState.Running, viewModel.state.value)

        // Stop
        viewModel.startStop()
        assertEquals(LocationServiceState.Stopped, viewModel.state.value)
    }

    @Test
    fun `multiple start-stop cycles work correctly`() = runTest {
        // First cycle
        viewModel.startStop()
        assertEquals(LocationServiceState.Running, viewModel.state.value)
        viewModel.startStop()
        assertEquals(LocationServiceState.Stopped, viewModel.state.value)

        // Second cycle
        viewModel.startStop()
        assertEquals(LocationServiceState.Running, viewModel.state.value)
        viewModel.startStop()
        assertEquals(LocationServiceState.Stopped, viewModel.state.value)

        // Third cycle
        viewModel.startStop()
        assertEquals(LocationServiceState.Running, viewModel.state.value)
    }
}

