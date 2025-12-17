package com.lainovic.tomtom.straycat

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow

abstract class LocationService<T> : Service() {
    protected abstract fun createLocationFlow(): Flow<T>

    val handler = CoroutineExceptionHandler { _, t ->
        Log.e(TAG.simpleName, "Exception caught in simulator", t)
        broadcastState(LocationServiceState.Error(t.message ?: "Flow collection error"))
    }

    private val simulator by lazy {
        Log.d(TAG.simpleName, "Creating LocationSimulator (lazy initialization)")
        LocationSimulator(
            locationFlow = createLocationFlow(),
            onTick = { tick -> Log.i(TAG.simpleName, "Tick: $tick") },
            onComplete = { Log.i(TAG.simpleName, "Simulation completed") },
            backgroundScope = CoroutineScope(
                Dispatchers.Default.limitedParallelism(1) +
                        handler
            )
        ).also {
            Log.d(TAG.simpleName, "LocationSimulator created successfully")
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
        Log.d(TAG.simpleName, "onCreate() completed")
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
        Log.d(TAG.simpleName, "onDestroy() completed")
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

        Log.d(TAG.simpleName, "Calling startForeground()")
        startForeground(1, notification)
        Log.d(TAG.simpleName, "startForeground() completed")
    }

    private fun broadcastState(state: LocationServiceState) {
        Log.d(TAG.simpleName, "broadcastState() called with state: $state")
        // Update the singleton state manager
        LocationServiceStateProvider.updateState(state)
    }

    companion object {
        val TAG = this::class
        const val ACTION_START = "com.lainovic.tomtom.straycat.action.START"
        const val ACTION_STOP = "com.lainovic.tomtom.straycat.action.STOP"
        const val ACTION_PAUSE = "com.lainovic.tomtom.straycat.action.PAUSE"
        const val ACTION_RESUME = "com.lainovic.tomtom.straycat.action.RESUME"
    }
}