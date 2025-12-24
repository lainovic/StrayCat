package com.lainovic.tomtom.straycat.application

import android.content.Context
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.lainovic.tomtom.straycat.ui.initPlaces
import com.lainovic.tomtom.straycat.ui.rememberCustomLocationProvider
import com.lainovic.tomtom.straycat.ui.rememberRoutePlanner
import com.lainovic.tomtom.straycat.ui.simulation.SimulationScreen
import com.tomtom.quantity.Distance
import com.tomtom.sdk.location.LocationProviderConfig
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun StrayCatApp(context: Context) {
    initPlaces(context)
    val routePlanner = rememberRoutePlanner(context)
    val locationProvider = rememberCustomLocationProvider(
        context = context,
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