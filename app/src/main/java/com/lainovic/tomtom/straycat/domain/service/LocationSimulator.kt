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
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException
import kotlin.math.cos
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

class LocationSimulator(
    private val configuration: LocationSimulatorConfiguration = LocationSimulatorConfiguration(),
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
            emit(postProcess(location))
        }
    }

    private fun postProcess(location: Location): Location {
        val processedLocation = Location(location)
            .apply {
                time = System.currentTimeMillis()
                elapsedRealtimeNanos = SystemClock.elapsedRealtimeNanos()
                accuracy = 10f // good enough for simulation
            }

        // TODO apply speed multiplier if needed
        // if (configuration.speedMultiplier != 1f) {}

        // Apply noise if needed
        if (configuration.noiseLevelInMeters > 0f) {
            val (noiseLat, noiseLon) = generateNoise()
            processedLocation.latitude += noiseLat
            processedLocation.longitude += noiseLon
        }

        return processedLocation
    }

    private fun generateNoise(): Pair<Float, Float> = with(configuration) {
        val noiseLat =
            (Math.random().toFloat() - 0.5f) * 2 * noiseLevelInMeters /
                    111320f
        val noiseLon =
            (Math.random().toFloat() - 0.5f) * 2 * noiseLevelInMeters /
                    (111320f * cos(Math.toRadians(0.0))).toFloat()
        return Pair(noiseLat, noiseLon)
    }

    private suspend fun Flow<Location>.collectLocations() =
        this
            .onEach { delayOrWaitUntilResumed() }
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
        private const val TAG = "LocationSimulator"
    }
}

data class LocationSimulatorConfiguration(
    val delayBetweenEmissions: Duration = 1000.milliseconds,
    val loopIndefinitely: Boolean = false,
    val speedMultiplier: Float = 1f,
    val noiseLevelInMeters: Float = 0f,
)