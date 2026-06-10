package com.lainovic.tomtom.straycat.domain.simulation

import com.lainovic.tomtom.straycat.domain.location.TrackPoint
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

interface PointTransform {
    fun transform(point: TrackPoint): TrackPoint
}

class PointTransformPipeline(
    private val configuration: StateFlow<SimulationConfiguration>,
    backgroundScope: CoroutineScope,
) : PointTransform {
    val transforms = mutableListOf<PointTransform>()

    init {
        transforms.add(BearingTransform)
        backgroundScope.launch(CoroutineName("PointTransformPipelineConfigObserver")) {
            configuration.collect { config ->
                if (config.noiseLevelInMeters > 0 && transforms.none { it is NoiseTransform }) {
                    transforms.add(NoiseTransform(config.noiseLevelInMeters))
                } else if (config.noiseLevelInMeters == 0f) {
                    transforms.removeAll { it is NoiseTransform }
                }
            }
        }
    }

    override fun transform(point: TrackPoint): TrackPoint {
        val transformedPoint =
            transforms.fold(point) { pt, transform -> transform.transform(pt) }
        return transformedPoint
    }
}

object BearingTransform : PointTransform {
    override fun transform(point: TrackPoint): TrackPoint {
        return point.copy(location = point.location.withBearing())
    }
}

class NoiseTransform(
    private val noiseLevelInMeters: Float
) : PointTransform {
    override fun transform(point: TrackPoint): TrackPoint {
        val (latNoise, lonNoise) = NoiseGenerator.generateNoise(noiseLevelInMeters)
        return point.copy(
            location = point.location.apply {
                latitude += latNoise
                longitude += lonNoise
            }
        )
    }
}
