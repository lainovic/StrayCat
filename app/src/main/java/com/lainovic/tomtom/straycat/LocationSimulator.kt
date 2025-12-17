package com.lainovic.tomtom.straycat

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.launch

class LocationSimulator<T>(
    private val locationFlow: Flow<T>,
    private val onTick: suspend (T) -> Unit,
    private val onComplete: () -> Unit = {},
    private val backgroundScope: CoroutineScope,
) {
    private val isPaused = MutableStateFlow(false)
    private var collectionJob: Job? = null

    init {
        Log.d(TAG, "LocationSimulator created")
    }

    fun start() {
        Log.d(TAG, "start() called, collectionJob=${collectionJob}, isActive=${collectionJob?.isActive}")
        if (collectionJob?.isActive == true) {
            Log.d(TAG, "Collection job already active, returning")
            return
        }

        Log.d(TAG, "Setting isPaused to false")
        isPaused.value = false

        Log.d(TAG, "Launching collection job in background scope")
        collectionJob = backgroundScope.launch {
            Log.d(TAG, "Collection job coroutine started")
            collect()
        }
        Log.d(TAG, "start() completed, collectionJob=$collectionJob")
    }

    private suspend fun collect() {
        Log.d(TAG, "collect() called")
        try {
            locationFlow
                .onCompletion {
                    Log.d(TAG, "Flow completed")
                    onComplete()
                }
                .collect { tick ->
                    Log.d(TAG, "Received tick: $tick, isPaused=${isPaused.value}")
                    if (isPaused.value) {
                        Log.d(TAG, "Flow is paused, waiting for resume...")
                    }
                    isPaused.first { !it }
                    Log.d(TAG, "Calling onTick callback with: $tick")
                    onTick(tick)
                    Log.d(TAG, "onTick callback completed")
                }
        } catch (e: Exception) {
            Log.e(TAG, "Exception in collect()", e)
            throw e
        }
    }

    fun pause() {
        Log.d(TAG, "pause() called")
        isPaused.value = true
        Log.d(TAG, "isPaused set to true")
    }

    fun resume() {
        Log.d(TAG, "resume() called")
        isPaused.value = false
        Log.d(TAG, "isPaused set to false")
    }

    fun stop() {
        Log.d(TAG, "stop() called, collectionJob=$collectionJob")
        collectionJob?.cancel()
        Log.d(TAG, "Collection job cancelled")
        collectionJob = null
        isPaused.value = false  // Reset state
        Log.d(TAG, "stop() completed")
    }

    companion object {
        private const val TAG = "LocationSimulator"
    }
}