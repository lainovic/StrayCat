package com.lainovic.tomtom.straycat.ui.simulation

import android.content.Context
import android.location.Location
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lainovic.tomtom.straycat.ui.showToast
import com.tomtom.sdk.location.LocationProvider
import com.tomtom.sdk.routing.RoutePlanner

@Composable
fun SimulationScreen(
    context: Context,
    routePlanner: RoutePlanner,
    locationProvider: LocationProvider,
    modifier: Modifier
) {
    val viewModel: SimulationViewModel = viewModel(
        factory = SimulationViewModel.Factory(routePlanner)
    )

    val origin by viewModel.origin
    val destination by viewModel.destination
    val locations by viewModel.points
    val errorMessage by viewModel.errorMessage

    val snackbarHostState = remember { SnackbarHostState() }

    Box(modifier = modifier) {
        SimulationContent(
            context = context,
            origin = origin,
            destination = destination,
            locations = locations,
            locationProvider = locationProvider,
            onOriginSelected = { location, address ->
                viewModel.setOrigin(location)
                context.showToast("Origin set to: $address")
            },
            onDestinationSelected = { location, address ->
                viewModel.setDestination(location)
                context.showToast("Destination set to: $address")
            },
            onMapLongPress = { location ->
                when {
                    origin == null -> {
                        viewModel.setOrigin(location)
                        context.showToast("Origin set to: ${location.prettyPrint()}")
                    }

                    destination == null -> {
                        viewModel.setDestination(location)
                        context.showToast("Destination set to: ${location.prettyPrint()}")
                    }

                    else -> {
                        viewModel.clearRoute()
                        context.showToast("Route cleared")
                    }
                }
            },
            modifier = modifier
        )
    }

    ErrorEffect(errorMessage, context)

    LaunchedEffect(Unit) {
        viewModel.errorEvents.collect { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
        }
    }
}

@Composable
private fun ErrorEffect(
    errorMessage: String?,
    context: Context
) {
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            context.showToast("Error: $it")
        }
    }
}

private fun Location.prettyPrint() =
    "%.2f, %.2f".format(latitude, longitude)