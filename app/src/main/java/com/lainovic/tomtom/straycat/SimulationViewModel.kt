package com.lainovic.tomtom.straycat

import android.app.Application
import android.content.Intent
import android.util.Log
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SimulationViewModel(
    private val application: Application,
) : ViewModel() {
    private val _state = MutableStateFlow<SimulationState>(SimulationState.Idle)
    val state: StateFlow<SimulationState> = _state

    fun startStopSimulation() {
        when (_state.value) {
            SimulationState.Idle, SimulationState.Stopped -> startSimulation()
            SimulationState.Running -> stopSimulation()
            SimulationState.Paused -> stopSimulation()
        }
    }

    fun pauseResumeSimulation() {
        when (_state.value) {
            SimulationState.Running -> pauseSimulation()
            SimulationState.Paused -> resumeSimulation()
            else -> { /* No-op */
                Log.d(
                    TAG.simpleName,
                    "pauseResumeSimulation called in invalid state: ${_state.value}"
                )
            }
        }
    }

    private fun startSimulation() {
        val intent = Intent(application, LocationSimulationService::class.java)
            .setAction(LocationSimulationService.ACTION_START)
        application.startForegroundService(intent)
        _state.value = SimulationState.Running
    }

    private fun pauseSimulation() {
        val intent = Intent(application, LocationSimulationService::class.java)
            .setAction(LocationSimulationService.ACTION_PAUSE)
        application.startForegroundService(intent)
        _state.value = SimulationState.Paused
    }

    private fun resumeSimulation() {
        val intent = Intent(application, LocationSimulationService::class.java)
            .setAction(LocationSimulationService.ACTION_RESUME)
        application.startForegroundService(intent)
        _state.value = SimulationState.Running
    }

    private fun stopSimulation() {
        val intent = Intent(application, LocationSimulationService::class.java)
            .setAction(LocationSimulationService.ACTION_STOP)
        application.startForegroundService(intent)
        _state.value = SimulationState.Stopped
    }

    private companion object {
        val TAG = SimulationViewModel::class
    }
}