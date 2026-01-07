# Coroutines & Flow Learning Opportunities in StrayCat

**Analysis Date:** December 24, 2024  
**Purpose:** Identify opportunities to deepen understanding of Kotlin Coroutines and Flow concepts

---

## Overview

Your project already demonstrates solid understanding of coroutines and Flow. This document identifies **specific opportunities** where you can explore more advanced concepts, patterns, and best practices.

---

## 🎯 Current State Assessment

### ✅ What You're Already Doing Well

1. **StateFlow & SharedFlow usage** - Good separation between hot and cold flows
2. **Pausable Flow Collection** - Excellent implementation in LocationSimulator
3. **Suspend function conversion** - Good wrapper for callback-based APIs (RoutePlanner)
4. **Flow operators** - Using debounce, filter, distinctUntilChanged, flatMapLatest
5. **Structured concurrency** - Using viewModelScope and backgroundScope
6. **Exception handling** - CoroutineExceptionHandler in service

---

## 🚀 Advanced Learning Opportunities

### 1. **callbackFlow & Channel-based Flows** ⭐⭐⭐

**Current State:** You're using suspendCancellableCoroutine for one-shot callbacks

**Opportunity:** Create streaming flows from Android's LocationManager

**Where:** Create a real GPS location flow in addition to your mock locations

**Example Implementation:**

```kotlin
// New file: domain/service/GpsLocationFlow.kt
fun LocationManager.locationUpdates(
    provider: String = LocationManager.GPS_PROVIDER,
    minTimeMs: Long = 1000L,
    minDistanceM: Float = 0f
): Flow<Location> = callbackFlow {
    val listener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            // Send to flow - suspends if buffer is full
            trySend(location).isSuccess
        }

        override fun onProviderEnabled(provider: String) {
            // Could emit a sealed class with different states
        }

        override fun onProviderDisabled(provider: String) {
            // Signal provider disabled state
        }
    }

    // Register listener
    requestLocationUpdates(provider, minTimeMs, minDistanceM, listener)

    // Cleanup when flow is cancelled
    awaitClose {
        removeUpdates(listener)
        Logger.d("GpsFlow", "Location listener removed")
    }
}.buffer(capacity = Channel.CONFLATED) // Only keep latest location
```

**Learning Goals:**
- Understand `callbackFlow` for multi-value callbacks
- Learn `trySend` vs `send` (suspending vs non-suspending)
- Master `awaitClose` for cleanup
- Explore different Channel buffer strategies

---

### 2. **SharedFlow with Replay & Buffer** ⭐⭐⭐

**Current State:** Using MutableStateFlow for simple state

**Opportunity:** Create event streams for analytics, logging, or error reporting

**Where:** Track simulation events throughout the app

```kotlin
// New file: domain/analytics/SimulationEventTracker.kt
sealed class SimulationEvent {
    data class RouteRequested(val origin: Location, val destination: Location) : SimulationEvent()
    data class RoutePlanned(val pointCount: Int, val duration: Duration) : SimulationEvent()
    data class PlaybackStarted(val totalPoints: Int) : SimulationEvent()
    data class PlaybackProgress(val currentIndex: Int, val totalPoints: Int) : SimulationEvent()
    data class PlaybackPaused(val atIndex: Int) : SimulationEvent()
    data class PlaybackCompleted(val duration: Duration) : SimulationEvent()
    data class Error(val message: String, val stackTrace: String) : SimulationEvent()
}

object SimulationEventTracker {
    // Replay last 10 events for debugging
    private val _events = MutableSharedFlow<SimulationEvent>(
        replay = 10,
        extraBufferCapacity = 50,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val events: SharedFlow<SimulationEvent> = _events

    suspend fun track(event: SimulationEvent) {
        _events.emit(event)
    }
    
    fun trackNonSuspending(event: SimulationEvent) {
        _events.tryEmit(event)
    }
}

// Usage in ViewModel:
viewModelScope.launch {
    SimulationEventTracker.events.collect { event ->
        when (event) {
            is SimulationEvent.Error -> {
                // Show error UI, log to Firebase
            }
            is SimulationEvent.PlaybackCompleted -> {
                // Track analytics
            }
            // etc
        }
    }
}
```

**Learning Goals:**
- Understand SharedFlow vs StateFlow
- Learn replay cache mechanics
- Explore BufferOverflow strategies
- Practice hot flow sharing

---

### 3. **Flow Operators & Transformations** ⭐⭐⭐⭐

**Current State:** Basic operators (debounce, filter, map)

