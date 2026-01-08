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
import com.lainovic.tomtom.straycat.domain.simulation.SimulationConfiguration
import com.lainovic.tomtom.straycat.infrastructure.location.MockLocationProvider
import com.lainovic.tomtom.straycat.infrastructure.shared.getLocationManager
import com.lainovic.tomtom.straycat.infrastructure.logging.Logger
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

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
                CoroutineName("LocationPlayerServiceScope")
    )

    override fun onBind(intent: Intent?): IBinder? {
        Logger.d(TAG, "onBind() called")
        return null
    }

    override fun onCreate() {
        super.onCreate()
        Logger.d(TAG, "onCreate() called")
        startForegroundNotification()
        mockLocationProvider.setup()
        Logger.d(TAG, "onCreate() completed")
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int, startId: Int
    ): Int {
        Logger.d(TAG, "onStartCommand() called")
        Logger.d(TAG, "  intent: $intent")
        Logger.d(TAG, "  action: ${intent?.action}")
        Logger.d(TAG, "  flags: $flags, startId: $startId")

        try {
            val action = requireNotNull(intent?.action) {
                "Intent action is null"
            }
            Logger.d(TAG, "Processing action: $action")

            when (action) {
                ACTION_START -> {
                    Logger.d(TAG, "ACTION_START: Starting simulation")
                    simulator.start()
                    Logger.i(TAG, "ACTION_START: Simulation started")
                }

                ACTION_PAUSE -> {
                    Logger.d(TAG, "ACTION_PAUSE: Pausing simulation")
                    simulator.pause()
                    Logger.i(TAG, "ACTION_PAUSE: Simulation paused")
                }

                ACTION_RESUME -> {
                    Logger.d(TAG, "ACTION_RESUME: Resuming simulation")
                    simulator.resume()
                    Logger.i(TAG, "ACTION_RESUME: Simulation resumed")
                }

                ACTION_STOP -> {
                    Logger.d(TAG, "ACTION_STOP: Stopping simulation")
                    simulator.stop()
                    stopSelf()
                    Logger.i(TAG, "ACTION_STOP: Simulation stopped")
                }

                ACTION_UPDATE_CONFIG -> {
                    Logger.d(TAG, "ACTION_UPDATE_CONFIG: Updating configuration")
                    val config = getConfigFromIntent(intent)
                    updateConfiguration(config)
                    Logger.i(TAG, "ACTION_UPDATE_CONFIG: Configuration updated to $config")
                }

                else -> {
                    Logger.w(TAG, "Unknown action received: $action")
                }
            }
        } catch (e: Exception) {
            Logger.e(TAG, "Error in onStartCommand", e)
            stopSelf()
        }

        Logger.d(TAG, "onStartCommand() returning START_STICKY")
        return START_STICKY
    }

    private fun createSimulator(): LocationSimulator {
        val handler = CoroutineExceptionHandler { _, throwable ->
            Logger.e(TAG, "Exception caught in simulator", throwable)
        }

        return LocationSimulator(
            onTick = this::onTick,
            onComplete = { Logger.i(TAG, "Simulation completed") },
            backgroundScope = CoroutineScope(
                backgroundScope.coroutineContext + handler
            )
        ).also {
            Logger.d(TAG, "LocationSimulator created successfully")
        }
    }

    private fun onTick(location: Location) {
        try {
            Logger.d(TAG, "onTick() called with tick: $location")
            locationManager.setTestProviderLocation(LocationManager.GPS_PROVIDER, location)
        } catch (e: SecurityException) {
            Logger.e(TAG, "Failed to set mock location", e)
        }
    }

    private fun updateConfiguration(config: SimulationConfiguration) {
        simulator.update(config)
    }

    private fun getConfigFromIntent(intent: Intent): SimulationConfiguration {
        val config =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra(EXTRA_CONFIG, SimulationConfiguration::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableExtra(EXTRA_CONFIG)
            } ?: throw IllegalArgumentException("Missing or invalid configuration extra")

        return config
    }

    override fun onDestroy() {
        super.onDestroy()
        Logger.d(TAG, "onDestroy() called")
        simulator.stop()
        mockLocationProvider.cleanup()
        Logger.d(TAG, "onDestroy() completed")
    }

    private fun startForegroundNotification() {
        Logger.d(TAG, "startForegroundNotification() called")
        val channelId = "stray_cat_location_simulation"

        val channel = NotificationChannel(
            channelId,
            "StrayCat Location Simulation",
            NotificationManager.IMPORTANCE_LOW,
        )

        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
        Logger.d(TAG, "Notification channel created")

        val notification = Notification.Builder(this, channelId)
            .setContentTitle("StrayCat")
            .setContentText("Simulating location updates")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()

        startForeground(notification)
    }

    private fun startForeground(notification: Notification) {
        Logger.d(TAG, "startForeground() called")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                1,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
            )
        } else {
            startForeground(1, notification)
        }
        Logger.d(TAG, "startForeground() completed")
    }

    companion object {
        val TAG = SimulationService::class.simpleName!!
        const val ACTION_START = "com.lainovic.tomtom.straycat.action.START"
        const val ACTION_STOP = "com.lainovic.tomtom.straycat.action.STOP"
        const val ACTION_PAUSE = "com.lainovic.tomtom.straycat.action.PAUSE"
        const val ACTION_RESUME = "com.lainovic.tomtom.straycat.action.RESUME"
        const val ACTION_UPDATE_CONFIG = "com.lainovic.tomtom.straycat.action.UPDATE_CONFIG"
        const val EXTRA_CONFIG = "com.lainovic.tomtom.straycat.extra.CONFIG"
    }
}