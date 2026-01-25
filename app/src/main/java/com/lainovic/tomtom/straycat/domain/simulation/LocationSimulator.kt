package com.lainovic.tomtom.straycat.domain.simulation

import android.location.Location
import com.lainovic.tomtom.straycat.domain.location.SimulationPoint
import com.lainovic.tomtom.straycat.infrastructure.analytics.InMemorySimulationEventBus
import com.lainovic.tomtom.straycat.infrastructure.logging.AndroidLogger
import com.lainovic.tomtom.straycat.infrastructure.simulation.InMemorySimulationDataRepository
import com.lainovic.tomtom.straycat.shared.toLocation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.withIndex
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

class LocationSimulator(
    configuration: SimulationConfiguration = SimulationConfiguration(),
    private val onTick: suspend (Location) -> Unit,
    private val onComplete: () -> Unit = {},
    private val onError: (Throwable) -> Unit = {},
    private val backgroundScope: CoroutineScope,
    private val configManager: SimulationConfigurationManager =
        SimpleSimulationConfigurationManager(configuration),
    private val delayCalculator: SimpleDelayCalculator = SimpleDelayCalculator(
        configManager.configuration,
    ),
    private val simulationPointProcessor: SimulationPointProcessor = SimpleSimulationPointProcessor(
        configManager.configuration,
    ),
    private val dataRepository: SimulationDataRepository = InMemorySimulationDataRepository,
    private val eventBus: SimulationEventBus = InMemorySimulationEventBus,
    private val pauseController: PauseController = SimplePauseController(eventBus),
    private val progressTracker: ProgressTracker = SimpleProgressTracker(eventBus),
) :
    SimulationConfigurationManager by configManager,
    SimulationPointProcessor by simulationPointProcessor,
    PauseController by pauseController,
    ProgressTracker by progressTracker,
    DelayCalculator by delayCalculator {

    private var collectionJob: Job? = null

    init {
        backgroundScope.launch {
            this@LocationSimulator.configuration
                .drop(1)
                .collect { newConfig ->
                    AndroidLogger.d(TAG, "Configuration changed: $newConfig")
                    if (collectionJob?.isActive == true) {
                        stop()
                        start()
                    }
                }
        }
    }

    fun start() {
        AndroidLogger.d(TAG, "start() called, collectionJob=${collectionJob}")
        if (collectionJob?.isActive == true) {
            AndroidLogger.d(TAG, "Collection job already active, returning")
            return
        }

        resetPause()
        collectionJob = backgroundScope.launch {
            AndroidLogger.d(TAG, "Simulation coroutine started")
            val simulationStartTime = System.currentTimeMillis()
            runSimulation(simulationStartTime)
        }

        eventBus.pushEvent(SimulationEvent.SimulationStarted)

        AndroidLogger.i(TAG, "start() completed")
        AndroidLogger.d(TAG, "collectionJob=$collectionJob")
    }

    private suspend fun runSimulation(simulationStartTime: Long) = supervisorScope {
        val config = this@LocationSimulator.configuration.value

        if (config.loopIndefinitely) {
            while (isActive) {
                resetProgress()
                runSimulationOnce(simulationStartTime)
            }
        } else {
            resetProgress()
            runSimulationOnce(simulationStartTime)
        }
    }

    private suspend fun runSimulationOnce(simulationStartTime: Long) = supervisorScope {
        createSimulationFlow(simulationStartTime)
            .catch { cause ->
                eventBus.pushEvent(
                    SimulationEvent.SimulationError(
                        cause.message ?: "Unknown error occurred during simulation",
                    )
                )
                onError(cause)
            }
            .onCompletion { onComplete() }
            .collect { onTick(it) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun createSimulationFlow(simulationStartTime: Long) =
        dataRepository.snapshot()
            .also { points -> setSize(points.size) }
            .asFlow()
            .withIndex()
            .onEach { (idx, _) -> updateProgress(idx + 1) }
            .onEach { waitIfPaused() }
            .onEach { (idx, point) -> delayIfNeeded(idx, point) }
            .flatMapLatest { (_, point) -> flow { emit(process(point)) } }
            .map { it.toLocation(simulationStartTime) }

    private suspend fun delayIfNeeded(
        idx: Int,
        point: SimulationPoint,
    ) {
        val delayMs = calculateDelay(idx, point)
        delay(delayMs)
    }

    fun stop() {
        AndroidLogger.d(TAG, "stop() called, collectionJob=$collectionJob")
        collectionJob?.cancel()
        collectionJob = null
        resetPause()
        resetProgress()

        eventBus.pushEvent(SimulationEvent.SimulationStopped)
    }

    companion object {
        private val TAG = LocationSimulator::class.simpleName!!
    }
}