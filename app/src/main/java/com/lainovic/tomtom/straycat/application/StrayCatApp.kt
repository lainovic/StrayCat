package com.lainovic.tomtom.straycat.application

import android.content.Context
import androidx.compose.runtime.Composable
import com.lainovic.tomtom.straycat.domain.simulation.SimulationConfiguration
import com.lainovic.tomtom.straycat.domain.simulation.SimulationEvent
import com.lainovic.tomtom.straycat.infrastructure.analytics.InMemorySimulationEventBus
import com.lainovic.tomtom.straycat.infrastructure.shared.initPlaces
import com.lainovic.tomtom.straycat.infrastructure.shared.rememberCustomLocationProvider
import com.lainovic.tomtom.straycat.infrastructure.shared.rememberRoutePlanner
import com.lainovic.tomtom.straycat.infrastructure.simulation.SimulationConfigurationManagerSingleton
import com.lainovic.tomtom.straycat.ui.simulation.SimulationScreen
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

@Composable
fun StrayCatApp(context: Context) {
    initPlaces(context)
    val routePlanner = rememberRoutePlanner(context)
    val simulationConfiguration by SimulationConfigurationManagerSingleton.configuration.collectAsState()
    val locationProvider = rememberCustomLocationProvider(context, simulationConfiguration)

    SimulationScreen(
        context = context,
        routePlanner = routePlanner,
        locationProvider = locationProvider,
    )

    InMemorySimulationEventBus.pushEvent(
        SimulationEvent.SimulationInitialized
    )
}