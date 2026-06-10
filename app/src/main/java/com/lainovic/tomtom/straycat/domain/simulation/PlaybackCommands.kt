package com.lainovic.tomtom.straycat.domain.simulation

/**
 * Domain-level interface (Application Service) for controlling the simulation.
 * This abstracts away the implementation details (like Android Services or Facades)
 * from the UI and ViewModel layers.
 */
interface PlaybackCommands {
    fun start()
    fun pause()
    fun resume()
    fun stop()
}
