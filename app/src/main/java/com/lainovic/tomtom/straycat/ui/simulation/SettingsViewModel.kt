package com.lainovic.tomtom.straycat.ui.simulation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.lainovic.tomtom.straycat.domain.simulation.SimulationConfiguration
import com.lainovic.tomtom.straycat.domain.simulation.SimulationConfigurationManager
import com.lainovic.tomtom.straycat.domain.simulation.SimulationController
import kotlinx.coroutines.flow.StateFlow

class SettingsViewModel(
    private val configurationManager: SimulationConfigurationManager,
    private val controller: SimulationController
) : ViewModel() {

    val configuration: StateFlow<SimulationConfiguration> = configurationManager.configuration

    fun updateConfiguration(config: SimulationConfiguration) {
        configurationManager.update(config)
        controller.updateConfiguration(config)
    }

    class Factory(
        private val configurationManager: SimulationConfigurationManager,
        private val controller: SimulationController
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SettingsViewModel(configurationManager, controller) as T
        }
    }
}
