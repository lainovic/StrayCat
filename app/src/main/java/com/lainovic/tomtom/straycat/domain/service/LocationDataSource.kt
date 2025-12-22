package com.lainovic.tomtom.straycat.domain.service

import android.location.Location
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object LocationDataSource {
    private val _locations = MutableStateFlow<List<Location>>(emptyList())
    val locations: StateFlow<List<Location>> = _locations

    fun update(newLocations: List<Location>) {
        _locations.value = newLocations
    }

    fun clear() {
        _locations.value = emptyList()
    }
}