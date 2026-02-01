package com.lainovic.tomtom.straycat.domain.simulation

import com.lainovic.tomtom.straycat.domain.location.SimulationPoint
import kotlinx.coroutines.flow.StateFlow

interface SimulationPointProcessor {
    fun process(point: SimulationPoint): SimulationPoint
}

class SimpleSimulationPointProcessor(
    private val configuration: StateFlow<SimulationConfiguration>,
) : SimulationPointProcessor {

    private val bearingProcessor = BearingProcessor()

    override fun process(point: SimulationPoint): SimulationPoint {
        val config = configuration.value
        var processedPoint = bearingProcessor.process(point)

        if (config.noiseLevelInMeters > 0) {
            processedPoint = NoiseProcessor(config.noiseLevelInMeters).process(processedPoint)
        }

        return processedPoint
    }
}

class BearingProcessor : SimulationPointProcessor {
    override fun process(point: SimulationPoint): SimulationPoint {
        return point.copy(location = point.location.withBearing())
    }
}

class NoiseProcessor(
    private val noiseLevelInMeters: Float
) : SimulationPointProcessor {
    override fun process(point: SimulationPoint): SimulationPoint {
        val (latNoise, lonNoise) = NoiseGenerator.generateNoise(noiseLevelInMeters)
        return point.copy(
            location = point.location.apply {
                latitude += latNoise
                longitude += lonNoise
            }
        )
    }
}
