package com.lainovic.tomtom.straycat

import android.location.Location
import android.location.LocationManager
import android.os.SystemClock
import android.util.Log
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow


class MockLocationService : LocationService() {
    override fun observeLocations(): Flow<Location> {
        Log.d(TAG.simpleName, "createLocationFlow() called")
        return flow {
            Log.d(TAG.simpleName, "Start emitting locations...")
            while (true) {
                Log.d(TAG.simpleName, "Emitting location")
                val location = createAmsterdamLocation()
                emit(location)
                delay(1_000L)
            }
        }
    }

    @Suppress("Unused")
    private fun createBelgradeLocation() = Location(LocationManager.GPS_PROVIDER).apply {
        latitude = 44.7866 + (-0.05..0.05).random()
        longitude = 20.4489 + (-0.05..0.05).random()
        accuracy = 5f
        time = System.currentTimeMillis()
        elapsedRealtimeNanos = SystemClock.elapsedRealtimeNanos()
    }

    private fun createAmsterdamLocation() = Location(LocationManager.GPS_PROVIDER).apply {
        latitude = 52.3676 + (-0.05..0.05).random()
        longitude = 4.9041 + (-0.05..0.05).random()
        accuracy = 5f
        time = System.currentTimeMillis()
        elapsedRealtimeNanos = SystemClock.elapsedRealtimeNanos()
    }

    private fun ClosedFloatingPointRange<Double>.random() =
        (endInclusive - start) * Math.random() + start

    companion object {
        val TAG = this::class
    }
}
