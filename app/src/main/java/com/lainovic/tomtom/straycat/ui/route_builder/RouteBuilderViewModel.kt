package com.lainovic.tomtom.straycat.ui.route_builder

import android.location.Location
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.lainovic.tomtom.straycat.infrastructure.shared.planRoute
import com.tomtom.sdk.routing.RoutePlanner
import kotlinx.coroutines.launch

class RouteBuilderViewModel(
    private val routePlanner: RoutePlanner
) : ViewModel() {

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
            try {
                val result = routePlanner.planRoute(origin, destination)
                _points.value = result
            } catch (e: Exception) {
                _errorMessage.value = e.message
            }
        }
    }

    companion object {
        fun Factory(routePlanner: RoutePlanner): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return RouteBuilderViewModel(routePlanner) as T
                }
            }
    }
}
