# StrayCat - Visual Architecture Diagrams

This document contains visual representations of the StrayCat architecture.

---

## Complete System Architecture

```
┌───────────────────────────────────────────────────────────────────────────────┐
│                              ANDROID SYSTEM                                   │
│                                                                               │
│  ┌─────────────────────────────────────────────────────────────────────────┐ │
│  │                         PRESENTATION LAYER                              │ │
│  │                                                                         │ │
│  │  ┌─────────────────────────────────────────────────────────────────┐  │ │
│  │  │ MainActivity (ComponentActivity)                                │  │ │
│  │  │                                                                 │  │ │
│  │  │  • Jetpack Compose UI (edge-to-edge)                          │  │ │
│  │  │  • Permission handling (runtime)                              │  │ │
│  │  │  • LaunchedEffect for side effects (Toast)                    │  │ │
│  │  │  • collectAsState() for reactive UI updates                   │  │ │
│  │  │                                                                 │  │ │
│  │  │  [Start/Stop Button] [Pause/Resume Button]                    │  │ │
│  │  │         │                    │                                  │  │ │
│  │  └─────────┼────────────────────┼──────────────────────────────────┘  │ │
│  │            │                    │                                      │ │
│  │            └────────┬───────────┘                                      │ │
│  │                     ↓                                                  │ │
│  │  ┌─────────────────────────────────────────────────────────────────┐  │ │
│  │  │ LocationPlayerViewModel                                         │  │ │
│  │  │                                                                 │  │ │
│  │  │  state: StateFlow<LocationServiceState>                        │  │ │
│  │  │                                                                 │  │ │
│  │  │  fun startStop() {                                             │  │ │
│  │  │    when (state.value) {                                        │  │ │
│  │  │      Idle, Stopped, Error -> service.start()                   │  │ │
│  │  │      Running, Paused -> service.stop()                         │  │ │
│  │  │    }                                                            │  │ │
│  │  │  }                                                              │  │ │
│  │  │                                                                 │  │ │
│  │  │  fun pauseResume() { ... }                                     │  │ │
│  │  │                                                                 │  │ │
│  │  └─────────────────────────┬───────────────────────────────────────┘  │ │
│  └────────────────────────────┼──────────────────────────────────────────┘ │
│                               │                                            │
│                               │ delegates to                               │
│                               ↓                                            │
│  ┌─────────────────────────────────────────────────────────────────────┐  │
│  │                           FACADE LAYER                              │  │
│  │                                                                     │  │
│  │  ┌───────────────────────────────────────────────────────────────┐ │  │
│  │  │ LocationServiceFacade                                         │ │  │
│  │  │                                                               │ │  │
│  │  │  private val _state = MutableStateFlow(Idle)                 │ │  │
│  │  │  val state: StateFlow<LocationServiceState> = _state         │ │  │
│  │  │                                                               │ │  │
│  │  │  private val stateReceiver = object : BroadcastReceiver() { │ │  │
│  │  │    override fun onReceive(context, intent) {                 │ │  │
│  │  │      val newState = intent.getSerializable(EXTRA_STATE)      │ │  │
│  │  │      _state.value = newState  // Update from Service         │ │  │
│  │  │    }                                                          │ │  │
│  │  │  }                                                            │ │  │
│  │  │                                                               │ │  │
│  │  │  fun start() { sendServiceIntent(ACTION_START) }             │ │  │
│  │  │  fun pause() { sendServiceIntent(ACTION_PAUSE) }             │ │  │
│  │  │  fun resume() { sendServiceIntent(ACTION_RESUME) }           │ │  │
│  │  │  fun stop() { sendServiceIntent(ACTION_STOP) }               │ │  │
│  │  │                                                               │ │  │
│  │  └───────────────┬───────────────────────────────┬───────────────┘ │  │
│  └──────────────────┼───────────────────────────────┼─────────────────┘  │
│                     │                               │                    │
│                     │ controls via Intents          │ observes via       │
│                     ↓                               │ BroadcastReceiver  │
│  ┌─────────────────────────────────────────────────┼─────────────────┐  │
│  │                    SERVICE LAYER                 │                 │  │
│  │                                                  ↑                 │  │
│  │  ┌───────────────────────────────────────────────┼───────────────┐│  │
│  │  │ LocationService<T> (Abstract)                 │               ││  │
│  │  │                                               │               ││  │
│  │  │  val handler = CoroutineExceptionHandler {   │               ││  │
│  │  │    broadcastState(Error(throwable.message)) ─┘               ││  │
│  │  │  }                                                            ││  │
│  │  │                                                               ││  │
│  │  │  private val simulator by lazy {                             ││  │
│  │  │    LocationSimulator(                                        ││  │
│  │  │      locationFlow = createLocationFlow(),                    ││  │
│  │  │      onTick = { tick -> Log.i(TAG, "Tick: $tick") },        ││  │
│  │  │      backgroundScope = CoroutineScope(Default + handler)     ││  │
│  │  │    )                                                          ││  │
│  │  │  }                                                            ││  │
│  │  │                                                               ││  │
│  │  │  override fun onStartCommand(intent, flags, startId) {       ││  │
│  │  │    try {                                                      ││  │
│  │  │      when (intent?.action) {                                 ││  │
│  │  │        ACTION_START -> {                                     ││  │
│  │  │          simulator.start()                                   ││  │
│  │  │          broadcastState(Running) ────────────────────────────┼┼──┐
│  │  │        }                                                      ││  │
│  │  │        ACTION_PAUSE -> simulator.pause()                     ││  │
│  │  │        ACTION_RESUME -> simulator.resume()                   ││  │
│  │  │        ACTION_STOP -> simulator.stop()                       ││  │
│  │  │      }                                                        ││  │
│  │  │    } catch (e: Exception) {                                  ││  │
│  │  │      broadcastState(Error(e.message)) ──────────────────────┼┼──┤
│  │  │    }                                                          ││  │
│  │  │  }                                                            ││  │
│  │  │                                                               ││  │
│  │  │  abstract fun createLocationFlow(): Flow<T>  // Hook method  ││  │
│  │  │                                                               ││  │
│  │  └───────────────────────────┬───────────────────────────────────┘│  │
│  │                              │                                    │  │
│  │                              │ extends                            │  │
│  │                              ↓                                    │  │
│  │  ┌─────────────────────────────────────────────────────────────┐ │  │
│  │  │ TickerLocationService                                       │ │  │
│  │  │                                                             │ │  │
│  │  │  override fun createLocationFlow(): Flow<Long> {            │ │  │
│  │  │    return tickerFlow(1_000L)                               │ │  │
│  │  │  }                                                          │ │  │
│  │  │                                                             │ │  │
│  │  └─────────────────────────┬───────────────────────────────────┘ │  │
│  └────────────────────────────┼─────────────────────────────────────┘  │
│                               │                                        │
│                               │ uses                                   │
│                               ↓                                        │
│  ┌─────────────────────────────────────────────────────────────────┐  │
│  │                     SIMULATION LAYER                            │  │
│  │                                                                 │  │
│  │  ┌───────────────────────────────────────────────────────────┐ │  │
│  │  │ LocationSimulator<T>                                      │ │  │
│  │  │                                                           │ │  │
│  │  │  private val isPaused = MutableStateFlow(false)          │ │  │
│  │  │  private var collectionJob: Job? = null                  │ │  │
│  │  │                                                           │ │  │
│  │  │  fun start() {                                            │ │  │
│  │  │    collectionJob = backgroundScope.launch {              │ │  │
│  │  │      collect()                                            │ │  │
│  │  │    }                                                      │ │  │
│  │  │  }                                                        │ │  │
│  │  │                                                           │ │  │
│  │  │  private suspend fun collect() {                          │ │  │
│  │  │    locationFlow                                           │ │  │
│  │  │      .onCompletion { onComplete() }                       │ │  │
│  │  │      .collect { tick ->                                   │ │  │
│  │  │        isPaused.first { !it }  // Suspends when paused   │ │  │
│  │  │        onTick(tick)                                       │ │  │
│  │  │      }                                                    │ │  │
│  │  │  }                                                        │ │  │
│  │  │                                                           │ │  │
│  │  │  fun pause() { isPaused.value = true }                   │ │  │
│  │  │  fun resume() { isPaused.value = false }                 │ │  │
│  │  │  fun stop() { collectionJob?.cancel() }                  │ │  │
│  │  │                                                           │ │  │
│  │  └─────────────────────────┬─────────────────────────────────┘ │  │
│  └────────────────────────────┼───────────────────────────────────┘  │
│                               │                                      │
│                               │ collects                             │
│                               ↓                                      │
│  ┌─────────────────────────────────────────────────────────────────┐│
│  │                         DATA LAYER                              ││
│  │                                                                 ││
│  │  ┌───────────────────────────────────────────────────────────┐ ││
│  │  │ Flow<T>                                                   │ ││
│  │  │                                                           │ ││
│  │  │  fun tickerFlow(periodMs: Long) = flow {                 │ ││
│  │  │    var tick = 0L                                         │ ││
│  │  │    while (true) {                                        │ ││
│  │  │      emit(tick++)      // Cold flow                      │ ││
│  │  │      delay(periodMs)   // Suspends                       │ ││
│  │  │    }                                                     │ ││
│  │  │  }                                                        │ ││
│  │  │                                                           │ ││
│  │  │  // Future: gpsFlow(), fileFlow(), mockFlow()            │ ││
│  │  │                                                           │ ││
│  │  └───────────────────────────────────────────────────────────┘ ││
│  └─────────────────────────────────────────────────────────────────┘│
└───────────────────────────────────────────────────────────────────────┘
```

