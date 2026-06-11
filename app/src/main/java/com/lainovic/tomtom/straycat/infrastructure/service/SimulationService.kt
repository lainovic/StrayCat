package com.lainovic.tomtom.straycat.infrastructure.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.IBinder
import com.lainovic.tomtom.straycat.R
import com.lainovic.tomtom.straycat.domain.simulation.LocationSimulator
import com.lainovic.tomtom.straycat.domain.simulation.SimulationEvent
import com.lainovic.tomtom.straycat.infrastructure.analytics.InMemorySimulationEventBus
import com.lainovic.tomtom.straycat.infrastructure.location.MockLocationProvider
import com.lainovic.tomtom.straycat.infrastructure.logging.AndroidLogger
import com.lainovic.tomtom.straycat.infrastructure.shared.getLocationManager
import com.lainovic.tomtom.straycat.infrastructure.simulation.AppGraph
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SimulationService : Service() {
    private val locationManager: LocationManager by lazy {
        getLocationManager()
    }

    private val mockLocationProvider by lazy {
        MockLocationProvider(locationManager)
    }

    private val simulator by lazy { createSimulator() }
    private val backgroundScope = CoroutineScope(
        Dispatchers.Default +
                CoroutineName("SimulationServiceBackgroundScope")
    )

    override fun onBind(intent: Intent?): IBinder? {
        AndroidLogger.d(TAG, "onBind() called")
        return null
    }

    override fun onCreate() {
        super.onCreate()
        AndroidLogger.d(TAG, "onCreate() called")
        startForegroundNotification()
        mockLocationProvider.setup()
        AndroidLogger.d(TAG, "onCreate() completed")
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int, startId: Int
    ): Int {
        if (intent?.action == null) {
            AndroidLogger.w(TAG, "Received onStartCommand with null action")
            return START_STICKY
        }

        AndroidLogger.d(TAG, "onStartCommand() called")
        AndroidLogger.d(TAG, "  intent: $intent")
        AndroidLogger.d(TAG, "  action: ${intent?.action}")
        AndroidLogger.d(TAG, "  flags: $flags, startId: $startId")

        val action = intent.action
        try {
            AndroidLogger.d(TAG, "Processing action: $action")
            when (action) {
                ACTION_START -> {
                    AndroidLogger.d(TAG, "ACTION_START: Starting simulation")
                    simulator.start()
                    AndroidLogger.i(TAG, "ACTION_START: Simulation started")
                }

                ACTION_PAUSE -> {
                    AndroidLogger.d(TAG, "ACTION_PAUSE: Pausing simulation")
                    simulator.pause()
                    AndroidLogger.i(TAG, "ACTION_PAUSE: Simulation paused")
                }

                ACTION_RESUME -> {
                    AndroidLogger.d(TAG, "ACTION_RESUME: Resuming simulation")
                    simulator.resume()
                    AndroidLogger.i(TAG, "ACTION_RESUME: Simulation resumed")
                }

                ACTION_SEEK -> {
                    val f = intent.getFloatExtra(EXTRA_SEEK_FRACTION, 0f)
                    backgroundScope.launch { simulator.seekTo(f) }
                }

                ACTION_STOP -> {
                    AndroidLogger.d(TAG, "ACTION_STOP: Stopping simulation")
                    simulator.stop()
                    stopSelf()
                    AndroidLogger.i(TAG, "ACTION_STOP: Simulation stopped")
                }

                else -> {
                    AndroidLogger.w(TAG, "Unknown action received: $action")
                }
            }
        } catch (e: Exception) {
            AndroidLogger.e(TAG, "Error in onStartCommand", e)
            stopSelf()
        }

        AndroidLogger.d(TAG, "onStartCommand() returning START_STICKY")
        return START_STICKY
    }

    private fun createSimulator(): LocationSimulator {
        val handler = CoroutineExceptionHandler { _, throwable ->
            AndroidLogger.e(TAG, "Exception caught in simulator", throwable)
        }

        return LocationSimulator(
            onLocation = this::onLocation,
            onComplete = {
                AndroidLogger.i(TAG, "Simulation completed")
                InMemorySimulationEventBus.pushEvent(SimulationEvent.Stopped)
            },
            backgroundScope = CoroutineScope(
                backgroundScope.coroutineContext + handler
            ),
            configManager = AppGraph.configStore,
            logger = AndroidLogger,
        ).also {
            AndroidLogger.d(TAG, "LocationSimulator created successfully")
        }
    }

    private fun onLocation(location: Location) {
        try {
            AndroidLogger.d(TAG, "onLocation() called with tick: $location")
            locationManager.setTestProviderLocation(LocationManager.GPS_PROVIDER, location)
        } catch (e: SecurityException) {
            AndroidLogger.e(TAG, "Failed to set mock location", e)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        AndroidLogger.d(TAG, "onDestroy() called")
        simulator.stop()
        mockLocationProvider.cleanup()
        AndroidLogger.d(TAG, "onDestroy() completed")
    }

    private fun startForegroundNotification() {
        AndroidLogger.d(TAG, "startForegroundNotification() called")
        val channelId = "stray_cat_location_simulation"

        val channel = NotificationChannel(
            channelId,
            "StrayCat Location Simulation",
            NotificationManager.IMPORTANCE_LOW,
        )

        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
        AndroidLogger.d(TAG, "Notification channel created")

        val notification = Notification.Builder(this, channelId)
            .setContentTitle("StrayCat")
            .setContentText("Simulating location updates")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()

        startForeground(notification)
    }

    private fun startForeground(notification: Notification) {
        AndroidLogger.d(TAG, "startForeground() called")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                1,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
            )
        } else {
            startForeground(1, notification)
        }
        AndroidLogger.d(TAG, "startForeground() completed")
    }

    companion object {
        val TAG = SimulationService::class.simpleName!!
        const val ACTION_START = "com.lainovic.tomtom.straycat.action.START"
        const val ACTION_STOP = "com.lainovic.tomtom.straycat.action.STOP"
        const val ACTION_PAUSE = "com.lainovic.tomtom.straycat.action.PAUSE"
        const val ACTION_RESUME = "com.lainovic.tomtom.straycat.action.RESUME"
        const val ACTION_SEEK = "com.lainovic.tomtom.straycat.action.SEEK"
        const val EXTRA_SEEK_FRACTION = "seek_fraction"
    }
}