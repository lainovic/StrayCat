package com.lainovic.tomtom.straycat.infrastructure.analytics

import com.lainovic.tomtom.straycat.domain.simulation.SimulationEvent
import com.lainovic.tomtom.straycat.domain.simulation.SimulationEventBus
import com.lainovic.tomtom.straycat.infrastructure.logging.AndroidLogger
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

object InMemorySimulationEventBus : SimulationEventBus {
    private val _events = MutableSharedFlow<SimulationEvent>(
        replay = 0,
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    override val events: SharedFlow<SimulationEvent> = _events

    override fun pushEvent(event: SimulationEvent) {
        AndroidLogger.d(TAG, "Pushing event: $event")
        val emitted = _events.tryEmit(event)
        if (!emitted) {
            AndroidLogger.w(TAG, "Failed to emit event (buffer full): $event")
        }
    }

    private val TAG = InMemorySimulationEventBus::class.simpleName!!
}