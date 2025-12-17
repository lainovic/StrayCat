package com.lainovic.tomtom.straycat

sealed interface SimulationState {
    object Idle : SimulationState
    object Running : SimulationState
    object Paused : SimulationState
    object Stopped : SimulationState
}


