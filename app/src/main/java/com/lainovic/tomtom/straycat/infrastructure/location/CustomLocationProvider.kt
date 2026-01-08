package com.lainovic.tomtom.straycat.infrastructure.location

import android.Manifest
import android.location.LocationManager
import androidx.annotation.RequiresPermission
import com.lainovic.tomtom.straycat.domain.location.observeLocations
import com.lainovic.tomtom.straycat.shared.toGeoLocation
import com.tomtom.sdk.location.GeoLocation
import com.tomtom.sdk.location.LocationProvider
import com.tomtom.sdk.location.OnLocationUpdateListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.util.concurrent.CopyOnWriteArraySet

class CustomLocationProvider(
    private val locationManager: LocationManager,
    private val configuration: GpsConfiguration,
    private val backgroundScope: CoroutineScope = CoroutineScope(
        Dispatchers.Main.limitedParallelism(1)
    ),
) : LocationProvider {
    private var locationJob: Job? = null

    override val lastKnownLocation: GeoLocation?
        @RequiresPermission(
            anyOf = [
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            ]
        )
        get() = locationManager
            .getLastKnownLocation(LocationManager.GPS_PROVIDER)
            ?.toGeoLocation()

    private val listeners: MutableSet<OnLocationUpdateListener> =
        CopyOnWriteArraySet()

    override fun disable() {
        locationJob?.cancel()
        locationJob = null
    }

    @RequiresPermission(
        allOf = [
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        ]
    )
    override fun enable() {
        locationManager
            .observeLocations(configuration = configuration)
            .onEach {
                val geoLocation = it.toGeoLocation()
                listeners.forEach { listener ->
                    listener.onLocationUpdate(geoLocation)
                }
            }
            .launchIn(backgroundScope)
    }

    override fun addOnLocationUpdateListener(listener: OnLocationUpdateListener) {
        listeners.add(listener)
    }

    override fun removeOnLocationUpdateListener(listener: OnLocationUpdateListener) {
        listeners.remove(listener)
    }

    override fun close() {
        disable()
        listeners.clear()
    }
}