package com.lainovic.tomtom.straycat.ui.simulation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lainovic.tomtom.straycat.domain.logging.Logger
import com.lainovic.tomtom.straycat.domain.simulation.SimulationEvent
import com.lainovic.tomtom.straycat.infrastructure.analytics.InMemorySimulationEventBus
import com.lainovic.tomtom.straycat.ui.showToast
import com.lainovic.tomtom.straycat.ui.theme.AppColors
import com.lainovic.tomtom.straycat.ui.theme.AppSizes
import com.tomtom.sdk.location.LocationProvider
import com.tomtom.sdk.routing.RoutePlanner
import kotlinx.coroutines.flow.SharedFlow

@Composable
fun SimulationScreen(
    routePlanner: RoutePlanner,
    locationProvider: LocationProvider,
    logger: Logger,
    modifier: Modifier = Modifier
) {
    val viewModel: SimulationScreenViewModel = viewModel(
        factory = SimulationScreenViewModel.Factory(
            routePlanner,
            InMemorySimulationEventBus,
        )
    )

    val origin by viewModel.origin
    val destination by viewModel.destination
    val points by viewModel.points
    val isLoading by viewModel.isLoading.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    Box(modifier = modifier) {
        SimulationMapContent(
            originProvider = { origin },
            destinationProvider = { destination },
            points = points,
            locationProvider = locationProvider,
            logger = logger,
            onOriginSelected = { location, _ ->
                viewModel.setOrigin(location)
                InMemorySimulationEventBus.pushEvent(SimulationEvent.OriginSet(location))
            },
            onDestinationSelected = { location, _ ->
                viewModel.setDestination(location)
                InMemorySimulationEventBus.pushEvent(SimulationEvent.DestinationSet(location))
            },
            onMapLongPress = { location ->
                when {
                    origin == null -> {
                        viewModel.setOrigin(location)
                        InMemorySimulationEventBus.pushEvent(SimulationEvent.OriginSet(location))
                    }

                    destination == null -> {
                        viewModel.setDestination(location)
                        InMemorySimulationEventBus.pushEvent(SimulationEvent.DestinationSet(location))
                    }

                    else -> {
                        viewModel.clearRoute()
                        InMemorySimulationEventBus.pushEvent(SimulationEvent.RouteCleared)
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

        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }

    ErrorEffect(viewModel.errorMessage)
    SnackbarEffect(snackbarHostState, viewModel.snackbarMessages)
}

@Composable
private fun ErrorEffect(errorMessage: State<String?>) {
    val context = LocalContext.current
    val message by errorMessage
    LaunchedEffect(message) {
        message?.let { context.showToast("Error: $it") }
    }
}

@Composable
private fun SnackbarEffect(
    snackbarHostState: SnackbarHostState,
    snackbarMessages: SharedFlow<String>,
) {
    LaunchedEffect(snackbarMessages) {
        snackbarMessages.collect { message ->
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
        containerColor = AppColors.Surface,
        contentColor = AppColors.PrimaryDarker,
        actionColor = AppColors.PrimaryDarker,
        dismissActionContentColor = AppColors.PrimaryDarker,
        shape = RoundedCornerShape(AppSizes.SnackbarCornerRadius),
        modifier = Modifier.padding(horizontal = AppSizes.ButtonPadding, vertical = 12.dp)
    )