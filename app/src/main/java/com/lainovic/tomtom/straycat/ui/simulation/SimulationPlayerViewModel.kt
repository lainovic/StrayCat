package com.lainovic.tomtom.straycat.ui.simulation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.lainovic.tomtom.straycat.domain.location.SimulationPoint
import com.lainovic.tomtom.straycat.domain.simulation.SimulationDataRepository
import com.lainovic.tomtom.straycat.domain.simulation.SimulationEvent
import com.lainovic.tomtom.straycat.domain.simulation.SimulationEventBus
import com.lainovic.tomtom.straycat.domain.simulation.SimulationState
import com.lainovic.tomtom.straycat.domain.simulation.SimulationStateRepository
import com.lainovic.tomtom.straycat.infrastructure.logging.Logger
import com.lainovic.tomtom.straycat.infrastructure.service.SimulationServiceFacade
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SimulationPlayerViewModel(
    private val service: SimulationServiceFacade,
    eventBus: SimulationEventBus,
    private val dataRepository: SimulationDataRepository,
    stateRepository: SimulationStateRepository,
) : ViewModel() {
    private val _progress = MutableStateFlow(0.0f)
    val progress: StateFlow<Float> = _progress
    val simulationState: StateFlow<SimulationState> = stateRepository.state

    init {
        Logger.d(TAG, "SimulationPlayerViewModel created")
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
            Logger.e(TAG, "Attempted to start simulation with empty points list, the call is ignored")
            return
        }

        Logger.d(TAG, "start() called, current state: ${simulationState.value}")
        when (simulationState.value) {
            SimulationState.Idle,
            SimulationState.Stopped,
            is SimulationState.Error -> {
                dataRepository.update(simulationPoints)
                service.start()
                Logger.i(TAG, "start() completed")
            }

            is SimulationState.Running,
            is SimulationState.Paused -> {
                Logger.d(TAG, "Service already running or paused, start() call ignored")
                return
            }
        }
    }

    fun stopPlaying() {
        Logger.d(TAG, "stop() called, current state: ${simulationState.value}")
        when (simulationState.value) {
            is SimulationState.Running,
            is SimulationState.Paused -> {
                service.stop()
                dataRepository.clear()
                Logger.i(TAG, "stop() completed")
            }

            SimulationState.Stopped -> {
                Logger.d(TAG, "Service already stopped, ensuring data is cleared")
                dataRepository.clear()
            }

            SimulationState.Idle -> {
                Logger.d(TAG, "Service is idle (never started), nothing to stop")
            }

            is SimulationState.Error -> {
                Logger.d(TAG, "Service in error state, clearing data and attempting cleanup")
                service.stop() // Try to clean up service
                dataRepository.clear()
            }
        }
    }

    fun pauseOrResume() {
        Logger.d(TAG, "pauseOrResume() called, current state: ${simulationState.value}")
        when (simulationState.value) {
            is SimulationState.Running -> {
                Logger.d(TAG, "Pausing service")
                service.pause()
            }

            is SimulationState.Paused -> {
                Logger.d(TAG, "Resuming service")
                service.resume()
            }

            else -> {
                Logger.d(TAG, "pauseOrResume() called in invalid state: ${simulationState.value}")
            }
        }
        Logger.d(TAG, "pauseResume() completed")
    }

    override fun onCleared() {
        Logger.d(TAG, "onCleared() called")
        super.onCleared()
        Logger.d(TAG, "onCleared() completed")
    }

    companion object {
        val TAG = SimulationPlayerViewModel::class.simpleName!!

        fun Factory(
            service: SimulationServiceFacade,
            eventBus: SimulationEventBus,
            dataSource: SimulationDataRepository,
            stateRepository: SimulationStateRepository,
        ): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return SimulationPlayerViewModel(service, eventBus, dataSource, stateRepository) as T
                }
            }
        }
    }
}