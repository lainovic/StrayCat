package com.lainovic.tomtom.straycat.ui.simulation

import android.location.Location
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.lainovic.tomtom.straycat.domain.location.SimulationPoint
import com.lainovic.tomtom.straycat.domain.simulation.SimulationEvent
import com.lainovic.tomtom.straycat.domain.simulation.SimulationEventBus
import com.lainovic.tomtom.straycat.infrastructure.shared.planRoute
import com.lainovic.tomtom.straycat.ui.prettyPrint
import com.tomtom.sdk.routing.RoutePlanner
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SimulationViewModel(
    private val routePlanner: RoutePlanner,
    private val eventBus: SimulationEventBus,
) : ViewModel() {
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorEvents = MutableSharedFlow<String>()
    val errorEvents: SharedFlow<String> = _errorEvents

    private val _origin = mutableStateOf<Location?>(null)
    val origin: State<Location?> = _origin

    private val _destination = mutableStateOf<Location?>(null)
    val destination: State<Location?> = _destination

    private val _points = mutableStateOf<List<SimulationPoint>>(emptyList())
    val points: State<List<SimulationPoint>> = _points

    private val _errorMessage = mutableStateOf<String?>(null)
    val errorMessage: State<String?> = _errorMessage

    private val _snackbarMessages = MutableSharedFlow<String>()
    val snackbarMessages: SharedFlow<String> = _snackbarMessages

    init {
        viewModelScope.launch {
            eventBus.events.collect { event ->
                val message = when (event) {
                    is SimulationEvent.SimulationError -> "Simulation error: ${event.message}"
                    is SimulationEvent.RoutePlanned -> "Route planned successfully"
                    is SimulationEvent.OriginSet -> "Origin set to: ${event.location.prettyPrint()}"
                    is SimulationEvent.DestinationSet -> "Destination set to: ${event.location.prettyPrint()}"
                    is SimulationEvent.RouteCleared -> "Route cleared"
                    is SimulationEvent.SimulationStarted -> "Simulation started"
                    is SimulationEvent.SimulationStopped -> "Simulation stopped"
                    is SimulationEvent.SimulationPaused -> "Simulation paused"
                    is SimulationEvent.SimulationResumed -> "Simulation resumed"
                    is SimulationEvent.SimulationInitialized -> "Simulation initialized"
                    else -> null
                }
                message?.let { _snackbarMessages.emit(it) }
            }
        }
    }

    fun setOrigin(location: Location) {
        _origin.value = location
        _points.value = emptyList()
        planRouteIfReady()
    }

    fun setDestination(location: Location) {
        _destination.value = location
        planRouteIfReady()
    }

    fun clearRoute() {
        _origin.value = null
        _destination.value = null
        _points.value = emptyList()
    }

    private fun planRouteIfReady() {
        val origin = _origin.value ?: return
        val destination = _destination.value ?: return

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = routePlanner.planRoute(origin, destination)
                _points.value = result
                eventBus.pushEvent(SimulationEvent.RoutePlanned)
            } catch (e: Exception) {
                _errorMessage.value = e.message
                _errorEvents.emit(e.message ?: "Unknown error")
            } finally {
                _isLoading.value = false
            }
        }
    }

    companion object {
        fun Factory(
            routePlanner: RoutePlanner,
            eventBus: SimulationEventBus
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return SimulationViewModel(routePlanner, eventBus) as T
                }
            }
    }
}
