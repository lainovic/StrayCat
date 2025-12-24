package com.lainovic.tomtom.straycat.domain.service

import android.location.Location
import android.os.SystemClock
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.retry
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext
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

    private val _progress = MutableStateFlow(0f)
    val progress: StateFlow<Float> = _progress

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
        supervisorScope {
            val snapshot = LocationDataSource.locations.value.map { Location(it) }
            snapshot
                .toFlow()
                .collectLocations()
        }
    }

    private fun List<Location>.toFlow(): Flow<IndexedValue<Location>> = flow {
        this@toFlow.forEachIndexed { idx, location ->
            emit(IndexedValue(idx, location))
        }
    }.buffer(capacity = Channel.RENDEZVOUS)

    private fun postProcess(location: Location): Location {
        val processedLocation = Location(location)
            .apply {
                time = System.currentTimeMillis()
                elapsedRealtimeNanos = SystemClock.elapsedRealtimeNanos()
                accuracy = 10f // good enough for simulation
            }

        return processedLocation
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun Flow<IndexedValue<Location>>.collectLocations() =
        this
            .retry(retries = 3) { cause ->
                delay(1000)
                Log.w(TAG, "Retrying after error", cause)
                true
            }
            .onEach { (idx, _) ->
                _progress.value = (idx + 1f) / LocationDataSource.locations.value.size
                delayOrWaitUntilResumed()
            }
            .map { it.value }
//            .map { postProcess(it) }
            .flatMapConcat { location ->
                flow {
                    val processed = withContext(Dispatchers.Default) {
                        postProcess(location)
                    }
                    emit(processed)
                }
            }
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
