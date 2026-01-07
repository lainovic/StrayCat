package com.lainovic.tomtom.straycat.domain.simulation

import com.lainovic.tomtom.straycat.infrastructure.logging.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

interface ProgressTracker {
    val progress: StateFlow<Float>
    fun updateProgress(current: Int)
    fun resetProgress()
    fun setSize(size: Int)
}

class SimpleProgressTracker(
    private val eventBus: SimulationEventBus,
) : ProgressTracker {
    private val _progress = MutableStateFlow(0f)
    override val progress: StateFlow<Float> = _progress
    private var size = 0

    override fun updateProgress(current: Int) {
        require(size > 0 && current <= size) {
            "Invalid progress update: current=$current, size=$size"
        }
        Logger.d(TAG, "updateProgress() called with: $current/$size")

        _progress.value = current.toFloat() / size
        eventBus.pushEvent(SimulationEvent.SimulationProgress(_progress.value))
    }

    override fun resetProgress() {
        _progress.value = 0f
    }

    override fun setSize(size: Int) {
        this.size = size
    }

    companion object {
        private val TAG = SimpleProgressTracker::class.simpleName!!
    }
}