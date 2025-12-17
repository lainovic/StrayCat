package com.lainovic.tomtom.straycat

import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Singleton state manager for LocationService state.
 * This is the single source of truth for service state across the app.
 *
 * Using a singleton StateFlow is appropriate here because:
 * 1. There's only one LocationService instance in the app
 * 2. Multiple components may need to observe the same state
 * 3. StateFlow is already designed to be shared and doesn't leak Context
 */
object LocationServiceStateProvider {
    private val TAG = LocationServiceStateProvider::class

    private val _state = MutableStateFlow<LocationServiceState>(LocationServiceState.Idle)
    val state: StateFlow<LocationServiceState> = _state

    /**
     * Called by LocationService to update the current state.
     * This is the only way to modify state - it's write-protected.
     */
    fun updateState(newState: LocationServiceState) {
        Log.d(TAG.simpleName, "State updated: ${_state.value} -> $newState")
        _state.value = newState
    }
}

