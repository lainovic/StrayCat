package com.lainovic.tomtom.straycat.domain.service

import android.util.Log
import com.tomtom.quantity.Angle
import com.tomtom.sdk.location.GeoLocation
import com.tomtom.sdk.location.GeoPoint
import com.tomtom.sdk.location.LocationProvider
import com.tomtom.sdk.location.OnLocationUpdateListener
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

internal class CustomLocationProvider(
    private val defaultLocationProvider: LocationProvider,
    configuration: SimulationConfiguration = SimulationConfiguration(),
) : LocationProvider by defaultLocationProvider {

    private val interceptors = mutableMapOf<Int, OnLocationUpdateListener>()
    private var lastLocation: GeoLocation? = null

    private val _configuration =
        MutableSimulationConfiguration(
            delayBetweenEmissions = configuration.delayBetweenEmissions,
            loopIndefinitely = configuration.loopIndefinitely,
            speedMultiplier = configuration.speedMultiplier,
            noiseLevelInMeters = configuration.noiseLevelInMeters,
        )

    val configuration: SimulationConfiguration
        get() = SimulationConfiguration(
            delayBetweenEmissions = _configuration.delayBetweenEmissions,
            loopIndefinitely = _configuration.loopIndefinitely,
            speedMultiplier = _configuration.speedMultiplier,
            noiseLevelInMeters = _configuration.noiseLevelInMeters,
        )

    override fun addOnLocationUpdateListener(listener: OnLocationUpdateListener) {
        val interceptor = OnLocationUpdateListener { location ->
            Log.d(TAG, "Intercepted location update: $location")
            listener.onLocationUpdate(postProcess(location))
        }
        defaultLocationProvider.addOnLocationUpdateListener(interceptor)
        interceptors[listener.hashCode()] = interceptor
    }

    override fun removeOnLocationUpdateListener(listener: OnLocationUpdateListener) {
        val interceptor = interceptors[listener.hashCode()]
        if (interceptor != null) {
            interceptors.remove(listener.hashCode())
            defaultLocationProvider.removeOnLocationUpdateListener(interceptor)
        }
    }

    fun updateConfiguration(configBlock: MutableSimulationConfiguration.() -> Unit) {
        _configuration.apply(configBlock)
    }

    private fun postProcess(location: GeoLocation): GeoLocation {
        var result = location
        if (_configuration.noiseLevelInMeters > 0f) {
            Log.d(
                TAG,
                "Adding noise to location: $location with noise level: ${_configuration.noiseLevelInMeters} meters"
            )
            val (noiseLat, noiseLon) =
                generateNoise(_configuration.noiseLevelInMeters)
            result = GeoLocation(
                position = GeoPoint(
                    latitude = location.position.latitude + noiseLat,
                    longitude = location.position.longitude + noiseLon
                ),
                accuracy = location.accuracy,
                speed = location.speed,
                extras = location.extras,
                course = location.course,
            )
        }

        if (result.course == null) {
            lastLocation?.let {
                result = calculateHeading(it, result)
            }
        }

        lastLocation = result
        return result
    }

    private fun generateNoise(noiseLevelInMeters: Float): Pair<Float, Float> {
        val noiseLat =
            (Math.random().toFloat() - 0.5f) * 2 * noiseLevelInMeters /
                    111320f
        val noiseLon =
            (Math.random().toFloat() - 0.5f) * 2 * noiseLevelInMeters /
                    (111320f * cos(Math.toRadians(0.0))).toFloat()
        return Pair(noiseLat, noiseLon)
    }

    private fun calculateHeading(
        from: GeoLocation,
        to: GeoLocation
    ): GeoLocation {
        val lat1 = Math.toRadians(from.position.latitude)
        val lon1 = Math.toRadians(from.position.longitude)
        val lat2 = Math.toRadians(to.position.latitude)
        val lon2 = Math.toRadians(to.position.longitude)

        val dLon = lon2 - lon1
        val y = sin(dLon) * cos(lat2)
        val x = cos(lat1) * sin(lat2) -
                sin(lat1) * cos(lat2) * cos(dLon)

        val heading = (Math.toDegrees(atan2(y, x)) + 360) % 360
        return GeoLocation(
            position = to.position,
            accuracy = to.accuracy,
            speed = to.speed,
            extras = to.extras,
            course = Angle.degrees(heading),
        )
    }

    private companion object {
        val TAG = CustomLocationProvider::class.simpleName
    }
}