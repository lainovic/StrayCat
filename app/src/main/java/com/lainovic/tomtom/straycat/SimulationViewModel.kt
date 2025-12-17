package com.lainovic.tomtom.straycat

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class SimulationViewModel(
    private val service: LocationServiceFacade,
) : ViewModel() {
    val state: StateFlow<LocationServiceState>
        = LocationServiceStateProvider.state

    val startStopButtonText: StateFlow<String> = state.map { currentState ->
        when (currentState) {
            LocationServiceState.Idle, LocationServiceState.Stopped -> "Start"
            LocationServiceState.Running, LocationServiceState.Paused -> "Stop"
            is LocationServiceState.Error -> "Retry"
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = "Start"
    )

    val pauseResumeButtonText: StateFlow<String> = state.map { currentState ->
        when (currentState) {
            LocationServiceState.Running -> "Pause"
            LocationServiceState.Paused -> "Resume"
            is LocationServiceState.Error -> "Error"
            else -> "Pause/Resume"
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = "Pause/Resume"
    )

    init {
        Log.d(TAG.simpleName, "LocationPlayerViewModel created")
    }

    fun startStop() {
        Log.d(TAG.simpleName, "startStop() called, current state: ${state.value}")
        when (state.value) {
            LocationServiceState.Idle,
            LocationServiceState.Stopped,
            is LocationServiceState.Error -> {
                Log.d(TAG.simpleName, "Starting service")
                service.start()
            }
            LocationServiceState.Running,
            LocationServiceState.Paused -> {
                Log.d(TAG.simpleName, "Stopping service")
                service.stop()
            }
        }
        Log.d(TAG.simpleName, "startStop() completed")
    }

    fun pauseResume() {
        Log.d(TAG.simpleName, "pauseResume() called, current state: ${state.value}")
        when (state.value) {
            LocationServiceState.Running -> {
                Log.d(TAG.simpleName, "Pausing service")
                service.pause()
            }
            LocationServiceState.Paused -> {
                Log.d(TAG.simpleName, "Resuming service")
                service.resume()
            }
            else -> {
                Log.d(TAG.simpleName, "pauseResumeSimulation called in invalid state: ${state.value}")
            }
        }
        Log.d(TAG.simpleName, "pauseResume() completed")
    }

    override fun onCleared() {
        Log.d(TAG.simpleName, "onCleared() called")
        super.onCleared()
        // No cleanup needed - facade just observes singleton state
        Log.d(TAG.simpleName, "onCleared() completed")
    }

    private companion object {
        val TAG = SimulationViewModel::class
    }
}