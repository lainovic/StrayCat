package com.lainovic.tomtom.straycat

import com.lainovic.tomtom.straycat.domain.simulation.LocationSimulator
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LocationSimulatorTest {

    @Test
    fun `pause suspends emission and resume continues from same point`() = runTest {
        val ticks = mutableListOf<Int>()
        val testFlow = flow {
            var tick = 0
            while (tick < 10) {
                emit(tick++)
                delay(1000)
            }
        }

        val tested = LocationSimulator(
            locationFlow = testFlow,
            onTick = { ticks.add(it) },
            backgroundScope = backgroundScope,
        )

        tested.start()

        // Collect tick 0 immediately, then wait for tick 1 after 1000ms
        advanceTimeBy(1_100)
        assertEquals("Should have ticks 0 and 1", listOf(0, 1), ticks)

        // Pause the collection
        tested.pause()

        // Advance time - no new ticks should be collected while paused
        advanceTimeBy(3_000)
        assertEquals("Should still have only ticks 0 and 1 while paused", listOf(0, 1), ticks)

        // Resume collection
        tested.resume()

        // After resume, tick 2 is collected almost immediately because:
        // 1. During the 3000ms pause, the flow's delay(1000) for tick 2 completed
        // 2. The flow tried to emit tick 2, but the collector was suspended (paused)
        // 3. The emit() call in the flow suspends, waiting for the collector to be ready
        // 4. When we resume(), the collector unblocks from isPaused.first { !it }
        // 5. The flow's suspended emit() completes immediately, delivering tick 2
        // This demonstrates backpressure: the producer (flow) waits for the consumer (collector)
        advanceTimeBy(100)
        assertEquals("Tick 2 collected immediately after resume", listOf(0, 1, 2), ticks)

        // Now advance 1000ms for tick 3
        advanceTimeBy(1_000)
        assertEquals("Tick 3 collected after 1000ms", listOf(0, 1, 2, 3), ticks)

        tested.stop()
    }

    @Test
    fun `start stop start restarts flow from beginning`() = runTest {
        val ticks = mutableListOf<Int>()
        val testFlow = flow {
            var tick = 0
            while (tick < 10) {
                emit(tick++)
                delay(1000)
            }
        }

        val tested = LocationSimulator(
            locationFlow = testFlow,
            onTick = { ticks.add(it) },
            backgroundScope = backgroundScope,
        )

        // First start
        tested.start()
        advanceTimeBy(2_000)
        assertEquals("First run should have ticks 0, 1", listOf(0, 1), ticks)

        // Stop
        tested.stop()
        advanceTimeBy(1_000)
        assertEquals("After stop, no new ticks", listOf(0, 1), ticks)

        // Clear and start again
        ticks.clear()
        tested.start()
        advanceTimeBy(2_000)
        assertEquals("Second run should restart from 0", listOf(0, 1), ticks)
    }

    @Test
    fun `multiple start calls are ignored when already running`() = runTest {
        val startCount = mutableListOf<Int>()
        var emitCount = 0
        val testFlow = flow {
            startCount.add(emitCount++)
            while (true) {
                emit(0)
                delay(1000)
            }
        }

        val tested = LocationSimulator(
            locationFlow = testFlow,
            onTick = {},
            backgroundScope = backgroundScope,
        )

        tested.start()
        advanceTimeBy(100)
        tested.start() // Should be ignored
        tested.start() // Should be ignored
        advanceTimeBy(100)

        assertEquals("Flow should only start once", 1, startCount.size)
    }

    @Test
    fun `stop when not started is safe`() = runTest {
        val tested = LocationSimulator(
            locationFlow = flow { emit(1) },
            onTick = {},
            backgroundScope = backgroundScope,
        )

        // Should not crash
        tested.stop()
        tested.stop()
    }

    @Test
    fun `pause resume then stop then start works`() = runTest {
        val ticks = mutableListOf<Int>()
        val testFlow = flow {
            var tick = 0
            while (tick < 10) {
                emit(tick++)
                delay(1000)
            }
        }

        val tested = LocationSimulator(
            locationFlow = testFlow,
            onTick = { ticks.add(it) },
            backgroundScope = backgroundScope,
        )

        tested.start()
        advanceTimeBy(2_000)
        assertEquals(listOf(0, 1), ticks)

        tested.pause()
        advanceTimeBy(1_000)

        tested.resume()
        advanceTimeBy(1_000)
        assertEquals(listOf(0, 1, 2), ticks)

        tested.stop()

        ticks.clear()
        tested.start() // Should restart from 0
        advanceTimeBy(2_000)
        assertEquals("Should restart from beginning", listOf(0, 1), ticks)
    }

    @Test
    fun `three consecutive start-stop cycles`() = runTest {
        val allTicks = mutableListOf<List<Int>>()
        val testFlow = flow {
            var tick = 0
            while (tick < 10) {
                emit(tick++)
                delay(1000)
            }
        }

        val tested = LocationSimulator(
            locationFlow = testFlow,
            onTick = {},
            backgroundScope = backgroundScope,
        )

        // Cycle 1
        val ticks1 = mutableListOf<Int>()
        tested.start()
        advanceTimeBy(2_000)
        tested.stop()
        allTicks.add(ticks1.toList())

        // Cycle 2
        val ticks2 = mutableListOf<Int>()
        tested.start()
        advanceTimeBy(2_000)
        tested.stop()
        allTicks.add(ticks2.toList())

        // Cycle 3
        val ticks3 = mutableListOf<Int>()
        tested.start()
        advanceTimeBy(2_000)
        tested.stop()
        allTicks.add(ticks3.toList())

        // All cycles should work the same
        assertEquals("All cycles should produce same result", 3, allTicks.size)
    }

    @Test
    fun `debug timing test`() = runTest {
        val ticks = mutableListOf<Int>()
        val testFlow = flow {
            var tick = 0
            while (tick < 5) {
                println("Flow: About to emit tick $tick at time ${currentTime}")
                emit(tick++)
                println("Flow: Emitted tick ${tick-1}, starting delay at time ${currentTime}")
                delay(1000)
                println("Flow: Delay completed at time ${currentTime}")
            }
        }

        val tested = LocationSimulator(
            locationFlow = testFlow,
            onTick = {
                println("Collected tick $it at time ${currentTime}")
                ticks.add(it)
            },
            backgroundScope = backgroundScope,
        )

        tested.start()
        advanceTimeBy(1_100)
        println("\nPausing at time ${currentTime}, ticks = $ticks\n")

        tested.pause()
        advanceTimeBy(3_000)
        println("After pause period at time ${currentTime}, ticks = $ticks\n")

        tested.resume()
        println("Resumed at time ${currentTime}\n")

        advanceTimeBy(100)
        println("After 100ms at time ${currentTime}, ticks = $ticks\n")

        advanceTimeBy(900)
        println("After another 900ms at time ${currentTime}, ticks = $ticks\n")

        tested.stop()
    }
}