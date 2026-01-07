package com.lainovic.tomtom.straycat.infrastructure.service

import android.content.Context
import android.content.Intent
import com.lainovic.tomtom.straycat.domain.simulation.SimulationConfiguration
import com.lainovic.tomtom.straycat.infrastructure.logging.Logger

/**
 * Facade for interacting with the location player service.
 * Exposed the commands to the player service (start, stop, pause, resume)
 */
class SimulationServiceFacade(
    private val context: Context,
    private val serviceClass: Class<*>,
) {
    fun start() {
        Logger.d(TAG, "start() called")
        sendServiceIntent(SimulationService.ACTION_START)
    }

    fun pause() {
        Logger.d(TAG, "pause() called")
        sendServiceIntent(SimulationService.ACTION_PAUSE)
    }

    fun resume() {
        Logger.d(TAG, "resume() called")
        sendServiceIntent(SimulationService.ACTION_RESUME)
    }

    fun stop() {
        Logger.d(TAG, "stop() called")
        sendServiceIntent(SimulationService.ACTION_STOP)
    }

    fun updateConfiguration(config: SimulationConfiguration) {
        val intent = Intent(context, serviceClass).apply {
            action = SimulationService.Companion.ACTION_UPDATE_CONFIG
            putExtra(SimulationService.Companion.EXTRA_CONFIG, config)
        }
        context.startService(intent)
    }

    private fun sendServiceIntent(action: String) {
        Logger.d(TAG, "sendServiceIntent: action=$action")
        val intent = Intent(context, serviceClass)
            .apply {
                setAction(action)
            }
        context.startForegroundService(intent)
    }

    companion object {
        val TAG = SimulationServiceFacade::class.simpleName!!
    }
}