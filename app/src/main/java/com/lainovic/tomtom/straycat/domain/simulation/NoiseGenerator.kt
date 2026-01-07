package com.lainovic.tomtom.straycat.domain.simulation

import kotlin.math.cos

object NoiseGenerator {
    fun generateNoise(
        noiseLevelInMeters: Float,
    ): Pair<Float, Float> {
        val noiseLat =
            (Math.random().toFloat() - 0.5f) * 2 * noiseLevelInMeters /
                    111320f
        val noiseLon =
            (Math.random().toFloat() - 0.5f) * 2 * noiseLevelInMeters /
                    (111320f * cos(Math.toRadians(0.0))).toFloat()
        return Pair(noiseLat, noiseLon)
    }
}