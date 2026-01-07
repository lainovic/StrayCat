package com.lainovic.tomtom.straycat.infrastructure.simulation

import com.lainovic.tomtom.straycat.domain.simulation.SimulationEventBus
import com.lainovic.tomtom.straycat.domain.simulation.SimulationState
import com.lainovic.tomtom.straycat.domain.simulation.SimulationStateRepository
import com.lainovic.tomtom.straycat.domain.simulation.SimulationStateTransitions
import com.lainovic.tomtom.straycat.infrastructure.logging.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class InMemorySimulationStateRepository(
    eventBus: SimulationEventBus,
    backgroundScope: CoroutineScope
) : SimulationStateRepository {

    private val _state = MutableStateFlow<SimulationState>(SimulationState.Idle)
    override val state: StateFlow<SimulationState> = _state.asStateFlow()

    init {
        Logger.d(TAG, "InMemorySimulationStateRepository initialized, starting event collection")
        backgroundScope.launch {
            eventBus.events.collect { event ->
                _state.update { currentState ->
                    val newState = SimulationStateTransitions.transition(
                        currentState = currentState,
                        event = event,
                    )
                    Logger.d(
                        TAG,
                        "State transitioned from $currentState to $newState " +
                                "on event $event"
                    )
                    newState
                }
            }
        }
    }

    companion object {
        private val TAG = InMemorySimulationStateRepository::class.simpleName!!
    }
}