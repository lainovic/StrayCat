package com.lainovic.tomtom.straycat.domain.service

import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * Facade for interacting with the location player service.
 * Exposed the commands to the player service (start, stop, pause, resume)
 */
class LocationPlayerServiceFacade(
    private val context: Context,
    private val serviceClass: Class<*>,
) {
    fun start() {
        Log.d(TAG.simpleName, "start() called")
        sendServiceIntent(LocationPlayerService.ACTION_START)
    }

    fun pause() {
        Log.d(TAG.simpleName, "pause() called")
        sendServiceIntent(LocationPlayerService.ACTION_PAUSE)
    }

    fun resume() {
        Log.d(TAG.simpleName, "resume() called")
        sendServiceIntent(LocationPlayerService.ACTION_RESUME)
    }

    fun stop() {
        Log.d(TAG.simpleName, "stop() called")
        sendServiceIntent(LocationPlayerService.ACTION_STOP)
    }

    private fun sendServiceIntent(action: String) {
        Log.d(TAG.simpleName, "sendServiceIntent: action=$action")
        val intent = Intent(context, serviceClass)
            .apply {
                setAction(action)
            }
        context.startForegroundService(intent)
    }

    companion object {
        val TAG = LocationPlayerServiceFacade::class
    }
}
