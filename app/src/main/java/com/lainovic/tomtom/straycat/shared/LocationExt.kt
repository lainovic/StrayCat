package com.lainovic.tomtom.straycat.shared

import android.location.Location
import android.location.LocationManager
import android.os.SystemClock
import com.lainovic.tomtom.straycat.domain.location.SimulationPoint
import com.tomtom.quantity.Angle
import com.tomtom.quantity.Distance
import com.tomtom.sdk.common.valueOr
import com.tomtom.sdk.location.GeoLocation
import com.tomtom.sdk.location.GeoPoint
import com.tomtom.sdk.routing.route.Route
import com.tomtom.sdk.routing.route.RoutePoint
import kotlin.time.Duration

fun Location.toGeoPoint(): GeoPoint = GeoPoint(
    latitude = latitude,
    longitude = longitude
)

fun Location.toGeoLocation() = GeoLocation(
    position = this.toGeoPoint(),
    accuracy = Distance.meters(if (hasAccuracy()) this.accuracy.toDouble() else 0.0),
    course = Angle.degrees(if (hasBearing()) this.bearing.toDouble() else 0.0),
    time = this.time,
    elapsedRealtimeNanos = this.elapsedRealtimeNanos,
)


fun GeoPoint.toLocation(
    provider: String = LocationManager.GPS_PROVIDER,
): Location = Location(provider).apply {
    latitude = this@toLocation.latitude
    longitude = this@toLocation.longitude
}

fun List<Location>.toGeoPoints(): List<GeoPoint> = map { it.toGeoPoint() }

fun List<GeoPoint>.toMapLocations(provider: String = "gps"): List<Location> =
    map { it.toLocation(provider) }

fun RoutePoint.toLocation(
    provider: String = LocationManager.GPS_PROVIDER,
): Location = Location(provider).apply {
    latitude = coordinate.latitude
    longitude = coordinate.longitude
}

fun RoutePoint.toSimulationPoint(
    elapsedTravelTime: Duration? = null,
    speed: Double? = null
) = SimulationPoint(
    location = toLocation(),
    elapsedTravelTime = elapsedTravelTime,
    speed = speed,
)

fun Route.calculateSpeedBetweenPoints(
    startOffset: Distance,
    endOffset: Distance
): Double? {
    val startTime =
        travelTimeUpTo(startOffset).valueOr { null }
            ?: return null
    val endTime =
        travelTimeUpTo(endOffset).valueOr { null }
            ?: return null
    val distance = (endOffset - startOffset).inMeters()
    val timeDelta = (endTime - startTime).inWholeSeconds

    return if (timeDelta > 0) distance / timeDelta else null
}

fun List<SimulationPoint>.toMapLocations() = map { it.location }

fun SimulationPoint.toLocation(simulationStartTime: Long) =
    Location(LocationManager.GPS_PROVIDER).apply {
        latitude = location.latitude
        longitude = location.longitude
        time = elapsedTravelTime?.let { simulationStartTime + it.inWholeMilliseconds } ?: System.currentTimeMillis()
        elapsedRealtimeNanos = SystemClock.elapsedRealtimeNanos()
        accuracy = 10.0f

        if (location.hasAltitude()) altitude = location.altitude
        if (location.hasBearing()) bearing = location.bearing

        this@toLocation.speed?.toFloat()?.let { speed = it }
    }
