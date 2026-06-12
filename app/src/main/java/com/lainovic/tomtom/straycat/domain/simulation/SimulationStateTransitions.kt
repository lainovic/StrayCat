package com.lainovic.tomtom.straycat.domain.simulation

object SimulationStateTransitions {
    fun transition(
        currentState: SimulationState,
        event: SimulationEvent
    ): SimulationState {
        return when (currentState) {
            is SimulationState.Idle -> when (event) {
                is SimulationEvent.Started -> SimulationState.Running
                is SimulationEvent.Error -> SimulationState.Error(event.message)
                else -> currentState
            }

            is SimulationState.Running -> when (event) {
                is SimulationEvent.Paused -> SimulationState.Paused
                is SimulationEvent.Stopped -> SimulationState.Stopped
                is SimulationEvent.Error -> SimulationState.Error(event.message)
                is SimulationEvent.RouteCleared -> SimulationState.Idle
                else -> currentState
            }

            is SimulationState.Paused -> when (event) {
                is SimulationEvent.Resumed -> SimulationState.Running
                is SimulationEvent.Stopped -> SimulationState.Stopped
                is SimulationEvent.Error -> SimulationState.Error(event.message)
                is SimulationEvent.RouteCleared -> SimulationState.Idle
                else -> currentState
            }

            is SimulationState.Stopped -> when (event) {
                is SimulationEvent.Started -> SimulationState.Running
                is SimulationEvent.Error -> SimulationState.Error(event.message)
                is SimulationEvent.RouteCleared -> SimulationState.Idle
                else -> currentState
            }

            is SimulationState.Error -> when (event) {
                is SimulationEvent.Started -> SimulationState.Running
                is SimulationEvent.RouteCleared -> SimulationState.Idle
                else -> currentState
            }
        }
    }
}