package com.lainovic.tomtom.straycat.ui.simulation

import android.content.Context
import android.location.Location
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lainovic.tomtom.straycat.domain.service.LocationPlayerService
import com.lainovic.tomtom.straycat.domain.service.LocationPlayerServiceFacade
import com.lainovic.tomtom.straycat.ui.player.LocationPlayerViewModel
import com.tomtom.sdk.location.LocationProvider

@Composable
internal fun SimulationContent(
    context: Context,
    origin: Location?,
    destination: Location?,
    locations: List<Location>,
    locationProvider: LocationProvider,
    onOriginSelected: (Location, String) -> Unit,
    onDestinationSelected: (Location, String) -> Unit,
    onMapLongPress: (Location) -> Unit,
    modifier: Modifier = Modifier
) {
    val service = LocationPlayerServiceFacade(
        context = context,
        serviceClass = LocationPlayerService::class.java,
    )

    val viewModel: LocationPlayerViewModel = viewModel(
        factory = LocationPlayerViewModel.Factory(service)
    )


    Box(modifier = modifier.fillMaxSize()) {
        SimulationInput(
            origin = origin,
            destination = destination,
            locations = locations,
            locationProvider = locationProvider,
            onOriginSelected = onOriginSelected,
            onDestinationSelected = onDestinationSelected,
            onMapLongPress = onMapLongPress
        )

        SimulationControls(
            viewModel = viewModel,
            locations = locations,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        )
    }
}
