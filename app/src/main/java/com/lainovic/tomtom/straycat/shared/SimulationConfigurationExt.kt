package com.lainovic.tomtom.straycat.shared

import com.lainovic.tomtom.straycat.domain.simulation.MutableSimulationConfiguration
import com.lainovic.tomtom.straycat.domain.simulation.SimulationConfiguration

fun SimulationConfiguration.update(
    block: MutableSimulationConfiguration.() -> Unit
): SimulationConfiguration {
    val mutableConfig = this.toMutable()
    mutableConfig.apply(block)
    return mutableConfig.toImmutable()
}

fun SimulationConfiguration.toMutable(): MutableSimulationConfiguration =
    MutableSimulationConfiguration(
        useRealisticTiming = useRealisticTiming,
        delayBetweenEmissions = delayBetweenEmissions,
        distanceBetweenEmissions = distanceBetweenEmissions,
        loopIndefinitely = loopIndefinitely,
        speedMultiplier = speedMultiplier,
        noiseLevelInMeters = noiseLevelInMeters,
    )

fun MutableSimulationConfiguration.toImmutable(): SimulationConfiguration =
    SimulationConfiguration(
        useRealisticTiming = useRealisticTiming,
        delayBetweenEmissions = delayBetweenEmissions,
        distanceBetweenEmissions = distanceBetweenEmissions,
        loopIndefinitely = loopIndefinitely,
        speedMultiplier = speedMultiplier,
        noiseLevelInMeters = noiseLevelInMeters,
    )