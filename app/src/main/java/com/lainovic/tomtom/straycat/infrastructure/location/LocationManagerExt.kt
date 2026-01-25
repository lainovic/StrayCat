package com.lainovic.tomtom.straycat.infrastructure.location

import android.Manifest
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.annotation.RequiresPermission
import com.lainovic.tomtom.straycat.infrastructure.logging.AndroidLogger
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.callbackFlow

@RequiresPermission(
    allOf = [
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ]
)
fun LocationManager.observeLocations(
    provider: String = LocationManager.GPS_PROVIDER,
    configuration: GpsConfiguration,
): Flow<Location> = callbackFlow {
    val tag = "LocationManagerExt"

    val listener = LocationListener { location ->
        trySend(location)
    }

    AndroidLogger.d(tag, "Requesting location updates for provider=$provider")
    requestLocationUpdates(
        provider,
        configuration.minTimeInterval.inWholeMilliseconds,
        configuration.minDistance.inMeters().toFloat(),
        listener,
    )

    awaitClose {
        removeUpdates(listener)
        AndroidLogger.d(tag, "Location updates removed for provider=$provider")
    }
}.buffer(capacity = Channel.CONFLATED)