# StrayCat - Quick Reference

## Architecture at a Glance

```
UI → ViewModel → Facade → Service → Simulator → Flow
         ↑          ↑         ↓
         └──────────┴─────────┘
              (Broadcasts)
```

## Key Files

| File | Purpose | Layer |
|------|---------|-------|
| `MainActivity.kt` | UI & Permissions | Presentation |
| `SimulationPlayerViewModel.kt` | UI Logic | Presentation |
| `SimulationServiceFacade.kt` | Service Abstraction | Facade |
| `SimulationService.kt` | Background Work | Service |
| `TickerLocationService.kt` | Ticker Implementation | Service |
| `LocationSimulator.kt` | Flow Collection | Simulation |
| `SimulationState.kt` | State Model | Domain |

## Command Cheat Sheet

### Build & Run
```bash
./gradlew assembleDebug
./gradlew installDebug
```

### Run Tests
```bash
./gradlew test
./gradlew testDebugUnitTest
```

### View Logs
```bash
# All app logs
adb logcat | grep "com.lainovic.tomtom.straycat"

# Specific component
adb logcat | grep "LocationPlayerViewModel"
```

## State Transitions

```
Idle ──start──> Running ──pause──> Paused ──resume──> Running
                   │                  │
                   └──stop──> Stopped ┘
                   
(Any state) ──error──> Error ──retry──> Running
```

## Common Tasks

### Add New Location Service

1. Create service class:
```kotlin
class MyLocationService : LocationService<Location>() {
    override fun createLocationFlow() = myCustomFlow()
}
```

2. Register in manifest:
```xml
<service android:name=".MyLocationService" 
         android:foregroundServiceType="location" />
```

3. Use in MainActivity:
```kotlin
LocationServiceFacade(application, MyLocationService::class.java)
```

### Debug Flow Not Emitting

```bash
adb logcat | grep -E "LocationSimulator|LocationService|TickerFlow"
```

Look for:
- ✅ "LocationSimulator created"
- ✅ "Collection job coroutine started"
- ✅ "Emitting tick: X"

### Debug State Not Updating

```bash
adb logcat | grep "LocationServiceFacade"
```

Look for:
- ✅ "Broadcast received"
- ✅ "Updated facade state to: X"

## Design Patterns Used

- **MVVM** - Overall architecture
- **Facade** - LocationServiceFacade
- **Template Method** - LocationService<T>
- **Observer** - StateFlow + Broadcasts
- **Strategy** - Swappable services

## Error Handling

**Two Levels:**
1. `try-catch` in `onStartCommand()` → Lifecycle errors
2. `CoroutineExceptionHandler` → Flow errors

**User Experience:**
Error → Toast + "Retry" button → Clean restart

## Key Principles

1. **Single Source of Truth** - Service owns state
2. **No Optimistic Updates** - Wait for broadcasts
3. **Unidirectional Flow** - Commands down, state up
4. **Testability** - Each layer independent
5. **Extensibility** - Generic service design

## Performance Tips

- Simulator uses lazy initialization
- Single thread for flow collection
- StateFlow conflates rapid updates
- Background dispatcher for CPU work

## Quick Debugging

**Problem:** Service not starting
```bash
adb shell dumpsys activity services | grep -A 10 "StrayCat"
```

**Problem:** Permissions denied
```bash
adb shell pm list permissions -d -g | grep LOCATION
```

**Problem:** Memory leak
```bash
adb shell dumpsys meminfo com.lainovic.tomtom.straycat
```

## Testing

```bash
# Unit tests
./gradlew test --tests "LocationSimulatorTest"
./gradlew test --tests "SimulationViewModelTest"

# All tests
./gradlew test
```

## Production Checklist

- [ ] Update SimulationViewModelTest
- [ ] Extract LocationServiceController interface
- [ ] Add integration tests
- [ ] Consider Snackbar for errors
- [ ] Enable ProGuard/R8
- [ ] Add crash reporting
- [ ] Performance profiling
- [ ] Accessibility audit

## Useful Links

- [Full Architecture Doc](./ARCHITECTURE.md)
- [Error Handling Guide](../ERROR_HANDLING.md)
- [Debugging Logs Guide](../DEBUGGING_LOGS.md)
- [Architecture Review](../ARCHITECTURE_REVIEW.md)