---

## State Flow Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                     State Lifecycle                             │
└─────────────────────────────────────────────────────────────────┘

                    ┌──────────┐
              ┌────→│   Idle   │←─────────────┐
              │     └─────┬────┘              │
              │           │                   │
              │     start()│                  │
              │           ↓                   │
              │     ┌──────────┐              │
              │     │ Running  │              │
              │     └──┬────┬──┘              │
              │        │    │                 │
    (app      │  pause()│  │stop()           │
    restart)  │        │    │                 │
              │        ↓    ↓                 │
              │   ┌────────┐ ┌──────────┐    │
              └───│ Paused │ │ Stopped  │────┘
                  └───┬────┘ └──────────┘
                      │
                resume()│
                      │
                      └────────┐
                               │
                               ↓
                        (back to Running)

                ┌──────────────────────┐
                │  Any state can       │
                │  transition to Error │
                │  on exception        │
                └──────────┬───────────┘
                           ↓
                      ┌─────────┐
                      │  Error  │
                      └────┬────┘
                           │
                      retry()│
                           ↓
                      (to Running)
```

---

## Command Flow (User Action → Service)

```
┌─────────────────────────────────────────────────────────────────┐
│                     Command Flow                                │
└─────────────────────────────────────────────────────────────────┘

1. User Action
   │
   │ clicks "Start" button
   ↓
