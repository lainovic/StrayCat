package com.lainovic.tomtom.straycat.domain.simulation

import android.location.Location

sealed class SimulationEvent {
    object Initialized : SimulationEvent()

    object Started : SimulationEvent()
    object Paused : SimulationEvent()
    object Resumed : SimulationEvent()
    object Stopped : SimulationEvent()

    data class Progress(val progress: Float) : SimulationEvent()

    data class OriginSet(val location: Location) : SimulationEvent()
    data class DestinationSet(val location: Location) : SimulationEvent()

    data class Error(val message: String) : SimulationEvent()

    data object RoutePlanned : SimulationEvent()
    data object RouteCleared : SimulationEvent()
}