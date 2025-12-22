package com.lainovic.tomtom.straycat.ui.route_builder

import android.content.Context
import android.location.Location
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lainovic.tomtom.straycat.BuildConfig
import com.lainovic.tomtom.straycat.ui.showToast
import com.tomtom.sdk.routing.RoutePlanner
import com.tomtom.sdk.routing.online.OnlineRoutePlanner

@Composable
fun RouteBuilderScreen(
    context: Context,
    onNavigateToPlayer: () -> Unit,
    onLocationsUpdated: (List<Location>) -> Unit = { _ -> },
    modifier: Modifier = Modifier
) {
    val routePlanner: RoutePlanner = remember {
        OnlineRoutePlanner.create(
            context = context,
            apiKey = BuildConfig.TOMTOM_API_KEY,
        )
    }

    val viewModel: RouteBuilderViewModel = viewModel(
        factory = RouteBuilderViewModel.Factory(routePlanner)
    )

    val origin by viewModel.origin
    val destination by viewModel.destination
    val locations by viewModel.points
    val errorMessage by viewModel.errorMessage

    RouteBuilderContent(
        modifier = modifier,
        origin = origin,
        destination = destination,
        points = locations,
        onOriginSelected = { location, address ->
            viewModel.setOrigin(location)
            showToast(context, "Origin set to: $address")
        },
        onDestinationSelected = { location, address ->
            viewModel.setDestination(location)
            showToast(context, "Destination set to: $address")
        },
        onMapLongPress = { location ->
            when {
                origin == null -> {
                    viewModel.setOrigin(location)
                    showToast(context, "Origin set to: ${location.prettyFormat()}")
                }

                destination == null -> {
                    viewModel.setDestination(location)
                    showToast(context, "Destination set to: ${location.prettyFormat()}")
                }

                else -> {
                    viewModel.clearRoute()
                    showToast(context, "Route cleared")
                }
            }
        },
        onNavigateToSimulation = onNavigateToPlayer
    )

    LaunchedEffect(locations) {
        onLocationsUpdated(locations)
    }

    ErrorEffect(errorMessage, context)
}

@Composable
private fun ErrorEffect(
    errorMessage: String?,
    context: Context
) {
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            showToast(context, "Error: $it")
        }
    }
}

private fun Location.prettyFormat(): String =
    "%.2f, %.2f".format(latitude, longitude)