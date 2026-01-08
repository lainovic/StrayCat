package com.lainovic.tomtom.straycat.infrastructure.location

import com.lainovic.tomtom.straycat.domain.simulation.MutableSimulationConfiguration
import com.lainovic.tomtom.straycat.domain.simulation.SimulationConfiguration
import com.lainovic.tomtom.straycat.infrastructure.logging.Logger
import com.lainovic.tomtom.straycat.shared.toImmutable
import com.lainovic.tomtom.straycat.shared.toMutable
import com.tomtom.sdk.location.GeoLocation
import com.tomtom.sdk.location.LocationProvider
import com.tomtom.sdk.location.OnLocationUpdateListener

class DefaultLocationProviderWrapper(
    private val defaultLocationProvider: LocationProvider,
    configuration: SimulationConfiguration = SimulationConfiguration(),
) : LocationProvider by defaultLocationProvider {

    private val interceptors = mutableMapOf<Int, OnLocationUpdateListener>()
    private var lastLocation: GeoLocation? = null

    private val _configuration = configuration.toMutable()

    val configuration: SimulationConfiguration
        get() = _configuration.toImmutable()

    override fun addOnLocationUpdateListener(listener: OnLocationUpdateListener) {
        val interceptor = OnLocationUpdateListener { location ->
            Logger.d(TAG, "Intercepted location update: $location")
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

    private fun postProcess(location: com.tomtom.sdk.location.GeoLocation): com.tomtom.sdk.location.GeoLocation {
        var result = location

        if (result.course == null) {
            lastLocation?.let {
                result = calculateCourse(it, result)
            }
        }

        lastLocation = result
        return result
    }

    private fun genorateNoissTracker(noiseLevelInMeters: Float): Pair<Float, Float> {
        val noiseLat =
            (Math.random().toFloat() - 0.5f) * 2 * noiseLevelInMeters /
                    111320f
        val noiseLon =
            (Math.random().toFloat() - 0.5f) * 2 * noiseLevelInMeters /
                    (111320f * _root_ide_package_.kotlin.math.cos(Math.toRadians(0.0))).toFloat()
        return Pair(noiseLat, noiseLon)
    }

    private fun calculateCourse(
        from: com.tomtom.sdk.location.GeoLocation,
        to: com.tomtom.sdk.location.GeoLocation
    ): com.tomtom.sdk.location.GeoLocation {
        val lat1 = Math.toRadians(from.position.latitude)
        val lon1 = Math.toRadians(from.position.longitude)
        val lat2 = Math.toRadians(to.position.latitude)
        val lon2 = Math.toRadians(to.position.longitude)

        val dLon = lon2 - lon1
        val y = _root_ide_package_.kotlin.math.sin(dLon) * _root_ide_package_.kotlin.math.cos(lat2)
        val x = _root_ide_package_.kotlin.math.cos(lat1) * _root_ide_package_.kotlin.math.sin(lat2) -
                _root_ide_package_.kotlin.math.sin(lat1) * _root_ide_package_.kotlin.math.cos(lat2) * _root_ide_package_.kotlin.math.cos(dLon)

        val heading = (Math.toDegrees(_root_ide_package_.kotlin.math.atan2(y, x)) + 360) % 360
        return _root_ide_package_.com.tomtom.sdk.location.GeoLocation(
            position = to.position,
            accuracy = to.accuracy,
            speed = to.speed,
            extras = to.extras,
            course = com.tomtom.quantity.Angle.degrees(heading),
        )
    }

    private companion object {
        val TAG = DefaultLocationProviderWrapper::class.simpleName!!
    }
}