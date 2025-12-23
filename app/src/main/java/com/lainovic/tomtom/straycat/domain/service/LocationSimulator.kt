package com.lainovic.tomtom.straycat.domain.service

import android.location.Location
import android.os.SystemClock
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

internal class LocationSimulator(
    private val configuration: SimulationConfiguration = SimulationConfiguration(),
    private val onTick: suspend (Location) -> Unit,
    private val onComplete: () -> Unit = {},
    private val onError: (Throwable) -> Unit = {},
    private val backgroundScope: CoroutineScope,
) {
    private val isPaused = MutableStateFlow(false)
    private var collectionJob: Job? = null

    fun start() {
        Log.d(TAG, "start() called, collectionJob=${collectionJob}")
        if (collectionJob?.isActive == true) {
            Log.d(TAG, "Collection job already active, returning")
            return
        }

        isPaused.value = false
        collectionJob = backgroundScope.launch {
            Log.d(TAG, "Start job coroutine started")
            runSimulation()
        }

        Log.i(TAG, "start() completed")
        Log.d(TAG, "collectionJob=$collectionJob")
    }

    private suspend fun runSimulation() {
        val snapshot = LocationDataSource.locations.value.map { Location(it) }
        snapshot
            .toFlow()
            .collectLocations()
    }

    private fun List<Location>.toFlow(): Flow<Location> = flow {
        for (location in this@toFlow) {
            emit(location)
        }
    }

    private fun postProcess(location: Location): Location {
        val processedLocation = Location(location)
            .apply {
                time = System.currentTimeMillis()
                elapsedRealtimeNanos = SystemClock.elapsedRealtimeNanos()
                accuracy = 10f // good enough for simulation
            }

        // TODO apply speed multiplier i
        // if (configuration.speedMultiplier != 1f) {}

        return processedLocation
    }

    private suspend fun Flow<Location>.collectLocations() =
        this
            .onEach { delayOrWaitUntilResumed() }
            .map { postProcess(it) }
            .catch { e ->
                Log.e(TAG, "Error while collecting flow", e)
                onError(e)
            }
            .onCompletion {
                Log.d(
                    TAG, "Flow " +
                            if (it is CancellationException)
                                "cancelled"
                            else
                                "completed"
                )
                onComplete()
            }
            .collect { location ->
                Log.i(TAG, "Received location: $location")
                onTick(location)
            }

    private suspend fun delayOrWaitUntilResumed() {
        if (isPaused.value) {
            Log.d(TAG, "Waiting until resumed...")
            isPaused.first { !it }
            Log.d(TAG, "Resumed from pause")
        } else {
            Log.d(TAG, "Delaying for ${configuration.delayBetweenEmissions}")
            delay(
                configuration
                    .delayBetweenEmissions
                    .inWholeMilliseconds
            )
        }
    }

    fun pause() {
        isPaused.value = true
        Log.i(TAG, "Paused")
    }

    fun resume() {
        isPaused.value = false
        Log.i(TAG, "Resumed")
    }

    fun stop() {
        Log.d(TAG, "stop() called, collectionJob=$collectionJob")
        collectionJob?.cancel()
        collectionJob = null
        isPaused.value = false  // Reset state
        Log.i(TAG, "stop() completed")
    }

    companion object {
        private val TAG = LocationSimulator::class.simpleName
    }
}
