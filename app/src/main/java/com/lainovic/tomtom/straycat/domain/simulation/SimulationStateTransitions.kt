package com.lainovic.tomtom.straycat.domain.simulation

object SimulationStateTransitions {
    fun transition(
        currentState: SimulationState,
        event: SimulationEvent
    ): SimulationState {
        return when (currentState) {
            is SimulationState.Idle -> when (event) {
                is SimulationEvent.SimulationStarted -> SimulationState.Running
                is SimulationEvent.SimulationError -> SimulationState.Error(event.message)
                else -> currentState
            }

            is SimulationState.Running -> when (event) {
                is SimulationEvent.SimulationPaused -> SimulationState.Paused
                is SimulationEvent.SimulationStopped -> SimulationState.Stopped
                is SimulationEvent.SimulationError -> SimulationState.Error(event.message)
                else -> currentState
            }

            is SimulationState.Paused -> when (event) {
                is SimulationEvent.SimulationResumed -> SimulationState.Running
                is SimulationEvent.SimulationStopped -> SimulationState.Stopped
                is SimulationEvent.SimulationError -> SimulationState.Error(event.message)
                else -> currentState
            }

            is SimulationState.Stopped -> when (event) {
                is SimulationEvent.SimulationStarted -> SimulationState.Running
                is SimulationEvent.SimulationError -> SimulationState.Error(event.message)
                else -> currentState
            }

            is SimulationState.Error -> when (event) {
                is SimulationEvent.SimulationStarted -> SimulationState.Running
                else -> currentState
            }
        }
    }
}