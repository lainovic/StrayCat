package com.lainovic.tomtom.straycat

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.location.Criteria
import android.location.Location
import android.location.LocationManager
import android.location.provider.ProviderProperties
import android.os.Build
import android.os.IBinder
import android.util.Log
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow

abstract class LocationService : Service() {
    protected abstract fun observeLocations(): Flow<Location>

    protected val locationManager: LocationManager by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            getSystemService(LocationManager::class.java)
        } else {
            @Suppress("DEPRECATION")
            getSystemService(LOCATION_SERVICE) as LocationManager
        }
    }

    private val simulator by lazy {
        val handler = CoroutineExceptionHandler { _, throwable ->
            Log.e(TAG.simpleName, "Exception caught in simulator", throwable)
            broadcastState(LocationServiceState.Error(throwable.message ?: "Flow collection error"))
        }

        Log.d(TAG.simpleName, "Creating LocationSimulator (lazy initialization)")
        LocationSimulator(
            locationFlow = observeLocations(),
            onTick = this::onTick,
            onComplete = { Log.i(TAG.simpleName, "Simulation completed") },
            backgroundScope = CoroutineScope(
                Dispatchers.Default.limitedParallelism(1) + handler
            )
        ).also {
            Log.d(TAG.simpleName, "LocationSimulator created successfully")
        }
    }

    private fun onTick(location: Location) {
        try {
            Log.d(TAG.simpleName, "onTick() called with tick: $location")
            locationManager.setTestProviderLocation(LocationManager.GPS_PROVIDER, location)
        } catch (e: SecurityException) {
            Log.e(TAG.simpleName, "Failed to set mock location", e)
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        Log.d(TAG.simpleName, "onBind() called")
        return null
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG.simpleName, "onCreate() called")
        startForegroundNotification()
        setupMockLocationProvider()
        Log.d(TAG.simpleName, "onCreate() completed")
    }

    private fun setupMockLocationProvider() {
        try {
            Log.d(TAG.simpleName, "Setting up mock location provider")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                locationManager.addTestProvider(
                    LocationManager.GPS_PROVIDER,
                    ProviderProperties.Builder()
                        .setHasNetworkRequirement(false)
                        .setHasSatelliteRequirement(false)
                        .setHasCellRequirement(false)
                        .setHasMonetaryCost(false)
                        .setHasAltitudeSupport(true)
                        .setHasSpeedSupport(true)
                        .setHasBearingSupport(true)
                        .setPowerUsage(ProviderProperties.POWER_USAGE_LOW)
                        .setAccuracy(ProviderProperties.ACCURACY_FINE)
                        .build()
                )
            } else {
                @Suppress("DEPRECATION")
                @SuppressLint("WrongConstant")
                locationManager.addTestProvider(
                    LocationManager.GPS_PROVIDER,
                    false, // requiresNetwork
                    false, // requiresSatellite
                    false, // requiresCell
                    false, // hasMonetaryCost
                    true,  // supportsAltitude
                    true,  // supportsSpeed
                    true,  // supportsBearing
                    Criteria.POWER_LOW,
                    Criteria.ACCURACY_FINE,
                )
            }
            locationManager.setTestProviderEnabled(LocationManager.GPS_PROVIDER, true)
            Log.d(TAG.simpleName, "Mock location provider enabled")
        } catch (e: SecurityException) {
            Log.e(TAG.simpleName, "Failed to enable mock location provider - missing permissions?", e)
        } catch (e: IllegalArgumentException) {
            Log.e(TAG.simpleName, "Test provider already exists or invalid parameters", e)
        }
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int, startId: Int
    ): Int {
        Log.d(TAG.simpleName, "onStartCommand() called")
        Log.d(TAG.simpleName, "  intent: $intent")
        Log.d(TAG.simpleName, "  action: ${intent?.action}")
        Log.d(TAG.simpleName, "  flags: $flags, startId: $startId")

        try {
            val action = requireNotNull(intent?.action) { "Intent action is null" }
            Log.d(TAG.simpleName, "Processing action: $action")

            when (action) {
                ACTION_START -> {
                    Log.d(TAG.simpleName, "ACTION_START: Starting simulation")
                    simulator.start()
                    Log.d(TAG.simpleName, "ACTION_START: Simulator.start() called")
                    broadcastState(LocationServiceState.Running)
                    Log.d(TAG.simpleName, "ACTION_START: Broadcast sent")
                }

                ACTION_PAUSE -> {
                    Log.d(TAG.simpleName, "ACTION_PAUSE: Pausing simulation")
                    simulator.pause()
                    Log.d(TAG.simpleName, "ACTION_PAUSE: Simulator.pause() called")
                    broadcastState(LocationServiceState.Paused)
                    Log.d(TAG.simpleName, "ACTION_PAUSE: Broadcast sent")
                }

                ACTION_RESUME -> {
                    Log.d(TAG.simpleName, "ACTION_RESUME: Resuming simulation")
                    simulator.resume()
                    Log.d(TAG.simpleName, "ACTION_RESUME: Simulator.resume() called")
                    broadcastState(LocationServiceState.Running)
                    Log.d(TAG.simpleName, "ACTION_RESUME: Broadcast sent")
                }

                ACTION_STOP -> {
                    Log.d(TAG.simpleName, "ACTION_STOP: Stopping simulation")
                    simulator.stop()
                    Log.d(TAG.simpleName, "ACTION_STOP: Simulator.stop() called")
                    broadcastState(LocationServiceState.Stopped)
                    Log.d(TAG.simpleName, "ACTION_STOP: Broadcast sent, calling stopSelf()")
                    stopSelf()
                    Log.d(TAG.simpleName, "ACTION_STOP: stopSelf() called")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG.simpleName, "Error in onStartCommand", e)
            broadcastState(LocationServiceState.Error(e.message ?: "Unknown error"))
            stopSelf()
        }

        Log.d(TAG.simpleName, "onStartCommand() returning START_STICKY")
        return START_STICKY
    }

    override fun onDestroy() {
        Log.d(TAG.simpleName, "onDestroy() called")
        super.onDestroy()
        simulator.stop()
        cleanupMockLocationProvider()
        Log.d(TAG.simpleName, "onDestroy() completed")
    }

    private fun cleanupMockLocationProvider() {
        try {
            Log.d(TAG.simpleName, "Cleaning up mock location provider")
            locationManager.removeTestProvider(LocationManager.GPS_PROVIDER)
            Log.d(TAG.simpleName, "Mock location provider removed")
        } catch (e: IllegalArgumentException) {
            Log.w(TAG.simpleName, "Test provider was not registered or already removed", e)
        } catch (e: Exception) {
            Log.e(TAG.simpleName, "Failed to remove mock location provider", e)
        }
    }

    private fun startForegroundNotification() {
        Log.d(TAG.simpleName, "startForegroundNotification() called")
        val channelId = "stray_cat_location_simulation"

        val channel = NotificationChannel(
            channelId,
            "StrayCat Location Simulation",
            NotificationManager.IMPORTANCE_LOW,
        )

        val manager =
            getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
        Log.d(TAG.simpleName, "Notification channel created")

        val notification = Notification.Builder(this, channelId)
            .setContentTitle("StrayCat")
            .setContentText("Simulating location updates")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()

        startForeground(notification)
    }

    private fun startForeground(notification: Notification) {
        Log.d(TAG.simpleName, "startForeground() called")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                1,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
            )
        } else {
            startForeground(1, notification)
        }
        Log.d(TAG.simpleName, "startForeground() completed")
    }

    private fun broadcastState(state: LocationServiceState) {
        LocationServiceStateProvider.updateState(state)
    }

    companion object {
        val TAG = LocationService::class
        const val ACTION_START = "com.lainovic.tomtom.straycat.action.START"
        const val ACTION_STOP = "com.lainovic.tomtom.straycat.action.STOP"
        const val ACTION_PAUSE = "com.lainovic.tomtom.straycat.action.PAUSE"
        const val ACTION_RESUME = "com.lainovic.tomtom.straycat.action.RESUME"
    }
}