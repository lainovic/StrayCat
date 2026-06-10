package com.lainovic.tomtom.straycat.domain.simulation

import android.location.Location
import com.lainovic.tomtom.straycat.domain.location.TrackPoint
import com.lainovic.tomtom.straycat.domain.logging.Logger
import com.lainovic.tomtom.straycat.infrastructure.analytics.InMemorySimulationEventBus
import com.lainovic.tomtom.straycat.infrastructure.simulation.InMemoryRouteTrackStore
import com.lainovic.tomtom.straycat.shared.toLocation
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.withIndex
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class LocationSimulator(
    configuration: SimulationConfiguration = SimulationConfiguration(),
    private val onLocation: suspend (Location) -> Unit,
    private val onComplete: () -> Unit = {},
    private val onError: (Throwable) -> Unit = {},
    private val backgroundScope: CoroutineScope,
    private val logger: Logger,
    private val configManager: SimulationConfigurationManager =
        InMemoryConfigurationStore(configuration),
    private val delayCalculator: RealisticDelayCalculator = RealisticDelayCalculator(
        configManager.configuration,
    ),
    private val simulationPointProcessor: PointTransform = PointTransformPipeline(
        configuration = configManager.configuration,
        backgroundScope = backgroundScope,
    ),
    private val dataRepository: RouteTrackStore = InMemoryRouteTrackStore,
    private val eventBus: SimulationEventBus = InMemorySimulationEventBus,
    private val progressTracker: ProgressTracker = EventBusProgressTracker(eventBus, logger),
) :
    SimulationConfigurationManager by configManager,
    PointTransform by simulationPointProcessor,
    ProgressTracker by progressTracker,
    DelayCalculator by delayCalculator {

    private val player = FlowPlayer(
        flowFactory = ::buildSimulationFlow,
        onLocation = onLocation,
        backgroundScope = backgroundScope,
    )

    @Volatile
    private var startIndex = 0
    private val seekMutex = Mutex()

    init {
        backgroundScope.launch(CoroutineName("LocationSimulatorConfigObserver")) {
            this@LocationSimulator.configuration
                .drop(1)
                .collect { newConfig ->
                    logger.d(TAG, "Configuration changed: $newConfig")
                }
        }
    }

    fun start() {
        logger.d(TAG, "start() called")
        player.start()
        eventBus.pushEvent(SimulationEvent.Started)
        logger.i(TAG, "start() completed")
    }

    fun stop() {
        logger.d(TAG, "stop() called")
        player.stop()
        eventBus.pushEvent(SimulationEvent.Stopped)
    }

    fun pause() {
        player.pause()
        logger.i(TAG, "Paused")
        eventBus.pushEvent(SimulationEvent.Paused)
    }

    fun resume() {
        player.resume()
        logger.i(TAG, "Resumed")
        eventBus.pushEvent(SimulationEvent.Resumed)
    }

    suspend fun seekTo(fraction: Float) = seekMutex.withLock {
        val points = dataRepository.snapshot()
        if (points.isEmpty()) return@withLock
        startIndex = (fraction.coerceIn(0f, 1f) * points.size).toInt().coerceIn(0, points.size - 1)
        player.restart()
    }

    private fun buildSimulationFlow() = flow {
        val config = configManager.configuration.value
        if (config.loopIndefinitely) {
            while (currentCoroutineContext().isActive) {
                resetProgress()
                emitAll(simulationFlowOnce())
            }
        } else {
            resetProgress()
            emitAll(simulationFlowOnce())
        }
    }

    private fun simulationFlowOnce() =
        createSimulationFlow(System.currentTimeMillis())
            .catch { cause ->
                eventBus.pushEvent(
                    SimulationEvent.Error(
                        cause.message ?: "Unknown error occurred during simulation",
                    )
                )
                onError(cause)
            }
            .onCompletion { onComplete() }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun createSimulationFlow(simulationStartTime: Long): Flow<Location> {
        val points = dataRepository.snapshot()
        setSize(points.size)
        val from = startIndex.coerceIn(0, maxOf(0, points.size - 1))
        startIndex = 0
        return points.drop(from).asFlow().withIndex()
            .onEach { (idx, _) -> updateProgress(from + idx + 1) }
            .onEach { (idx, point) -> delayIfNeeded(idx, point) }
            .mapLatest { (_, point) -> transform(point) }
            .map { it.toLocation(simulationStartTime) }
    }

    private suspend fun delayIfNeeded(
        idx: Int,
        point: TrackPoint,
    ) {
        val delayMs = calculateDelay(idx, point)
        delay(delayMs)
    }


    companion object {
        private val TAG = LocationSimulator::class.simpleName!!
    }
}