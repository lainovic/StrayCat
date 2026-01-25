package com.lainovic.tomtom.straycat.ui.simulation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.lainovic.tomtom.straycat.domain.location.SimulationPoint
import com.lainovic.tomtom.straycat.domain.simulation.SimulationController
import com.lainovic.tomtom.straycat.domain.simulation.SimulationDataRepository
import com.lainovic.tomtom.straycat.domain.simulation.SimulationEvent
import com.lainovic.tomtom.straycat.domain.simulation.SimulationEventBus
import com.lainovic.tomtom.straycat.domain.simulation.SimulationState
import com.lainovic.tomtom.straycat.domain.simulation.SimulationStateRepository
import com.lainovic.tomtom.straycat.infrastructure.logging.AndroidLogger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PlaybackViewModel(
    private val controller: SimulationController,
    eventBus: SimulationEventBus,
    private val dataRepository: SimulationDataRepository,
    stateRepository: SimulationStateRepository,
) : ViewModel() {
    private val _progress = MutableStateFlow(0.0f)
    val progress: StateFlow<Float> = _progress
    val simulationState: StateFlow<SimulationState> = stateRepository.state

    init {
        AndroidLogger.d(TAG, "PlaybackViewModel created")
        viewModelScope.launch {
            eventBus.events.collect { event ->
                if (event is SimulationEvent.SimulationProgress) {
                    _progress.value = event.progress
                }
            }
        }
    }

    fun startPlaying(simulationPoints: List<SimulationPoint>) {
        if (simulationPoints.isEmpty()) {
            AndroidLogger.e(TAG, "Attempted to start simulation with empty points list, the call is ignored")
            return
        }

        AndroidLogger.d(TAG, "start() called, current state: ${simulationState.value}")
        when (simulationState.value) {
            SimulationState.Idle,
            SimulationState.Stopped,
            is SimulationState.Error -> {
                dataRepository.update(simulationPoints)
                controller.start()
                AndroidLogger.i(TAG, "start() completed")
            }

            is SimulationState.Running,
            is SimulationState.Paused -> {
                AndroidLogger.d(TAG, "Service already running or paused, start() call ignored")
                return
            }
        }
    }

    fun stopPlaying() {
        AndroidLogger.d(TAG, "stop() called, current state: ${simulationState.value}")
        when (simulationState.value) {
            is SimulationState.Running,
            is SimulationState.Paused -> {
                controller.stop()
                dataRepository.clear()
                AndroidLogger.i(TAG, "stop() completed")
            }

            SimulationState.Stopped -> {
                AndroidLogger.d(TAG, "Service already stopped, ensuring data is cleared")
                dataRepository.clear()
            }

            SimulationState.Idle -> {
                AndroidLogger.d(TAG, "Service is idle (never started), nothing to stop")
            }

            is SimulationState.Error -> {
                AndroidLogger.d(TAG, "Service in error state, clearing data and attempting cleanup")
                controller.stop() // Try to clean up service
                dataRepository.clear()
            }
        }
    }

    fun pauseOrResume() {
        AndroidLogger.d(TAG, "pauseOrResume() called, current state: ${simulationState.value}")
        when (simulationState.value) {
            is SimulationState.Running -> {
                AndroidLogger.d(TAG, "Pausing service")
                controller.pause()
            }

            is SimulationState.Paused -> {
                AndroidLogger.d(TAG, "Resuming service")
                controller.resume()
            }

            else -> {
                AndroidLogger.d(TAG, "pauseOrResume() called in invalid state: ${simulationState.value}")
            }
        }
        AndroidLogger.d(TAG, "pauseResume() completed")
    }

    override fun onCleared() {
        AndroidLogger.d(TAG, "onCleared() called")
        super.onCleared()
        AndroidLogger.d(TAG, "onCleared() completed")
    }

    companion object {
        val TAG = PlaybackViewModel::class.simpleName!!

        fun Factory(
            controller: SimulationController,
            eventBus: SimulationEventBus,
            dataSource: SimulationDataRepository,
            stateRepository: SimulationStateRepository,
        ): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return PlaybackViewModel(controller, eventBus, dataSource, stateRepository) as T
                }
            }
        }
    }
}