package com.lainovic.tomtom.straycat.infrastructure.shared

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.google.android.libraries.places.api.Places
import com.lainovic.tomtom.straycat.BuildConfig
import com.lainovic.tomtom.straycat.domain.shared.toGpsConfiguration
import com.lainovic.tomtom.straycat.domain.simulation.SimulationConfiguration
import com.lainovic.tomtom.straycat.infrastructure.location.CustomLocationProvider
import com.tomtom.sdk.location.LocationProvider
import com.tomtom.sdk.map.display.MapOptions
import com.tomtom.sdk.routing.online.OnlineRoutePlanner

@Composable
fun rememberCustomLocationProvider(
    context: Context,
    configuration: SimulationConfiguration = SimulationConfiguration(),
): LocationProvider = remember {
    CustomLocationProvider(
        locationManager = context.getLocationManager(),
        configuration = configuration.toGpsConfiguration(),
    )
}

@Composable
fun rememberRoutePlanner(context: Context) =
    remember {
        OnlineRoutePlanner.create(
            context = context,
            apiKey = BuildConfig.TOMTOM_API_KEY,
        )
    }

@Composable
fun rememberMapOptions() =
    remember { MapOptions(mapKey = BuildConfig.TOMTOM_API_KEY) }

fun initPlaces(context: Context) {
    if (!Places.isInitialized()) {
        Places.initializeWithNewPlacesApiEnabled(
            context,
            BuildConfig.GOOGLE_PLACES_API_KEY,
        )
    }
}