**Opportunity:** Create sophisticated data processing pipelines

**Where:** Process location data with validation, smoothing, and filtering

```kotlin
// New file: domain/service/LocationProcessingPipeline.kt

fun Flow<Location>.validateAndSmooth(
    maxSpeedMps: Double = 50.0, // 180 km/h
    smoothingWindowSize: Int = 3
): Flow<Location> = this
    // 1. Filter out invalid locations
    .filter { location ->
        location.latitude in -90.0..90.0 &&
        location.longitude in -180.0..180.0 &&
        location.accuracy < 100f
    }
    // 2. Remove duplicates (same lat/lng)
    .distinctUntilChangedBy { "${it.latitude},${it.longitude}" }
    // 3. Detect and filter impossible speeds
    .scan(null as Pair<Location, Location>?) { previous, current ->
        previous?.second to current
    }
    .filter { pair ->
        pair?.let { (prev, curr) ->
            val distance = prev.distanceTo(curr)
            val timeDiff = (curr.time - prev.time) / 1000.0
            val speed = distance / timeDiff
            speed <= maxSpeedMps
        } ?: true
    }
    .map { it?.second ?: error("Unexpected null") }
    // 4. Apply moving average for smoothing
    .windowed(smoothingWindowSize)
    .map { window ->
        Location(window.first()).apply {
            latitude = window.map { it.latitude }.average()
            longitude = window.map { it.longitude }.average()
            time = window.last().time
        }
    }

// Extension: Custom windowed operator for learning
fun <T> Flow<T>.windowed(size: Int): Flow<List<T>> = flow {
    val window = mutableListOf<T>()
    collect { value ->
        window.add(value)
        if (window.size > size) {
            window.removeAt(0)
        }
        if (window.size == size) {
            emit(window.toList())
        }
    }
}

// Usage in LocationSimulator:
private suspend fun runSimulation() {
    val snapshot = LocationDataSource.locations.value
    snapshot
        .toFlow()
        .validateAndSmooth(
            maxSpeedMps = 50.0,
            smoothingWindowSize = 3
        )
        .onEach { delayOrWaitUntilResumed() }
        .map { postProcess(it) }
        .collect { location ->
            onTick(location)
        }
}
```

**Learning Goals:**
- Master `scan` for stateful transformations
- Learn `windowed` for moving window operations
- Practice combining multiple operators
- Understand flow transformation order

---

### 4. **Combine, Zip, and Merge Operators** ⭐⭐⭐⭐

**Current State:** Independent flows, no combining

**Opportunity:** React to multiple state changes simultaneously

**Where:** Coordinate simulation state with configuration changes

```kotlin
// In LocationPlayerViewModel:

// Combine multiple state flows
val simulationUiState: StateFlow<SimulationUiState> = combine(
    LocationPlayerServiceStateProvider.state,
    LocationDataSource.locations,
    configurationFlow,
    progress
) { serviceState, locations, config, progress ->
    SimulationUiState(
        isPlaying = serviceState is LocationServiceState.Running,
        isPaused = serviceState is LocationServiceState.Paused,
        hasLocations = locations.isNotEmpty(),
        progress = progress,
        speed = config.speedMultiplier,
        canStart = locations.isNotEmpty() && serviceState is LocationServiceState.Idle
    )
}.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = SimulationUiState()
)

// Zip for pairing events from two flows
fun Flow<Location>.zipWithTimestamps(): Flow<Pair<Location, Long>> = 
    zip(
        flow { 
            while (true) {
                emit(System.currentTimeMillis())
                delay(100)
            }
        }
    ) { location, timestamp ->
        location to timestamp
    }

// Merge multiple error sources
val allErrors: Flow<String> = merge(
    routePlanningErrors,
    simulationErrors,
    networkErrors
).shareIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    replay = 1
)
```

**Learning Goals:**
- Understand `combine` vs `zip` vs `merge`
- Learn when each operator is appropriate
- Practice multi-source reactive programming
- Understand sharing and replay strategies

---

### 5. **Flow Context & Dispatcher Management** ⭐⭐⭐

**Current State:** Basic dispatcher usage

**Opportunity:** Optimize performance with proper dispatcher switching

**Where:** Heavy computation in location processing

