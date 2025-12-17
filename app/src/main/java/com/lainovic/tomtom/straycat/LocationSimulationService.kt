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

class LocationSimulationService : Service() {
    val handler = CoroutineExceptionHandler { _, t ->
        Log.d(TAG.simpleName, "Caught $t")
    }

    private val simulator = LocationSimulator(
        tickerFlow = tickerFlow(1_000),
        onTick = { tick -> Log.i(TAG.simpleName, "Tick: $tick") },
        onComplete = { Log.i(TAG.simpleName, "Simulation completed") },
        backgroundScope = CoroutineScope(
            Dispatchers.Default.limitedParallelism(1) +
                    handler
        )
    )

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        startForegroundNotification()
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int, startId: Int
    ): Int {
        val action =
            requireNotNull(intent?.action) { "Intent action is null" }

        when (action) {
            ACTION_START -> {
                Log.d(TAG.simpleName, "Starting simulation")
                simulator.start()
            }

            ACTION_PAUSE -> {
                Log.d(TAG.simpleName, "Pausing simulation")
                simulator.pause()
            }

            ACTION_RESUME -> {
                Log.d(TAG.simpleName, "Resuming simulation")
                simulator.resume()
            }

            ACTION_STOP -> {
                Log.d(TAG.simpleName, "Stopping simulation")
                simulator.stop()
            }
        }

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        simulator.stop()
    }

    private fun startForegroundNotification() {
        val channelId = "stray_cat_location_simulation"

        val channel = NotificationChannel(
            channelId,
            "StrayCat Location Simulation",
            NotificationManager.IMPORTANCE_LOW,
        )

        val manager =
            getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)

        val notification = Notification.Builder(this, channelId)
            .setContentTitle("StrayCat")
            .setContentText("Simulating location updates")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()

        startForeground(1, notification)
    }

    companion object {
        val TAG = LocationSimulationService::class
        const val ACTION_START = "com.lainovic.tomtom.straycat.action.START"
        const val ACTION_STOP = "com.lainovic.tomtom.straycat.action.STOP"
        const val ACTION_PAUSE = "com.lainovic.tomtom.straycat.action.PAUSE"
        const val ACTION_RESUME = "com.lainovic.tomtom.straycat.action.RESUME"
    }
}