package com.lainovic.tomtom.straycat.domain.service

sealed class LocationServiceState {
    object Idle : LocationServiceState()
    data class Running(val progress: Float = 0f) : LocationServiceState()
    data class Paused(val progress: Float = 0f) : LocationServiceState()
    object Stopped : LocationServiceState()
    data class Error(val message: String) : LocationServiceState()

    override fun toString(): String {
        return when (this) {
            is Idle -> "Idle"
            is Running -> "Running"
            is Paused -> "Paused"
            is Stopped -> "Stopped"
            is Error -> "Error(message='$message')"
        }
    }
}