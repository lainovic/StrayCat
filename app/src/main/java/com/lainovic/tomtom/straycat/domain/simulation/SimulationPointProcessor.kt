package com.lainovic.tomtom.straycat.domain.simulation

import com.lainovic.tomtom.straycat.domain.location.SimulationPoint
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

interface SimulationPointProcessor {
    fun process(point: SimulationPoint): SimulationPoint
}

class SimpleSimulationPointProcessor(
    private val configuration: StateFlow<SimulationConfiguration>,
    backgroundScope: CoroutineScope,
) : SimulationPointProcessor {
    val processors = mutableListOf<SimulationPointProcessor>()

    init {
        processors.add(BearingProcessor)
        backgroundScope.launch(CoroutineName("SimulationPointProcessorConfigObserver")) {
            configuration.collect { config ->
                if (config.noiseLevelInMeters > 0 && processors.none { it is NoiseProcessor }) {
                    processors.add(NoiseProcessor(config.noiseLevelInMeters))
                } else if (config.noiseLevelInMeters == 0f) {
                    processors.removeAll { it is NoiseProcessor }
                }
            }
        }
    }

    override fun process(point: SimulationPoint): SimulationPoint {
        val processedPoint =
            processors.fold(point) { pt, processor -> processor.process(pt) }
        return processedPoint
    }
}

object BearingProcessor : SimulationPointProcessor {
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
