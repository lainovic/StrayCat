package com.lainovic.tomtom.straycat.domain.simulation

import com.lainovic.tomtom.straycat.shared.toImmutable
import com.lainovic.tomtom.straycat.shared.toMutable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

interface SimulationConfigurationManager {
    val configuration: StateFlow<SimulationConfiguration>
    fun update(newConfig: SimulationConfiguration)
    fun update(block: (MutableSimulationConfiguration) -> Unit)
}

class SimpleSimulationConfigurationManager(
    initialConfiguration: SimulationConfiguration = SimulationConfiguration()
) : SimulationConfigurationManager {
    private val _configuration = MutableStateFlow(initialConfiguration)
    override val configuration: StateFlow<SimulationConfiguration> = _configuration

    override fun update(newConfig: SimulationConfiguration) {
        _configuration.value = newConfig
    }

    override fun update(block: (MutableSimulationConfiguration) -> Unit) {
        _configuration.update { currentConfig ->
            currentConfig
                .toMutable()
                .apply(block)
                .toImmutable()
        }
    }
}
