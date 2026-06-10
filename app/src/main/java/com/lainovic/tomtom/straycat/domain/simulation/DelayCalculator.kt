package com.lainovic.tomtom.straycat.domain.simulation

import com.lainovic.tomtom.straycat.domain.location.TrackPoint
import kotlinx.coroutines.flow.StateFlow

interface DelayCalculator {
    fun calculateDelay(idx: Int, point: TrackPoint): Long
}

class RealisticDelayCalculator(
    val configuration: StateFlow<SimulationConfiguration>
) : DelayCalculator {
    private var previousElapsedTime = 0L

    override fun calculateDelay(idx: Int, point: TrackPoint): Long {
        val delay = when {
            !configuration.value.useRealisticTiming -> // fixed delay
                configuration.value.delayBetweenEmissions.inWholeMilliseconds

            idx == 0 -> { // first point, hence no delay
                val currentElapsedTime = point.elapsedTravelTime?.inWholeMilliseconds ?: 0L
                previousElapsedTime = currentElapsedTime
                0L
            }

            else -> {
                val currentElapsedTime = point.elapsedTravelTime?.inWholeMilliseconds ?: 0L
                val delta = if (previousElapsedTime > 0L) {
                    currentElapsedTime - previousElapsedTime
                } else {
                    0L
                }
                previousElapsedTime = currentElapsedTime
                delta
            }
        }

        return (delay / configuration.value.speedMultiplier).toLong()
    }
}