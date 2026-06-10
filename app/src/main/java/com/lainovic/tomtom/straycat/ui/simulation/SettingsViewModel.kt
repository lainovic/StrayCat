package com.lainovic.tomtom.straycat.ui.simulation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.lainovic.tomtom.straycat.domain.simulation.MutableSimulationConfiguration
import com.lainovic.tomtom.straycat.domain.simulation.SimulationConfiguration
import com.lainovic.tomtom.straycat.domain.simulation.SimulationConfigurationManager
import com.lainovic.tomtom.straycat.infrastructure.simulation.AppGraph
import kotlinx.coroutines.flow.StateFlow

class SettingsViewModel(
    private val configurationManager: SimulationConfigurationManager,
) : ViewModel() {
    val configuration: StateFlow<SimulationConfiguration> = configurationManager.configuration

    fun updateConfiguration(block: MutableSimulationConfiguration.() -> Unit) {
        configurationManager.update(block)
    }

    class Factory : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            SettingsViewModel(AppGraph.configStore) as T
    }
}
