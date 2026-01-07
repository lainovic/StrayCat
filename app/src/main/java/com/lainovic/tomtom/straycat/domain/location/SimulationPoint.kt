package com.lainovic.tomtom.straycat.domain.location

import android.location.Location
import kotlin.time.Duration

data class SimulationPoint(
    val location: Location,
    val elapsedTravelTime: Duration? = null,
    val speed: Double?,
)