package com.lainovic.tomtom.straycat

import java.io.Serializable

sealed class LocationServiceState : Serializable {
    object Idle : LocationServiceState() {
        private fun readResolve(): Any = Idle
    }

    object Running : LocationServiceState() {
        private fun readResolve(): Any = Running
    }

    object Paused : LocationServiceState() {
        private fun readResolve(): Any = Paused
    }

    object Stopped : LocationServiceState() {
        private fun readResolve(): Any = Stopped
    }

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
