package com.lainovic.tomtom.straycat.infrastructure.simulation

import com.lainovic.tomtom.straycat.domain.location.SimulationPoint
import com.lainovic.tomtom.straycat.domain.simulation.SimulationDataRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object InMemorySimulationDataRepository : SimulationDataRepository {
    private val _points = MutableStateFlow<List<SimulationPoint>>(emptyList())
    override val points: StateFlow<List<SimulationPoint>> = _points

    override fun update(newPoints: List<SimulationPoint>) {
        _points.value = newPoints
    }

    override fun clear() {
        _points.value = emptyList()
    }

    override fun isEmpty(): Boolean = _points.value.isEmpty()

    override fun size() = _points.value.size

    override fun snapshot(): List<SimulationPoint> {
        return points.value.map {
            SimulationPoint(
                location = it.location,
                elapsedTravelTime = it.elapsedTravelTime,
                speed = it.speed,
            )
        }
    }
}