┌──────────────────────┐
│ MainActivity         │
│ onStartStopClick()   │
└──────────┬───────────┘
           │
           │ calls
           ↓
┌──────────────────────┐
│ ViewModel            │
│ startStop()          │
│                      │
│ when (state.value) { │
│   Idle -> start()    │
│   ...                │
│ }                    │
└──────────┬───────────┘
           │
           │ delegates to
           ↓
┌──────────────────────┐
│ Facade               │
│ start()              │
│                      │
│ sendServiceIntent(   │
│   ACTION_START       │
│ )                    │
└──────────┬───────────┘
           │
           │ creates Intent
           ↓
┌──────────────────────┐
│ Android System       │
│ startForegroundSvc() │
└──────────┬───────────┘
           │
           │ starts Service
           ↓
┌──────────────────────┐
│ Service              │
│ onStartCommand()     │
│                      │
│ ACTION_START ->      │
│   simulator.start()  │
└──────────┬───────────┘
           │
           │ calls
           ↓
┌──────────────────────┐
│ Simulator            │
│ start()              │
│                      │
│ collectionJob =      │
│   launch { collect() }│
└──────────┬───────────┘
           │
           │ collects
           ↓
┌──────────────────────┐
│ Flow<T>              │
│ emit(tick)           │
│ delay(1000)          │
│ emit(tick+1)         │
│ ...                  │
└──────────────────────┘
```

---

## State Flow (Service → UI)

```
┌─────────────────────────────────────────────────────────────────┐
│                      State Flow                                 │
└─────────────────────────────────────────────────────────────────┘