```kotlin
// In LocationSimulator:

private suspend fun runSimulation() {
    supervisorScope {
        LocationDataSource.locations.value
            .toFlow()
            // CPU-intensive: Calculate route metrics
            .flowOn(Dispatchers.Default)
            .map { location ->
                // Heavy computation
                calculateRouteMetrics(location)
            }
            // Switch to IO for database/network
            .flowOn(Dispatchers.IO)
            .onEach { metrics ->
                // Save to database
                saveMetricsToDb(metrics)
            }
            // Back to Default for processing
            .flowOn(Dispatchers.Default)
            .map { processedLocation ->
                // More computation
                applyFilters(processedLocation)
            }
            // Main for UI updates (via onTick callback)
            .flowOn(Dispatchers.Main.immediate)
            .collect { location ->
                onTick(location)
            }
    }
}

// Learn about flowOn placement
fun Flow<Location>.processWithOptimalDispatchers(): Flow<ProcessedLocation> = 
    this
        .map { location ->
            // Runs on upstream dispatcher
            location
        }
        .flowOn(Dispatchers.IO) // Everything above runs on IO
        .map { location ->
            // Heavy processing
            ProcessedLocation(location)
        }
        .flowOn(Dispatchers.Default) // Processing runs on Default
        // Collection happens on collector's dispatcher
```

**Learning Goals:**
- Understand `flowOn` vs `withContext`
- Learn dispatcher selection strategies
- Practice context switching optimization
- Understand Dispatchers.Main.immediate vs Dispatchers.Main

---

### 6. **Cancellation & Cooperation** ⭐⭐⭐⭐

**Current State:** Basic cancellation with Job

**Opportunity:** Implement graceful cancellation and cleanup

**Where:** Handle interruption during route planning or playback

```kotlin
// New file: domain/service/CancellableRoutePlanner.kt

class CancellableRoutePlanner(private val routePlanner: RoutePlanner) {
    
    suspend fun planRouteWithProgress(
        origin: Location,
        destination: Location,
        onProgress: (Float) -> Unit
    ): List<Location> = coroutineScope {
        // Launch progress reporter
        val progressJob = launch {
            var progress = 0f
            while (progress < 0.9f) {
                ensureActive() // Check for cancellation
                progress += 0.1f
                onProgress(progress)
                delay(100)
            }
        }
        
        try {
            val result = routePlanner.planRoute(origin, destination)
            progressJob.cancel()
            onProgress(1f)
            result
        } catch (e: CancellationException) {
            Logger.d(TAG, "Route planning cancelled")
            throw e // Always re-throw CancellationException
        } finally {
            progressJob.cancel()
        }
    }
}

// In LocationSimulator - demonstrate cancellation handling:

fun start() {
    collectionJob = backgroundScope.launch {
        try {
            runSimulation()
        } catch (e: CancellationException) {
            Logger.d(TAG, "Simulation cancelled, cleaning up...")
            cleanup()
            throw e // Re-throw to parent
        }
    }
}

private suspend fun runSimulation() {
    LocationDataSource.locations.value
        .toFlow()
        .cancellable() // Make flow respect cancellation
        .onEach { 
            // Explicitly check for cancellation
            yield() // or ensureActive()
            delayOrWaitUntilResumed() 
        }
        .collect { location ->
            onTick(location)
        }
}
```

**Learning Goals:**
- Understand cooperative cancellation
- Learn `ensureActive()` vs `yield()`
- Practice proper CancellationException handling
- Master cleanup in finally blocks

---

### 7. **StateIn & ShareIn Operators** ⭐⭐⭐⭐⭐

**Current State:** Manual StateFlow creation

**Opportunity:** Convert cold flows to hot flows efficiently

**Where:** Share expensive operations across multiple collectors

```kotlin
// In LocationPlayerViewModel:

// Convert cold flow to hot StateFlow
val currentLocation: StateFlow<Location?> = 
    LocationDataSource.locations
        .map { it.lastOrNull() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(
                stopTimeoutMillis = 5000,
                replayExpirationMillis = 0
            ),
            initialValue = null
        )

// Share expensive network calls
val routeRecommendations: Flow<List<Route>> = flow {
    // Expensive API call
    val recommendations = fetchRouteRecommendations()
    emit(recommendations)
}.shareIn(
    scope = viewModelScope,
    started = SharingStarted.Lazily, // Start on first subscriber
    replay = 1 // Keep last value for new subscribers
)

// Different SharingStarted strategies:

// 1. WhileSubscribed - stop when no subscribers
val temperatureReadings = sensorFlow
    .shareIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000), // 5s grace period
        replay = 1
    )

// 2. Eagerly - start immediately
val criticalSystemState = systemStateFlow
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = SystemState.Unknown
    )

// 3. Lazily - start on first subscriber, never stop
val configCache = configFlow
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = Config.default()
    )

// Custom SharingStarted
val customSharing = SharingStarted { subscriptionCount ->
    subscriptionCount.map { count ->
        if (count > 0) SharingCommand.START
        else SharingCommand.STOP_AND_RESET_REPLAY_CACHE
    }
}
```

