package com.lainovic.tomtom.straycat.domain.simulation

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/**
 * Drives a [Flow] with start/stop/pause/resume lifecycle.
 *
 * Pause uses backpressure: the producer suspends at [waitIfPaused] inside
 * [onEach], so the upstream flow halts until [resume] is called.
 * Stop cancels the collection job; the next [start] calls [flowFactory] again,
 * restarting the flow from scratch.
 */
class FlowPlayer<T>(
    private val flowFactory: () -> Flow<T>,
    private val onLocation: suspend (T) -> Unit,
    private val backgroundScope: CoroutineScope,
) {
    private var collectionJob: Job? = null
    private val isPaused = MutableStateFlow(false)

    fun start() {
        if (collectionJob?.isActive == true) return
        isPaused.value = false
        collectionJob = backgroundScope.launch {
            flowFactory()
                .onEach { waitIfPaused() }
                .collect { onLocation(it) }
        }
    }

    fun stop() {
        collectionJob?.cancel()
        collectionJob = null
        isPaused.value = false
    }

    fun pause() {
        isPaused.value = true
    }

    fun resume() {
        isPaused.value = false
    }

    suspend fun restart() {
        collectionJob?.cancelAndJoin()
        collectionJob = null
        isPaused.value = false
        collectionJob = backgroundScope.launch {
            flowFactory()
                .onEach { waitIfPaused() }
                .collect { onLocation(it) }
        }
    }

    private suspend fun waitIfPaused() {
        if (isPaused.value) isPaused.first { !it }
    }
}
