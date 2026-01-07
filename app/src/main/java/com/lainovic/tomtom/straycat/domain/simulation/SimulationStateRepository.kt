package com.lainovic.tomtom.straycat.domain.simulation

import kotlinx.coroutines.flow.StateFlow

interface SimulationStateRepository {
    val state: StateFlow<SimulationState>
}
