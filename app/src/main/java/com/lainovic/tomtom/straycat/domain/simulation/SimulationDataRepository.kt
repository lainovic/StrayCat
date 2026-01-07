package com.lainovic.tomtom.straycat.domain.simulation

import com.lainovic.tomtom.straycat.domain.location.SimulationPoint
import kotlinx.coroutines.flow.StateFlow

interface SimulationDataRepository {
    val points: StateFlow<List<SimulationPoint>>

    fun update(newPoints: List<SimulationPoint>)
    fun clear()
    fun isEmpty(): Boolean
    fun size(): Int
    fun snapshot(): List<SimulationPoint>
}
