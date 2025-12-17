package com.lainovic.tomtom.straycat

sealed interface SimulationState {
    object Idle : SimulationState
    object Started : SimulationState
    object Stopped : SimulationState
    object Paused : SimulationState
    object Resumed : SimulationState
}


