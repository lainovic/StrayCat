package com.lainovic.tomtom.straycat.ui.simulation

import android.location.Location
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.lainovic.tomtom.straycat.infrastructure.shared.planRoute
import com.tomtom.sdk.routing.RoutePlanner
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SimulationViewModel(
    private val routePlanner: RoutePlanner
) : ViewModel() {
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

//    private val _animatedPoints = MutableStateFlow<List<Location>>(emptyList())
//    val animatedPoints: StateFlow<List<Location>> = _animatedPoints

    private val _errorEvents = MutableSharedFlow<String>()
    val errorEvents: SharedFlow<String> = _errorEvents

    private val _origin = mutableStateOf<Location?>(null)
    val origin: State<Location?> = _origin

    private val _destination = mutableStateOf<Location?>(null)
    val destination: State<Location?> = _destination

    private val _points = mutableStateOf<List<Location>>(emptyList())
    val points: State<List<Location>> = _points

    private val _errorMessage = mutableStateOf<String?>(null)
    val errorMessage: State<String?> = _errorMessage

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
//                animateRoute(result)
            } catch (e: Exception) {
                _errorMessage.value = e.message
                _errorEvents.emit(e.message ?: "Unknown error")
            } finally {
                _isLoading.value = false
            }
        }
    }

//    private fun animateRoute(points: List<Location>) {
//        viewModelScope.launch {
//            points.asFlow()
//                .onEach { delay(1) }
//                .scan(emptyList<Location>()) { acc, point -> acc + point }
//                .collect { _animatedPoints.value = it }
//        }
//    }

    companion object {
        fun Factory(routePlanner: RoutePlanner): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return SimulationViewModel(routePlanner) as T
                }
            }
    }
}