**Learning Goals:**
- Understand cold vs hot flows
- Master SharingStarted strategies
- Learn when to use stateIn vs shareIn
- Practice subscription count management

---

### 8. **Retry & Error Recovery** ⭐⭐⭐

**Current State:** Basic retry in LocationSimulator

**Opportunity:** Implement sophisticated retry strategies

**Where:** Network calls, GPS signal loss, service recovery

```kotlin
// In SimulationViewModel:

private val _routePlanningState = MutableStateFlow<RoutePlanningState>(RoutePlanningState.Idle)
val routePlanningState: StateFlow<RoutePlanningState> = _routePlanningState

fun planRouteWithRetry(origin: Location, destination: Location) {
    viewModelScope.launch {
        flow {
            emit(routePlanner.planRoute(origin, destination))
        }
            .retry(retries = 3) { cause ->
                Logger.w(TAG, "Route planning failed, retrying...", cause)
                delay(1000) // Wait before retry
                cause is IOException // Only retry on network errors
            }
            .retryWhen { cause, attempt ->
                // Exponential backoff
                if (attempt < 3 && cause is IOException) {
                    delay(2.0.pow(attempt.toDouble()).toLong() * 1000)
                    true
                } else {
                    false
                }
            }
            .catch { e ->
                _routePlanningState.value = RoutePlanningState.Error(e.message ?: "Unknown error")
            }
            .collect { route ->
                _routePlanningState.value = RoutePlanningState.Success(route)
            }
    }
}

// Advanced: Circuit breaker pattern
class CircuitBreaker(
    private val failureThreshold: Int = 5,
    private val resetTimeoutMs: Long = 60_000
) {
    private var failureCount = 0
    private var lastFailureTime = 0L
    private var state = State.CLOSED

    enum class State { CLOSED, OPEN, HALF_OPEN }

    fun <T> protect(block: suspend () -> T): Flow<T> = flow {
        when (state) {
            State.OPEN -> {
                if (System.currentTimeMillis() - lastFailureTime > resetTimeoutMs) {
                    state = State.HALF_OPEN
                } else {
                    throw CircuitBreakerOpenException()
                }
            }
            else -> {}
        }

        try {
            val result = block()
            if (state == State.HALF_OPEN) {
                state = State.CLOSED
                failureCount = 0
            }
            emit(result)
        } catch (e: Exception) {
            failureCount++
            lastFailureTime = System.currentTimeMillis()
            if (failureCount >= failureThreshold) {
                state = State.OPEN
            }
            throw e
        }
    }
}
```

**Learning Goals:**
- Master retry and retryWhen operators
- Implement exponential backoff
- Learn circuit breaker pattern
- Practice error recovery strategies

---

### 9. **Flow Testing** ⭐⭐⭐⭐

**Current State:** Good test for LocationSimulator

**Opportunity:** Comprehensive flow testing strategies

**Where:** Test all ViewModels and flow transformations

