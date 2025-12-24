package com.lainovic.tomtom.straycat.ui.player

import android.location.Location
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.lainovic.tomtom.straycat.domain.service.LocationDataSource
import com.lainovic.tomtom.straycat.domain.service.LocationPlayerServiceFacade
import com.lainovic.tomtom.straycat.domain.service.LocationServiceState
import com.lainovic.tomtom.straycat.domain.service.LocationPlayerServiceStateProvider
import com.lainovic.tomtom.straycat.ui.components.PlayerButtonState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

internal class LocationPlayerViewModel(
    private val service: LocationPlayerServiceFacade,
) : ViewModel() {
    val state: StateFlow<LocationServiceState> = LocationPlayerServiceStateProvider.state

    val startStopButtonState: StateFlow<PlayerButtonState> = state.map { currentState ->
        when (currentState) {
            LocationServiceState.Idle,
            LocationServiceState.Stopped -> PlayerButtonState.Start
            is LocationServiceState.Running,
            is LocationServiceState.Paused -> PlayerButtonState.Stop
            is LocationServiceState.Error -> PlayerButtonState.Retry
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = PlayerButtonState.Start
    )

    val pauseResumeButtonState: StateFlow<PlayerButtonState> = state.map { currentState ->
        when (currentState) {
            is LocationServiceState.Running -> PlayerButtonState.Pause
            is LocationServiceState.Paused -> PlayerButtonState.Resume
            is LocationServiceState.Error -> PlayerButtonState.Retry
            else -> PlayerButtonState.Pause
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = PlayerButtonState.Pause,
    )

    val progress: StateFlow<Float> = state.map { currentState ->
        when (currentState) {
            is LocationServiceState.Running -> currentState.progress
            is LocationServiceState.Paused -> currentState.progress
            else -> 0f
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = 0f
    )

    init {
        Log.d(TAG, "LocationPlayerViewModel created")
    }

    fun start(locations: List<Location>) {
        if (locations.isEmpty()) {
            Log.d(TAG, "start() called with empty locations list, aborting")
            return
        }

        when (state.value) {
            LocationServiceState.Idle,
            LocationServiceState.Stopped,
            is LocationServiceState.Error -> {
                LocationDataSource.update(locations)
                service.start()
                Log.i(TAG, "start() completed")
            }

            is LocationServiceState.Running,
            is LocationServiceState.Paused -> {
                Log.d(TAG, "Service already running or paused, start() call ignored")
                return
            }
        }
    }

    fun stop() {
        when (state.value) {
            is LocationServiceState.Running,
            is LocationServiceState.Paused -> {
                service.stop()
                LocationDataSource.clear()
                Log.i(TAG, "stop() completed")
            }

            LocationServiceState.Idle,
            LocationServiceState.Stopped,
            is LocationServiceState.Error -> {
                Log.d(TAG, "Service already stopped or in error state, stop() call ignored")
                return
            }
        }
    }

    fun pauseResume() {
        Log.d(TAG, "pauseResume() called, current state: ${state.value}")
        when (state.value) {
            is LocationServiceState.Running -> {
                Log.d(TAG, "Pausing service")
                service.pause()
            }

            is LocationServiceState.Paused -> {
                Log.d(TAG, "Resuming service")
                service.resume()
            }

            else -> {
                Log.d(TAG, "pauseResumeSimulation called in invalid state: ${state.value}")
            }
        }
        Log.d(TAG, "pauseResume() completed")
    }

    override fun onCleared() {
        Log.d(TAG, "onCleared() called")
        super.onCleared()
        Log.d(TAG, "onCleared() completed")
    }

    companion object {
        val TAG = LocationPlayerViewModel::class.simpleName

        fun Factory(service: LocationPlayerServiceFacade): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return LocationPlayerViewModel(service) as T
                }
            }
        }
    }
}
