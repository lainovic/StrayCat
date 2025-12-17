package com.lainovic.tomtom.straycat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class SimulationViewModelFactory(
    private val service: LocationServiceFacade,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SimulationViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SimulationViewModel(service) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}