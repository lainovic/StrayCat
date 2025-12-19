package com.lainovic.tomtom.straycat

sealed class LocationServiceState {
    object Idle : LocationServiceState()
    object Running : LocationServiceState()
    object Paused : LocationServiceState()
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
