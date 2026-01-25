package com.lainovic.tomtom.straycat.infrastructure.simulation

import com.lainovic.tomtom.straycat.domain.simulation.SimpleSimulationConfigurationManager
import com.lainovic.tomtom.straycat.domain.simulation.SimulationConfigurationManager

/**
 * Singleton instance of the simulation configuration manager.
 * This ensures a single source of truth for simulation configuration across the app.
 */
object SimulationConfigurationManagerSingleton : SimulationConfigurationManager
by SimpleSimulationConfigurationManager()
