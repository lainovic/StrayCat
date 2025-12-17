package com.lainovic.tomtom.straycat

import android.util.Log
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class TickerLocationService : LocationService<Long>() {
    override fun createLocationFlow(): Flow<Long> {
        Log.d(TAG.simpleName, "createLocationFlow() called")
        return tickerFlow(1_000L).also {
            Log.d(TAG.simpleName, "tickerFlow created")
        }
    }

    companion object {
        val TAG = TickerLocationService::class
    }
}

fun tickerFlow(periodMs: Long) = flow {
    Log.d("TickerFlow", "tickerFlow started with periodMs=$periodMs")
    var tick = 0L
    while (true) {
        Log.d("TickerFlow", "Emitting tick: $tick")
        emit(tick++)
        Log.d("TickerFlow", "Tick emitted, delaying for $periodMs ms")
        delay(periodMs)
    }
}