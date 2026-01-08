package com.lainovic.tomtom.straycat.ui.simulation

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lainovic.tomtom.straycat.domain.simulation.SimulationEvent
import com.lainovic.tomtom.straycat.infrastructure.analytics.InMemorySimulationEventBus
import com.lainovic.tomtom.straycat.infrastructure.simulation.InMemorySimulationDataRepository
import com.lainovic.tomtom.straycat.infrastructure.simulation.SimulationStateRepositorySingleton
import com.lainovic.tomtom.straycat.ui.showToast
import com.lainovic.tomtom.straycat.ui.theme.AppColors
import com.lainovic.tomtom.straycat.ui.theme.AppSizes
import com.tomtom.sdk.location.LocationProvider
import com.tomtom.sdk.routing.RoutePlanner

@Composable
fun SimulationScreen(
    context: Context,
    routePlanner: RoutePlanner,
    locationProvider: LocationProvider,
    modifier: Modifier = Modifier
) {
    val viewModel: SimulationViewModel = viewModel(
        factory = SimulationViewModel.Factory(
            routePlanner,
            InMemorySimulationEventBus,
        )
    )

    val origin by viewModel.origin
    val destination by viewModel.destination
    val points by viewModel.points
    val errorMessage by viewModel.errorMessage

    val eventBus = InMemorySimulationEventBus
    val dataRepository = InMemorySimulationDataRepository
    val stateRepository = SimulationStateRepositorySingleton

    val snackbarHostState = remember { SnackbarHostState() }
    SnackbarEffect(snackbarHostState, viewModel)

    Scaffold(
        modifier = modifier
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            SimulationContent(
                context = context,
                origin = origin,
                destination = destination,
                points = points,
                locationProvider = locationProvider,
                eventBus = eventBus,
                dataRepository = dataRepository,
                stateRepository = stateRepository,
                onOriginSelected = { location, _ ->
                    viewModel.setOrigin(location)
                    eventBus.pushEvent(SimulationEvent.OriginSet(location))
                },
                onDestinationSelected = { location, _ ->
                    viewModel.setDestination(location)
                    eventBus.pushEvent(SimulationEvent.DestinationSet(location))
                },
                onMapLongPress = { location ->
                    when {
                        origin == null -> {
                            viewModel.setOrigin(location)
                            eventBus.pushEvent(SimulationEvent.OriginSet(location))
                        }

                        destination == null -> {
                            viewModel.setDestination(location)
                            eventBus.pushEvent(SimulationEvent.DestinationSet(location))
                        }

                        else -> {
                            viewModel.clearRoute()
                            eventBus.pushEvent(SimulationEvent.RouteCleared)
                        }
                    }
                },
            )

            SnackbarHost(
                hostState = snackbarHostState,
                snackbar = { SimulationSnackbar(it) },
                modifier = Modifier
                    .align(Alignment.TopCenter)
            )
        }
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
            context.showToast("Error: $it")
        }
    }
}

@Composable
private fun SnackbarEffect(
    snackbarHostState: SnackbarHostState,
    viewModel: SimulationViewModel,
) {
    LaunchedEffect(snackbarHostState) {
        viewModel.snackbarMessages.collect { message ->
            snackbarHostState.showSnackbar(
                message = message,
                withDismissAction = true,
                duration = SnackbarDuration.Short,
            )
        }
    }
}

@Composable
private fun SimulationSnackbar(data: SnackbarData) =
    Snackbar(
        snackbarData = data,
        containerColor = AppColors.PrimaryDarker,
        contentColor = AppColors.OnPrimary,
        actionColor = AppColors.OnPrimary,
        dismissActionContentColor = AppColors.OnPrimary,
        shape = RoundedCornerShape(AppSizes.SnackbarCornerRadius),
        modifier = Modifier.padding(horizontal = AppSizes.ButtonPadding, vertical = 12.dp)
    )