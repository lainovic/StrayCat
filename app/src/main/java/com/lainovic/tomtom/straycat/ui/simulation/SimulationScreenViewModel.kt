package com.lainovic.tomtom.straycat.ui.simulation

import android.location.Location
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.lainovic.tomtom.straycat.domain.location.TrackPoint
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

class SimulationScreenViewModel(
    private val routePlanner: RoutePlanner,
    private val eventBus: SimulationEventBus,
) : ViewModel() {
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _origin = mutableStateOf<Location?>(null)
    val origin: State<Location?> = _origin

    private val _destination = mutableStateOf<Location?>(null)
    val destination: State<Location?> = _destination

    private val _points = mutableStateOf<List<TrackPoint>>(emptyList())
    val points: State<List<TrackPoint>> = _points

    private val _errorMessage = mutableStateOf<String?>(null)
    val errorMessage: State<String?> = _errorMessage

    private val _snackbarMessages = MutableSharedFlow<String>()
    val snackbarMessages: SharedFlow<String> = _snackbarMessages

    init {
        viewModelScope.launch {
            eventBus.events.collect { event ->
                val message = when (event) {
                    is SimulationEvent.Error -> "Simulation error: ${event.message}"
                    is SimulationEvent.RoutePlanned -> "Route planned successfully"
                    is SimulationEvent.OriginSet -> "Origin set to: ${event.location.prettyPrint()}"
                    is SimulationEvent.DestinationSet -> "Destination set to: ${event.location.prettyPrint()}"
                    is SimulationEvent.RouteCleared -> "Route cleared"
                    is SimulationEvent.Started -> "Simulation started"
                    is SimulationEvent.Stopped -> "Simulation stopped"
                    is SimulationEvent.Paused -> "Simulation paused"
                    is SimulationEvent.Resumed -> "Simulation resumed"
                    is SimulationEvent.Initialized -> "Simulation initialized"
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
        eventBus.pushEvent(SimulationEvent.OriginSet(location))
    }

    fun setDestination(location: Location) {
        _destination.value = location
        _points.value = emptyList()
        planRouteIfReady()
        eventBus.pushEvent(SimulationEvent.DestinationSet(location))
    }

    fun clearRoute() {
        _origin.value = null
        _destination.value = null
        _points.value = emptyList()
        eventBus.pushEvent(SimulationEvent.RouteCleared)
    }

    private fun planRouteIfReady() {
        val origin = _origin.value ?: return
        val destination = _destination.value ?: return

        viewModelScope.launch {
            try {
                _isLoading.value = true
                val points = routePlanner.planRoute(origin, destination)
                _points.value = points
                eventBus.pushEvent(SimulationEvent.RoutePlanned)
            } catch (e: Exception) {
                val errorMessage = "Failed to plan route: ${e.message}"
                _errorMessage.value = errorMessage
            } finally {
                _isLoading.value = false
            }
        }
    }

    class Factory(
        private val routePlanner: RoutePlanner,
        private val eventBus: SimulationEventBus,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SimulationScreenViewModel(routePlanner, eventBus) as T
        }
    }
}
