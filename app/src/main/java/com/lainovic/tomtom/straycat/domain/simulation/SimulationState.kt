package com.lainovic.tomtom.straycat.domain.simulation

sealed class SimulationState {
    object Idle : SimulationState()
    object Running : SimulationState()
    object Paused : SimulationState()
    object Stopped : SimulationState()
    data class Error(val message: String) : SimulationState()

    override fun toString(): String {
        return when (this) {
            is Idle -> "Idle"
            is Running -> "Running"
            is Paused -> "Paused"
            is Stopped -> "Stopped"
            is Error -> "Error(message='$message')"
        }
    }
}