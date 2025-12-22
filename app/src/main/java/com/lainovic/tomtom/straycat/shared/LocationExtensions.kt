package com.lainovic.tomtom.straycat.shared

import android.location.Location
import android.location.LocationManager
import com.tomtom.sdk.location.GeoPoint
import com.tomtom.sdk.routing.route.RoutePoint

fun Location.toGeoPoint(): GeoPoint = GeoPoint(
    latitude = latitude,
    longitude = longitude
)

fun GeoPoint.toLocation(
    provider: String = LocationManager.GPS_PROVIDER,
): Location = Location(provider).apply {
    latitude = this@toLocation.latitude
    longitude = this@toLocation.longitude
}

fun List<Location>.toGeoPoints(): List<GeoPoint> = map { it.toGeoPoint() }

fun List<GeoPoint>.toLocations(provider: String = "gps"): List<Location> =
    map { it.toLocation(provider) }

fun RoutePoint.toLocation(
    provider: String = LocationManager.GPS_PROVIDER,
): Location = Location(provider).apply {
    latitude = coordinate.latitude
    longitude = coordinate.longitude
}