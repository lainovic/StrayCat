package com.lainovic.tomtom.straycat

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SimulationControllerTest {

    @Test
    fun `initial state is Idle`() {
        val controller = SimulationController()
        assertEquals(SimulationState.Idle, controller.state.value)
    }

    @Test
    fun `start from Idle transitions to Started`() {
        val controller = SimulationController()

        controller.start()

        assertEquals(SimulationState.Started, controller.state.value)
    }

    @Test
    fun `stop transitions to Stopped`() {
        val controller = SimulationController()
        controller.start()

        controller.stop()

        assertEquals(SimulationState.Stopped, controller.state.value)
    }

    @Test
    fun `start after stop transitions to Started`() {
        val controller = SimulationController()
        controller.start()
        controller.stop()

        controller.start()

        assertEquals(SimulationState.Started, controller.state.value)
    }

    @Test
    fun `multiple stop-start cycles work correctly`() {
        val controller = SimulationController()

        // First cycle
        controller.start()
        assertEquals(SimulationState.Started, controller.state.value)
        controller.stop()
        assertEquals(SimulationState.Stopped, controller.state.value)

        // Second cycle
        controller.start()
        assertEquals(SimulationState.Started, controller.state.value)
        controller.stop()
        assertEquals(SimulationState.Stopped, controller.state.value)

        // Third cycle
        controller.start()
        assertEquals(SimulationState.Started, controller.state.value)
    }

    @Test
    fun `pause and resume flow works`() {
        val controller = SimulationController()

        controller.start()
        assertEquals(SimulationState.Started, controller.state.value)

        controller.pause()
        assertEquals(SimulationState.Paused, controller.state.value)

        controller.resume()
        assertEquals(SimulationState.Started, controller.state.value)
    }

    @Test
    fun `start when already started does nothing`() {
        val controller = SimulationController()

        controller.start()
        val firstState = controller.state.value
        controller.start() // Should be ignored

        assertEquals(firstState, controller.state.value)
    }

    @Test
    fun `pause when not started does nothing`() {
        val controller = SimulationController()

        controller.pause()

        assertEquals(SimulationState.Idle, controller.state.value)
    }

    @Test
    fun `resume when not paused does nothing`() {
        val controller = SimulationController()

        controller.resume()

        assertEquals(SimulationState.Idle, controller.state.value)
    }

    @Test
    fun `state flow emits all transitions`() = runTest {
        val controller = SimulationController()
        val states = mutableListOf<SimulationState>()

        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            controller.state.toList(states)
        }

        controller.start()
        controller.pause()
        controller.resume()
        controller.stop()
        controller.start()

        job.cancel()

        assertEquals(
            listOf(
                SimulationState.Idle,
                SimulationState.Started,
                SimulationState.Paused,
                SimulationState.Started,
                SimulationState.Stopped,
                SimulationState.Started
            ),
            states
        )
    }
}

