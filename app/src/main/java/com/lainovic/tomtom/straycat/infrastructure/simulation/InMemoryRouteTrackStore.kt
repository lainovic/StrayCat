package com.lainovic.tomtom.straycat.infrastructure.simulation

import com.lainovic.tomtom.straycat.domain.location.TrackPoint
import com.lainovic.tomtom.straycat.domain.simulation.RouteTrackStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object InMemoryRouteTrackStore : RouteTrackStore {
    private val _points = MutableStateFlow<List<TrackPoint>>(emptyList())
    override val points: StateFlow<List<TrackPoint>> = _points

    override fun update(newPoints: List<TrackPoint>) {
        _points.value = newPoints
    }

    override fun clear() {
        _points.value = emptyList()
    }

    override fun isEmpty(): Boolean = _points.value.isEmpty()

    override fun size(): Int = _points.value.size

    override fun snapshot(): List<TrackPoint> = _points.value.map { point ->
        point.copy()
    }
}
