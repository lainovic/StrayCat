package com.lainovic.tomtom.straycat.infrastructure.simulation

import com.lainovic.tomtom.straycat.domain.simulation.SimulationStateRepository
import com.lainovic.tomtom.straycat.infrastructure.analytics.InMemorySimulationEventBus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * Singleton instance of the simulation state repository.
 * This ensures a single source of truth for simulation state across the app.
 */
object SimulationStateRepositorySingleton : SimulationStateRepository by InMemorySimulationStateRepository(
    eventBus = InMemorySimulationEventBus,
    backgroundScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
)

