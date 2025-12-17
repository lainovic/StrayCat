package com.lainovic.tomtom.straycat

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SimulationController {
    private val _state = MutableStateFlow<SimulationState>(SimulationState.Idle)
    val state: StateFlow<SimulationState> = _state

    fun start() {
        if (_state.value == SimulationState.Idle || _state.value == SimulationState.Stopped) {
            _state.value = SimulationState.Started
        }
    }

    fun pause() {
        if (_state.value == SimulationState.Started) {
            _state.value = SimulationState.Paused
        }
    }

    fun resume() {
        if (_state.value == SimulationState.Paused) {
            _state.value = SimulationState.Started
        }
    }

    fun stop() {
        _state.value = SimulationState.Stopped
    }
}