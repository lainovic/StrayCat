package com.lainovic.tomtom.straycat.application

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.lainovic.tomtom.straycat.domain.simulation.SimulationEvent
import com.lainovic.tomtom.straycat.infrastructure.analytics.InMemorySimulationEventBus
import com.lainovic.tomtom.straycat.infrastructure.logging.AndroidLogger
import com.lainovic.tomtom.straycat.infrastructure.shared.initPlaces
import com.lainovic.tomtom.straycat.infrastructure.shared.rememberCustomLocationProvider
import com.lainovic.tomtom.straycat.infrastructure.shared.rememberRoutePlanner
import com.lainovic.tomtom.straycat.infrastructure.simulation.AppGraph
import com.lainovic.tomtom.straycat.ui.simulation.SimulationScreen

@Composable
fun StrayCatApp() {
    val context = LocalContext.current.applicationContext
    initPlaces(context)
    val routePlanner = rememberRoutePlanner(context)
    val simulationConfiguration by AppGraph.configStore.configuration.collectAsState()
    val locationProvider = rememberCustomLocationProvider(context, simulationConfiguration)

    Scaffold { paddingValues ->
        SimulationScreen(
            routePlanner = routePlanner,
            locationProvider = locationProvider,
            logger = AndroidLogger,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        )
    }

    InMemorySimulationEventBus.pushEvent(
        SimulationEvent.Initialized
    )
}