package com.lainovic.tomtom.straycat

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.launch

class LocationSimulator<T>(
    private val tickerFlow: Flow<T>,
    private val onTick: suspend (T) -> Unit,
    private val onComplete: () -> Unit = {},
    private val backgroundScope: CoroutineScope,
) {
    private val isPaused = MutableStateFlow(false)
    private var collectionJob: Job? = null

    fun start() {
        if (collectionJob?.isActive == true) {
            return
        }

        isPaused.value = false

        collectionJob = backgroundScope.launch {
            collect()
        }
    }

    private suspend fun collect() {
        tickerFlow
            .onCompletion { onComplete() }
            .collect { tick ->
                isPaused.first { !it }
                onTick(tick)
            }
    }

    fun pause() {
        isPaused.value = true
    }

    fun resume() {
        isPaused.value = false
    }

    fun stop() {
        collectionJob?.cancel()
        collectionJob = null
        isPaused.value = false  // Reset state
    }
}