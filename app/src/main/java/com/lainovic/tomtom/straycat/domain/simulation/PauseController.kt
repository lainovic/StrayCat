package com.lainovic.tomtom.straycat.domain.simulation

import com.lainovic.tomtom.straycat.infrastructure.logging.AndroidLogger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first

interface PauseController {
    suspend fun waitIfPaused()
    fun pause()
    fun resume()
    fun resetPause()
}

class SimplePauseController(
    private val eventBus: SimulationEventBus,
) : PauseController {
    private val _isPaused = MutableStateFlow(false)

    override suspend fun waitIfPaused() {
        if (_isPaused.value) {
            _isPaused.first { !it }
        }
    }

    override fun pause() {
        _isPaused.value = true
        AndroidLogger.i(TAG, "Paused")
        eventBus.pushEvent(SimulationEvent.SimulationPaused)
    }

    override fun resume() {
        _isPaused.value = false
        AndroidLogger.i(TAG, "Resumed")
        eventBus.pushEvent(SimulationEvent.SimulationResumed)
    }

    override fun resetPause() {
        _isPaused.value = false
        AndroidLogger.i(TAG, "Reset")
    }

    private companion object {
        val TAG = PauseController::class.simpleName!!
    }
}