package com.lainovic.tomtom.straycat.application

import android.content.Context
import androidx.compose.runtime.Composable
import com.lainovic.tomtom.straycat.domain.simulation.SimulationEvent
import com.lainovic.tomtom.straycat.infrastructure.analytics.InMemorySimulationEventBus
import com.lainovic.tomtom.straycat.infrastructure.shared.initPlaces
import com.lainovic.tomtom.straycat.infrastructure.shared.rememberCustomLocationProvider
import com.lainovic.tomtom.straycat.infrastructure.shared.rememberRoutePlanner
import com.lainovic.tomtom.straycat.ui.simulation.SimulationScreen

@Composable
fun StrayCatApp(context: Context) {
    initPlaces(context)
    val routePlanner = rememberRoutePlanner(context)
    val locationProvider = rememberCustomLocationProvider(context = context)

    SimulationScreen(
        context = context,
        routePlanner = routePlanner,
        locationProvider = locationProvider,
    )

    InMemorySimulationEventBus.pushEvent(
        SimulationEvent.SimulationInitialized
    )
}