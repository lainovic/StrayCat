package com.lainovic.tomtom.straycat.application

import android.content.Context
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.google.android.libraries.places.api.Places
import com.lainovic.tomtom.straycat.BuildConfig
import com.lainovic.tomtom.straycat.ui.rememberCustomLocationProvider
import com.lainovic.tomtom.straycat.ui.simulation.SimulationScreen
import com.tomtom.quantity.Distance
import com.tomtom.sdk.routing.online.OnlineRoutePlanner
import com.tomtom.sdk.location.LocationProviderConfig
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun StrayCatApp(context: Context) {
    if (!Places.isInitialized()) {
        Places.initializeWithNewPlacesApiEnabled(
            context,
            BuildConfig.GOOGLE_PLACES_API_KEY,
        )
    }

    val routePlanner = remember {
        OnlineRoutePlanner.create(
            context = context,
            apiKey = BuildConfig.TOMTOM_API_KEY,
        )
    }

    val locationProvider = rememberCustomLocationProvider(
        context =  context,
        locationProviderConfig = LocationProviderConfig(
            minTimeInterval = 250L.milliseconds,
            minDistance = Distance.meters(5.0)
        )
    )

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        SimulationScreen(
            context = context,
            routePlanner = routePlanner,
            locationProvider = locationProvider,
            modifier = Modifier
                .padding(innerPadding)
        )
    }
}