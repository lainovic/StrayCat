package com.lainovic.tomtom.straycat

import android.content.Context
import android.content.Intent
import android.util.Log
import kotlinx.coroutines.flow.StateFlow

/**
 * Facade for interacting with LocationService.
 *
 * This class:
 * 1. Sends commands to the service (start, stop, pause, resume)
 * 2. Exposes the service state from LocationServiceStateManager
 *
 * It does NOT manage state - that's handled by LocationServiceStateManager singleton.
 */
class LocationServiceFacade(
    private val context: Context,
    private val serviceClass: Class<*>,
) {
    init {
        Log.d(TAG.simpleName, "LocationServiceFacade created with service: ${serviceClass.simpleName}")
    }

    fun start() {
        Log.d(TAG.simpleName, "start() called")
        sendServiceIntent(LocationService.ACTION_START)
    }

    fun pause() {
        Log.d(TAG.simpleName, "pause() called")
        sendServiceIntent(LocationService.ACTION_PAUSE)
    }

    fun resume() {
        Log.d(TAG.simpleName, "resume() called")
        sendServiceIntent(LocationService.ACTION_RESUME)
    }

    fun stop() {
        Log.d(TAG.simpleName, "stop() called")
        sendServiceIntent(LocationService.ACTION_STOP)
    }

    private fun sendServiceIntent(action: String) {
        Log.d(TAG.simpleName, "sendServiceIntent: action=$action")
        val intent = Intent(context, serviceClass).apply {
            setAction(action)
        }
        context.startForegroundService(intent)
    }

    companion object {
        val TAG = LocationServiceFacade::class
    }
}