1. State Change in Service
   │
   │ simulator.start() completes
   ↓
┌──────────────────────┐
│ Service              │
│ broadcastState(      │
│   Running            │
│ )                    │
└──────────┬───────────┘
           │
           │ sendBroadcast(intent)
           ↓
┌──────────────────────┐
│ Android System       │
│ Broadcast Manager    │
└──────────┬───────────┘
           │
           │ delivers to
           ↓
┌──────────────────────┐
│ Facade               │
│ stateReceiver        │
│ onReceive()          │
│                      │
│ _state.value =       │
│   Running            │
└──────────┬───────────┘
           │
           │ StateFlow emits
           ↓
┌──────────────────────┐
│ ViewModel            │
│ state: StateFlow     │
│   (exposes facade    │
│    state)            │
└──────────┬───────────┘
           │
           │ collectAsState()
           ↓
┌──────────────────────┐
│ MainActivity         │
│ Composable           │
│                      │
│ val state by         │
│   viewModel.state    │
│   .collectAsState()  │
└──────────┬───────────┘
           │
           │ recompose UI
           ↓
┌──────────────────────┐
│ Button               │
│ Text("Stop")         │ ← Updated from "Start"
└──────────────────────┘
```

---

## Error Flow

```
┌─────────────────────────────────────────────────────────────────┐
│                       Error Flow                                │
└─────────────────────────────────────────────────────────────────┘

1. Error Occurs
   │
   ├─→ In onStartCommand()       ├─→ In Flow collection
   │   (try-catch)                │   (CoroutineExceptionHandler)
   │                              │
   ↓                              ↓
┌──────────────────────┐    ┌──────────────────────┐
│ Service              │    │ Handler              │
│ catch (e: Exception) │    │ handle(throwable)    │
│   broadcastState(    │    │   broadcastState(    │
│     Error(message)   │    │     Error(message)   │
│   )                  │    │   )                  │
│   stopSelf()         │    │                      │
└──────────┬───────────┘    └──────────┬───────────┘
           │                           │
           └───────────┬───────────────┘
                       │
                       │ broadcasts
                       ↓
           ┌──────────────────────┐
           │ Facade               │
           │ receives Error state │
           │ _state.value = Error │
           └──────────┬───────────┘
                      │
                      │ exposes
                      ↓
           ┌──────────────────────┐
           │ ViewModel            │
           │ state = Error        │
           └──────────┬───────────┘
                      │
                      │ observes
                      ↓
           ┌──────────────────────┐
           │ MainActivity         │
           │                      │
           │ LaunchedEffect(state)│
           │   if (is Error)      │
           │     Toast.show()     │
           │                      │
           │ Button {             │
           │   Text("Retry")      │ ← Changed from "Start"
           │ }                    │
           └──────────┬───────────┘
                      │
                      │ user clicks Retry
                      ↓
           ┌──────────────────────┐
           │ ViewModel.startStop()│
           │ case Error:          │
           │   service.start()    │ ← Attempts restart
           └──────────────────────┘
