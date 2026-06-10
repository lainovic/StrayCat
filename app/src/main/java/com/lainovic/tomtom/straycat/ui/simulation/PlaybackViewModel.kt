package com.lainovic.tomtom.straycat.ui.simulation

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.lainovic.tomtom.straycat.domain.location.TrackPoint
import com.lainovic.tomtom.straycat.domain.logging.Logger
import com.lainovic.tomtom.straycat.domain.simulation.PlaybackCommands
import com.lainovic.tomtom.straycat.domain.simulation.RouteTrackStore
import com.lainovic.tomtom.straycat.domain.simulation.SimulationEvent
import com.lainovic.tomtom.straycat.domain.simulation.SimulationEventBus
import com.lainovic.tomtom.straycat.domain.simulation.SimulationState
import com.lainovic.tomtom.straycat.domain.simulation.SimulationStateRepository
import com.lainovic.tomtom.straycat.infrastructure.analytics.InMemorySimulationEventBus
import com.lainovic.tomtom.straycat.infrastructure.simulation.InMemoryRouteTrackStore
import com.lainovic.tomtom.straycat.infrastructure.simulation.AppGraph
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PlaybackViewModel(
    private val controller: PlaybackCommands,
    private val logger: Logger,
    private val dataRepository: RouteTrackStore,
    stateRepository: SimulationStateRepository,
    eventBus: SimulationEventBus,
) : ViewModel() {
    private val _progress = MutableStateFlow(0.0f)
    val progress: StateFlow<Float> = _progress

    // VM owns state so startStop() can update it synchronously for immediate UI
    // response. stateRepository.state is authoritative; we sync from it so
    // service-driven corrections are reflected.
    private val _simulationState = MutableStateFlow(stateRepository.state.value)
    val simulationState: StateFlow<SimulationState> = _simulationState.asStateFlow()

    init {
        logger.d(TAG, "PlaybackViewModel created")
        viewModelScope.launch {
            stateRepository.state.collect { _simulationState.value = it }
        }
        viewModelScope.launch {
            eventBus.events.collect { event ->
                if (event is SimulationEvent.Progress) {
                    _progress.value = event.progress
                }
            }
        }
    }

    /** Toggle: starts from Idle/Stopped, stops from Running/Paused. */
    @VisibleForTesting
    fun startStop() {
        logger.d(TAG, "startStop() called, state: ${_simulationState.value}")
        when (_simulationState.value) {
            SimulationState.Idle,
            SimulationState.Stopped -> {
                _simulationState.value = SimulationState.Running
                controller.start()
            }
            is SimulationState.Running,
            is SimulationState.Paused -> {
                _simulationState.value = SimulationState.Stopped
                controller.stop()
            }
            else -> logger.d(TAG, "startStop() ignored in state: ${_simulationState.value}")
        }
    }

    fun startPlaying(simulationPoints: List<TrackPoint>) {
        if (simulationPoints.isEmpty()) {
            logger.e(TAG, "Attempted to start simulation with empty points list, the call is ignored")
            return
        }

        logger.d(TAG, "start() called, current state: ${simulationState.value}")
        when (simulationState.value) {
            SimulationState.Idle,
            SimulationState.Stopped,
            is SimulationState.Error -> {
                dataRepository.update(simulationPoints)
                controller.start()
                logger.i(TAG, "start() completed")
            }

            is SimulationState.Running,
            is SimulationState.Paused -> {
                logger.d(TAG, "Service already running or paused, start() call ignored")
                return
            }
        }
    }

    fun stopPlaying() {
        logger.d(TAG, "stop() called, current state: ${simulationState.value}")
        when (simulationState.value) {
            is SimulationState.Running,
            is SimulationState.Paused -> {
                controller.stop()
                dataRepository.clear()
                logger.i(TAG, "stop() completed")
            }

            SimulationState.Stopped -> {
                logger.d(TAG, "Service already stopped, ensuring data is cleared")
                dataRepository.clear()
            }

            SimulationState.Idle -> {
                logger.d(TAG, "Service is idle (never started), nothing to stop")
            }

            is SimulationState.Error -> {
                logger.d(TAG, "Service in error state, clearing data and attempting cleanup")
                controller.stop() // Try to clean up service
                dataRepository.clear()
            }
        }
    }

    fun pauseResume() {
        logger.d(TAG, "PauseResume() called, state: ${_simulationState.value}")
        when (_simulationState.value) {
            is SimulationState.Running -> {
                _simulationState.value = SimulationState.Paused
                controller.pause()
            }
            is SimulationState.Paused -> {
                _simulationState.value = SimulationState.Running
                controller.resume()
            }
            else -> logger.d(TAG, "PauseResume() ignored in state: ${_simulationState.value}")
        }
    }

    override fun onCleared() {
        logger.d(TAG, "onCleared() called")
        super.onCleared()
        logger.d(TAG, "onCleared() completed")
    }

    companion object {
        val TAG = PlaybackViewModel::class.simpleName!!

        fun Factory(
            controller: PlaybackCommands,
            logger: Logger,
        ): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return PlaybackViewModel(
                        controller = controller,
                        logger = logger,
                        dataRepository = InMemoryRouteTrackStore,
                        stateRepository = AppGraph.stateStore,
                        eventBus = InMemorySimulationEventBus,
                    ) as T
                }
            }
        }
    }
}