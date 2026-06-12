package com.lainovic.tomtom.straycat.infrastructure.service

import android.content.Context
import android.content.Intent
import com.lainovic.tomtom.straycat.domain.logging.Logger
import com.lainovic.tomtom.straycat.domain.simulation.SimulationConfiguration
import com.lainovic.tomtom.straycat.domain.simulation.PlaybackCommands

/**
 * Implementation of [PlaybackCommands] that communicates with the [SimulationService]
 * via Android Intents.
 */
class ServicePlaybackCommands(
    private val context: Context,
    private val serviceClass: Class<*>,
    private val logger: Logger,
) : PlaybackCommands {
    override fun start() {
        logger.d(TAG, "start() called")
        sendServiceIntent(SimulationService.ACTION_START)
    }

    override fun pause() {
        logger.d(TAG, "pause() called")
        sendServiceIntent(SimulationService.ACTION_PAUSE)
    }

    override fun resume() {
        logger.d(TAG, "resume() called")
        sendServiceIntent(SimulationService.ACTION_RESUME)
    }

    override fun seek(fraction: Float) {
        val intent = Intent(context, serviceClass).apply {
            action = SimulationService.ACTION_SEEK
            putExtra(SimulationService.EXTRA_SEEK_FRACTION, fraction)
        }
        context.startForegroundService(intent)
    }

    override fun stop() {
        logger.d(TAG, "stop() called")
        sendServiceIntent(SimulationService.ACTION_STOP)
    }

    private fun sendServiceIntent(action: String) {
        logger.d(TAG, "sendServiceIntent: action=$action")
        val intent = Intent(context, serviceClass)
            .apply {
                setAction(action)
            }
        context.startForegroundService(intent)
    }

    companion object {
        val TAG = ServicePlaybackCommands::class.simpleName!!
    }
}