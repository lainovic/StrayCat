package com.lainovic.tomtom.straycat.infrastructure.service

import android.content.Context
import android.content.Intent
import com.lainovic.tomtom.straycat.domain.simulation.SimulationConfiguration
import com.lainovic.tomtom.straycat.domain.simulation.SimulationController
import com.lainovic.tomtom.straycat.infrastructure.logging.AndroidLogger

/**
 * Implementation of [SimulationController] that communicates with the [SimulationService]
 * via Android Intents.
 */
class ServiceSimulationController(
    private val context: Context,
    private val serviceClass: Class<*>,
) : SimulationController {
    override fun start() {
        AndroidLogger.d(TAG, "start() called")
        sendServiceIntent(SimulationService.ACTION_START)
    }

    override fun pause() {
        AndroidLogger.d(TAG, "pause() called")
        sendServiceIntent(SimulationService.ACTION_PAUSE)
    }

    override fun resume() {
        AndroidLogger.d(TAG, "resume() called")
        sendServiceIntent(SimulationService.ACTION_RESUME)
    }

    override fun stop() {
        AndroidLogger.d(TAG, "stop() called")
        sendServiceIntent(SimulationService.ACTION_STOP)
    }

    override fun updateConfiguration(config: SimulationConfiguration) {
        val intent = Intent(context, serviceClass).apply {
            action = SimulationService.Companion.ACTION_UPDATE_CONFIG
            putExtra(SimulationService.Companion.EXTRA_CONFIG, config)
        }
        context.startService(intent)
    }

    private fun sendServiceIntent(action: String) {
        AndroidLogger.d(TAG, "sendServiceIntent: action=$action")
        val intent = Intent(context, serviceClass)
            .apply {
                setAction(action)
            }
        context.startForegroundService(intent)
    }

    companion object {
        val TAG = ServiceSimulationController::class.simpleName!!
    }
}