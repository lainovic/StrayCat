package com.lainovic.tomtom.straycat.domain.simulation

import androidx.compose.runtime.Stable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow

@Stable
interface SimulationEventBus {
    val events: SharedFlow<SimulationEvent>
    fun pushEvent(event: SimulationEvent)
}