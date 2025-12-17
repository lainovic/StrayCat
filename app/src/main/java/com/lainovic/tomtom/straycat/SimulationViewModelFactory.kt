package com.lainovic.tomtom.straycat

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class SimulationViewModelFactory(
    private val application: Application,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SimulationViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SimulationViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}