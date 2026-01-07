package com.lainovic.tomtom.straycat.domain.simulation

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow

interface SimulationEventBus {
    val events: SharedFlow<SimulationEvent>
    fun pushEvent(event: SimulationEvent)
}