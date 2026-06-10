package com.lainovic.tomtom.straycat.infrastructure.simulation

import com.lainovic.tomtom.straycat.domain.simulation.InMemoryConfigurationStore
import com.lainovic.tomtom.straycat.domain.simulation.SimulationConfigurationManager
import com.lainovic.tomtom.straycat.domain.simulation.SimulationStateRepository
import com.lainovic.tomtom.straycat.infrastructure.analytics.InMemorySimulationEventBus
import com.lainovic.tomtom.straycat.infrastructure.logging.AndroidLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

object AppGraph {
    val configStore: SimulationConfigurationManager = InMemoryConfigurationStore()
    val stateStore: SimulationStateRepository = InMemorySimulationStateRepository(
        eventBus = InMemorySimulationEventBus,
        backgroundScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
        logger = AndroidLogger
    )
}
