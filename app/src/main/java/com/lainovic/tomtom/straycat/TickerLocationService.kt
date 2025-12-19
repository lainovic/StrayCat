package com.lainovic.tomtom.straycat

import android.location.Location
import android.util.Log
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class TickerLocationService : LocationService() {
    override fun observeLocations(): Flow<Location> {
        Log.d(TAG.simpleName, "createLocationFlow() called")
        return tickerFlow(1_000L).also {
            Log.d(TAG.simpleName, "tickerFlow created")
        }
    }

    @Suppress("SameParameterValue")
    private fun tickerFlow(periodMs: Long) = flow {
        Log.d("TickerFlow", "tickerFlow started with periodMs=$periodMs")
        var tick = 0L
        while (true) {
            Log.d("TickerFlow", "Emitting tick: $tick")
            tick += 1
            emit(Location("gps").apply {
                latitude = tick.toDouble()
                longitude = tick.toDouble()
                accuracy = 5f
                time = System.currentTimeMillis()
                elapsedRealtimeNanos = System.nanoTime()
            })
            delay(periodMs)
        }
    }

    companion object {
        val TAG = TickerLocationService::class
    }
}