```kotlin
// In test/: New file ViewModelFlowTest.kt

@OptIn(ExperimentalCoroutinesApi::class)
class LocationPlayerViewModelFlowTest {
    
    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)
    
    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }
    
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
    
    @Test
    fun `state flow emits correct sequence`() = testScope.runTest {
        // Arrange
        val fakeService = FakeLocationService()
        val viewModel = LocationPlayerViewModel(fakeService)
        
        // Act & Assert with turbine library (add dependency)
        viewModel.state.test {
            assertEquals(LocationServiceState.Idle, awaitItem())
            
            fakeService.emitState(LocationServiceState.Running(0f))
            assertEquals(LocationServiceState.Running(0f), awaitItem())
            
            fakeService.emitState(LocationServiceState.Paused(0.5f))
            assertEquals(LocationServiceState.Paused(0.5f), awaitItem())
            
            cancelAndIgnoreRemainingEvents()
        }
    }
    
    @Test
    fun `progress updates reflect service state`() = testScope.runTest {
        val fakeService = FakeLocationService()
        val viewModel = LocationPlayerViewModel(fakeService)
        
        val progressValues = mutableListOf<Float>()
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.progress.collect { progressValues.add(it) }
        }
        
        fakeService.emitState(LocationServiceState.Running(0.25f))
        advanceUntilIdle()
        
        fakeService.emitState(LocationServiceState.Running(0.50f))
        advanceUntilIdle()
        
        fakeService.emitState(LocationServiceState.Paused(0.75f))
        advanceUntilIdle()
        
        assertEquals(listOf(0f, 0.25f, 0.50f, 0.75f), progressValues)
        
        job.cancel()
    }
    
    @Test
    fun `combining multiple flows produces correct ui state`() = testScope.runTest {
        // Test combine operator
        val serviceState = MutableStateFlow<LocationServiceState>(LocationServiceState.Idle)
        val locations = MutableStateFlow<List<Location>>(emptyList())
        
        val uiState = combine(serviceState, locations) { state, locs ->
            UiState(
                canStart = state is LocationServiceState.Idle && locs.isNotEmpty(),
                isPlaying = state is LocationServiceState.Running
            )
        }
        
        uiState.test {
            // Initial state
            assertEquals(UiState(canStart = false, isPlaying = false), awaitItem())
            
            // Add locations
            locations.value = listOf(mockLocation())
            assertEquals(UiState(canStart = true, isPlaying = false), awaitItem())
            
            // Start playing
            serviceState.value = LocationServiceState.Running(0f)
            assertEquals(UiState(canStart = false, isPlaying = true), awaitItem())
        }
    }
}

// Test flow transformations
@Test
fun `location validation filters invalid points`() = runTest {
    val testLocations = listOf(
        createLocation(0.0, 0.0, accuracy = 10f),    // Valid
        createLocation(91.0, 0.0, accuracy = 10f),   // Invalid latitude
        createLocation(0.0, 0.0, accuracy = 150f),   // Invalid accuracy
        createLocation(45.0, 90.0, accuracy = 20f),  // Valid
    )
    
    val results = testLocations
        .asFlow()
        .validateAndSmooth()
        .toList()
    
    assertEquals(2, results.size)
    assertEquals(0.0, results[0].latitude, 0.001)
    assertEquals(45.0, results[1].latitude, 0.001)
}
```

**Learning Goals:**
- Master TestScope and TestDispatcher
- Learn turbine library for flow testing
- Practice testing flow operators
- Understand time manipulation in tests

---

### 10. **Actor Pattern with Channels** ⭐⭐⭐⭐⭐

**Current State:** Traditional state management

**Opportunity:** Implement actor pattern for thread-safe state

**Where:** Manage simulation configuration changes safely

```kotlin
// New file: domain/service/SimulationConfigurationActor.kt

sealed class ConfigCommand {
    data class UpdateSpeed(val multiplier: Float) : ConfigCommand()
    data class UpdateNoise(val meters: Float) : ConfigCommand()
    data class UpdateDelay(val delay: Duration) : ConfigCommand()
    object GetConfig : ConfigCommand()
    object Reset : ConfigCommand()
}

sealed class ConfigResponse {
    data class CurrentConfig(val config: SimulationConfiguration) : ConfigResponse()
    object Updated : ConfigResponse()
}

class SimulationConfigActor(
    private val scope: CoroutineScope
) {
    private val commandChannel = Channel<Pair<ConfigCommand, CompletableDeferred<ConfigResponse>>>()
    
    private var currentConfig = SimulationConfiguration()
    
    init {
        scope.launch {
            processCommands()
        }
    }
    
    private suspend fun processCommands() {
        for ((command, response) in commandChannel) {
            when (command) {
                is ConfigCommand.UpdateSpeed -> {
                    currentConfig = currentConfig.copy(speedMultiplier = command.multiplier)
                    response.complete(ConfigResponse.Updated)
                }
                is ConfigCommand.UpdateNoise -> {
                    currentConfig = currentConfig.copy(noiseLevelInMeters = command.meters)
                    response.complete(ConfigResponse.Updated)
                }
                is ConfigCommand.UpdateDelay -> {
                    currentConfig = currentConfig.copy(delayBetweenEmissions = command.delay)
                    response.complete(ConfigResponse.Updated)
                }
                is ConfigCommand.GetConfig -> {
                    response.complete(ConfigResponse.CurrentConfig(currentConfig))
                }
                is ConfigCommand.Reset -> {
                    currentConfig = SimulationConfiguration()
                    response.complete(ConfigResponse.Updated)
                }
            }
        }
    }
    
    suspend fun send(command: ConfigCommand): ConfigResponse {
        val response = CompletableDeferred<ConfigResponse>()
        commandChannel.send(command to response)
        return response.await()
    }
    
    fun close() {
        commandChannel.close()
    }
}

// Usage:
class LocationPlayerService : Service() {
    private val configActor = SimulationConfigActor(serviceScope)
    
    suspend fun updateSpeed(multiplier: Float) {
        configActor.send(ConfigCommand.UpdateSpeed(multiplier))
    }
    
    suspend fun getCurrentConfig(): SimulationConfiguration {
        return when (val response = configActor.send(ConfigCommand.GetConfig)) {
            is ConfigResponse.CurrentConfig -> response.config
            else -> error("Unexpected response")
        }
    }
}
```

