package com.lainovic.tomtom.straycat.domain.service

import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

internal data class SimulationConfiguration(
    val delayBetweenEmissions: Duration = 1000.milliseconds,
    val loopIndefinitely: Boolean = false,
    val speedMultiplier: Float = 1f,
    val noiseLevelInMeters: Float = 0f,
)

internal data class MutableSimulationConfiguration(
    var delayBetweenEmissions: Duration = 1000.milliseconds,
    var loopIndefinitely: Boolean = false,
    var speedMultiplier: Float = 1f,
    var noiseLevelInMeters: Float = 0f,
)