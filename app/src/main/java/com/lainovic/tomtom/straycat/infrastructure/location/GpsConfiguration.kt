package com.lainovic.tomtom.straycat.infrastructure.location

import com.tomtom.quantity.Distance
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

data class GpsConfiguration(
    val minTimeInterval: Duration = 1000.milliseconds,
    val minDistance: Distance = Distance.meters(10.0),
)