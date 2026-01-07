package com.lainovic.tomtom.straycat.domain.simulation

import android.location.Location

sealed class SimulationEvent {
    object SimulationInitialized : SimulationEvent()

    object SimulationStarted : SimulationEvent()
    object SimulationPaused : SimulationEvent()
    object SimulationResumed : SimulationEvent()
    object SimulationStopped : SimulationEvent()

    data class SimulationProgress(val progress: Float) : SimulationEvent()

    data class OriginSet(val location: Location) : SimulationEvent()
    data class DestinationSet(val location: Location) : SimulationEvent()

    data class SimulationError(val message: String) : SimulationEvent()

    data object RoutePlanned : SimulationEvent()
    data object RouteCleared : SimulationEvent()
}