```

---

## Pause/Resume Mechanism

```
┌─────────────────────────────────────────────────────────────────┐
│                  Pause/Resume Mechanism                         │
└─────────────────────────────────────────────────────────────────┘

                    ┌─────────────────┐
                    │ Flow<T>         │
                    │ emit(0)         │
                    │ emit(1)         │
                    │ emit(2) ← paused│
                    │ emit(3) ← paused│
                    │ emit(4)         │
                    └────────┬────────┘
                             │
                             ↓
                    ┌─────────────────┐
                    │ LocationSimulator│
                    │                 │
                    │ collect { tick ->│
                    │   isPaused      │
                    │     .first{!it} │← Suspends here when paused
                    │   onTick(tick)  │
                    │ }               │
                    └────────┬────────┘
                             │
              ┌──────────────┴──────────────┐
              │                             │
         pause()│                       resume()│
              │                             │
              ↓                             ↓
    ┌──────────────────┐        ┌──────────────────┐
    │ isPaused = true  │        │ isPaused = false │
    │                  │        │                  │
    │ Producer pauses  │        │ Producer resumes │
    │ (tick 2, 3 wait) │        │ (continues from  │
    │                  │        │  where it paused)│
    └──────────────────┘        └──────────────────┘

Timeline:
  0ms: start() → collect begins
  0ms: emit(0) → onTick(0) → log "Tick: 0"
1000ms: emit(1) → onTick(1) → log "Tick: 1"
2000ms: pause() → isPaused = true
2000ms: emit(2) → SUSPENDED (waits for resume)
3000ms: emit(3) would happen but 2 is still waiting
4000ms: resume() → isPaused = false
4000ms: emit(2) completes → onTick(2) → log "Tick: 2"
5000ms: emit(3) → onTick(3) → log "Tick: 3"

Result: No data loss, continues from exact point
```

---

## Testing Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                    Testing Strategy                             │
└─────────────────────────────────────────────────────────────────┘

┌────────────────────────┐
│ LocationSimulatorTest  │  ← Unit Tests (JVM)
│                        │
│ • Pause suspends       │
│ • Resume continues     │
│ • Stop cancels         │
│ • Start/stop cycles    │
│                        │
│ Uses:                  │
│ • runTest              │
│ • TestCoroutineScope   │
│ • advanceTimeBy        │
└────────────────────────┘

┌────────────────────────┐
│ ViewModelTest          │  ← Unit Tests (JVM)
│                        │
│ • State transitions    │
│ • Command logic        │
│ • Error handling       │
│                        │
│ Uses:                  │
│ • FakeLocationService  │
│   Facade               │
└────────────────────────┘

┌────────────────────────┐
│ Integration Tests      │  ← Instrumented (Future)
│ (Future)               │
│                        │
│ • Full lifecycle       │
│ • Service survival     │
│ • Permission handling  │
│                        │
│ Uses:                  │
│ • Robolectric or       │
│ • Android Test         │
└────────────────────────┘
```

---

## Extension Points

```
┌─────────────────────────────────────────────────────────────────┐
│                    Extension Points                             │
└─────────────────────────────────────────────────────────────────┘

        ┌──────────────────────┐
        │ LocationService<T>   │  ← Abstract base
        │ (Template Method)    │
        │                      │
        │ abstract fun         │
        │ createLocationFlow() │← Hook method
        └──────────┬───────────┘
                   │
        ┌──────────┴──────────────────────────┐
        │                                     │
        ↓                                     ↓
┌──────────────────────┐          ┌──────────────────────┐
│ TickerLocationSvc    │          │ GpsLocationService   │
│ (Current)            │          │ (Future)             │
│                      │          │                      │
│ override fun         │          │ override fun         │
│ createLocationFlow() │          │ createLocationFlow() │
│   = tickerFlow()     │          │   = gpsFlow()        │
└──────────────────────┘          └──────────────────────┘
        │                                     │
        ↓                                     ↓
┌──────────────────────┐          ┌──────────────────────┐
│ FileLocationService  │          │ MockLocationService  │
│ (Future)             │          │ (Future)             │
│                      │          │                      │
│ override fun         │          │ override fun         │
│ createLocationFlow() │          │ createLocationFlow() │
│   = readGpxFile()    │          │   = mockRoute()      │
└──────────────────────┘          └──────────────────────┘

All services:
• Same lifecycle management
• Same error handling
• Same state broadcasting
• Only flow source differs
```

---

**Document Version:** 1.0  
**Last Updated:** December 17, 2025  
**For:** StrayCat Architecture Documentation

