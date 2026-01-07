package com.lainovic.tomtom.straycat.domain.simulation

import com.lainovic.tomtom.straycat.domain.location.SimulationPoint
import kotlinx.coroutines.flow.StateFlow

interface DelayCalculator {
    fun calculateDelay(idx: Int, point: SimulationPoint): Long
}

class SimpleDelayCalculator(
    val configuration: StateFlow<SimulationConfiguration>
) : DelayCalculator {
    private var previousElapsedTime = 0L

    override fun calculateDelay(idx: Int, point: SimulationPoint): Long {
        return when {
            !configuration.value.useRealisticTiming ->
                configuration.value.delayBetweenEmissions.inWholeMilliseconds

            idx == 0 -> 0L

            else -> {
                val currentElapsedTime = point.elapsedTravelTime?.inWholeMilliseconds ?: 0L
                val delta = currentElapsedTime - previousElapsedTime
                previousElapsedTime = currentElapsedTime
                delta
            }
        }
    }
}