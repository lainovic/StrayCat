<div align="center">
  <img src="docs/assets/straycat-logo.png" alt="StrayCat Logo" width="200"/>
  
  # StrayCat 🐱
  
  **Location Simulation & GPS Tracking for Android**
  
  [![Android](https://img.shields.io/badge/Platform-Android-green.svg)](https://developer.android.com)
  [![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-blue.svg)](https://kotlinlang.org)
  [![Min SDK](https://img.shields.io/badge/Min%20SDK-26-orange.svg)](https://developer.android.com/about/versions/oreo)
  [![Architecture](https://img.shields.io/badge/Architecture-MVVM-purple.svg)](https://developer.android.com/topic/architecture)
</div>

---

> [!NOTE]
> This has started as a final project on [Coroutines Mastery 2025](https://coroutinesmastery.com/)

## Overview

StrayCat is a Android application for location simulation and GPS tracking.
Perfect for testing location-based features and route simulations.

<div align="center">
  <img src="docs/assets/screenshot.png" alt="screenshot" width="400"/>
</div>

### Features

- 🎮 **Full Playback Control** - Start, pause, resume, stop, and replay
- ⏩ **Touch Seeking** - Tap or scrub the progress bar to jump to any position in the simulation
- 📍 **Route Simulation** - Replay predefined routes with realistic timing using TomTom Routing SDK
- ⚙️ **Configurable** - Speed multiplier, interpolation, noise injection, looping, and more
- 🔄 **Map display** - via TomTom Maps SDK
- 🔍 **Search Locations** - Find points of interest using Google Places Search SDK

#### Playback Controls
- **Play / Pause / Resume** - Full playback lifecycle control
- **Stop** - End the simulation; transitions to a Replay button
- **Replay** - Restart the simulation from the beginning after it ends (naturally or manually)
- **Seek** - Tap anywhere on the progress bar to jump to that position; drag to scrub in real time

#### Simulation Configuration
- **Speed Multiplier** - Control playback speed (0.1x - 10x)
- **Delay** - Add a fixed delay between location updates (in seconds)
- **Loop Mode** - Continuously replay the simulation
- **Realistic Timing** - Use actual route timing vs fixed intervals
- **Noise Level** - Add GPS signal variance (in meters)

## Getting Started

### Prerequisites
- Android device or emulator with API 26+
- Location permissions enabled

### Prerequisites
- Android Studio Hedgehog or newer
- `local.properties` in the project root with the following keys:
  ```
  tomtomApiKey=YOUR_TOMTOM_API_KEY
  googlePlacesApiKey=YOUR_GOOGLE_PLACES_API_KEY
  ```
  The build will fail with a clear error message if either key is missing.

### Permissions

The app requires the following permissions:
- **Location Access** - For GPS tracking
- **Foreground Service** - For background operation

Permissions are requested at runtime on first launch.

## Usage

### Basic Controls

1. **Start** - Begin simulation; tap the play button after setting a route
2. **Pause** - Temporarily suspend updates (state preserved)
3. **Resume** - Continue from where you paused
4. **Stop** - End current session; the button transitions to Replay
5. **Replay** - Restart the simulation from the beginning
6. **Seek** - Tap or drag the progress bar to jump to any point in the simulation

To begin simulation, long-press on the map to set origin and destination points, which will generate a route.

To clear and start over, long-press on the map again after a route has been generated.

## Roadmap

### v1.2 (Current)
- ✅ Route simulation with replay
- ✅ GPS location tracking
- ✅ Pause/resume functionality
- ✅ Speed control and looping
- ✅ Error handling and recovery
- ✅ Speed multiplier UI
- ✅ Loop mode toggle
- ✅ Realistic timing option
- ✅ GPS noise injection
- ✅ Touch-seekable progress bar (tap or scrub to any position)
- ✅ Replay button after simulation ends
- ✅ Build-time and runtime API key validation

### TODO

- 🚧 More elaborate testing strategy
- 🚧 Comprehensive documentation

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

