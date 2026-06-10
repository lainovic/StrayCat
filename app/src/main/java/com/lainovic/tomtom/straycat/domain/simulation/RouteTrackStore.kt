package com.lainovic.tomtom.straycat.domain.simulation

import com.lainovic.tomtom.straycat.domain.location.TrackPoint
import kotlinx.coroutines.flow.StateFlow

interface RouteTrackStore {
    val points: StateFlow<List<TrackPoint>>
    fun update(newPoints: List<TrackPoint>)
    fun clear()
    fun isEmpty(): Boolean
    fun size(): Int
    fun snapshot(): List<TrackPoint>
}
