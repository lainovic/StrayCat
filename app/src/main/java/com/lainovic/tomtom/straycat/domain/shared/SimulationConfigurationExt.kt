package com.lainovic.tomtom.straycat.domain.shared

import com.lainovic.tomtom.straycat.domain.simulation.SimulationConfiguration
import com.lainovic.tomtom.straycat.infrastructure.location.GpsConfiguration

fun SimulationConfiguration.toGpsConfiguration() = GpsConfiguration(
    minTimeInterval = this.delayBetweenEmissions,
    minDistance = this.distanceBetweenEmissions,
)