**Learning Goals:**
- Understand actor pattern for concurrency
- Learn Channel vs Flow differences
- Practice CompletableDeferred
- Master thread-safe state management

---

## 📚 Recommended Learning Path

### Phase 1: Foundations (Weeks 1-2)
1. ✅ Review your existing code - identify patterns
2. Implement **callbackFlow** for GPS locations (#1)
3. Add **Flow operators** for location validation (#3)
4. Practice **Flow testing** (#9)

### Phase 2: Intermediate (Weeks 3-4)
5. Implement **SharedFlow** event tracking (#2)
6. Master **combine/zip/merge** for multi-source state (#4)
7. Add **retry strategies** with exponential backoff (#8)
8. Explore **flowOn** and dispatcher optimization (#5)

### Phase 3: Advanced (Weeks 5-6)
9. Implement **stateIn/shareIn** for hot flows (#7)
10. Practice **cancellation** and cleanup (#6)
11. Build **actor pattern** for config management (#10)
12. Comprehensive testing of all new features (#9)

---

## 🎓 Specific Exercises

### Exercise 1: Real GPS Flow
Create a real GPS location service using `callbackFlow`:
- File: `domain/service/GpsLocationService.kt`
- Use LocationManager with callbackFlow
- Handle provider enabled/disabled states
- Implement proper cleanup

### Exercise 2: Location Analytics
Create an analytics system using SharedFlow:
- Track all simulation events
- Implement replay buffer
- Create a debug UI to show recent events
- Practice BufferOverflow strategies

### Exercise 3: Smart Route Planning
Enhance route planning with:
- Retry with exponential backoff
- Progress reporting during planning
- Cancellation support
- Cache results with shareIn

### Exercise 4: Multi-Source Location
Combine multiple location sources:
- GPS sensor flow
- Mock location flow  
- Network location flow
- Use `merge` or `combine` to create best estimate

### Exercise 5: Performance Monitoring
Create a performance monitoring flow:
- Track FPS, memory, battery
- Use `scan` to calculate running averages
- Emit warnings when thresholds exceeded
- Practice custom operators

---

## 📖 Additional Resources

### Books
- "Kotlin Coroutines: Deep Dive" by Marcin Moskała
- "Asynchronous Programming with Kotlin Flow" by Raywenderlich

### Official Docs
- [Kotlin Flow Documentation](https://kotlinlang.org/docs/flow.html)
- [Android Coroutines Guide](https://developer.android.com/kotlin/coroutines)

### Libraries to Explore
- **Turbine** - Flow testing library
- **Kotlin-retry** - Sophisticated retry strategies
- **Molecule** - Compose for flows

### Video Courses
- Roman Elizarov's KotlinConf talks
- Android Developers YouTube channel - Coroutines series

---

## 🔍 Code Review Checklist

When implementing these concepts, check:

- [ ] Are flows cold or hot? Is this intentional?
- [ ] Is cancellation handled properly?
- [ ] Are exceptions caught and handled?
- [ ] Is cleanup performed in `awaitClose` or finally blocks?
- [ ] Are the right dispatchers used for the work type?
- [ ] Is there any unnecessary StateFlow → Flow → StateFlow conversion?
- [ ] Are tests covering happy path, errors, and cancellation?
- [ ] Is backpressure strategy appropriate?
- [ ] Are shared flows using the right SharingStarted strategy?
- [ ] Is `ensureActive()` called in long-running loops?

---

## 🎯 Next Steps

1. **Pick one opportunity** from the list above
2. **Implement it** in a feature branch
3. **Write tests** to verify behavior
4. **Compare** before/after - what did you learn?
5. **Document** your findings
6. **Move to next opportunity**

Remember: The goal is **learning**, not just adding features. Take time to understand each concept deeply before moving on.

---

**Happy Learning! 🚀**
