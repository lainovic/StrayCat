package com.lainovic.tomtom.straycat.ui.simulation

import android.content.Context
import android.location.Location
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lainovic.tomtom.straycat.domain.location.SimulationPoint
import com.lainovic.tomtom.straycat.domain.simulation.SimulationController
import com.lainovic.tomtom.straycat.domain.simulation.SimulationDataRepository
import com.lainovic.tomtom.straycat.domain.simulation.SimulationEventBus
import com.lainovic.tomtom.straycat.domain.simulation.SimulationStateRepository
import com.lainovic.tomtom.straycat.infrastructure.service.SimulationService
import com.lainovic.tomtom.straycat.infrastructure.service.ServiceSimulationController
import com.lainovic.tomtom.straycat.shared.toMapLocations
import com.lainovic.tomtom.straycat.ui.components.SearchButton
import com.lainovic.tomtom.straycat.ui.components.SettingsButton
import com.lainovic.tomtom.straycat.ui.components.TomTomMap
import com.lainovic.tomtom.straycat.ui.theme.AppSizes
import com.tomtom.sdk.location.LocationProvider
import com.lainovic.tomtom.straycat.infrastructure.simulation.SimulationConfigurationManagerSingleton
import com.google.android.libraries.places.api.Places

@Composable
fun Dashboard(
    context: Context,
    origin: Location?,
    destination: Location?,
    points: List<SimulationPoint>,
    locationProvider: LocationProvider,
    eventBus: SimulationEventBus,
    dataRepository: SimulationDataRepository,
    stateRepository: SimulationStateRepository,
    onOriginSelected: (Location, String) -> Unit,
    onDestinationSelected: (Location, String) -> Unit,
    onMapLongPress: (Location) -> Unit,
    modifier: Modifier = Modifier
) {
    val controller: SimulationController = remember(context) {
        ServiceSimulationController(
            context = context,
            serviceClass = SimulationService::class.java,
        )
    }

    val playbackViewModel: PlaybackViewModel = viewModel(
        factory = PlaybackViewModel.Factory(controller, eventBus, dataRepository, stateRepository)
    )

    val settingsViewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModel.Factory(
            configurationManager = SimulationConfigurationManagerSingleton,
            controller = controller
        )
    )

    val searchViewModel: SearchViewModel = viewModel(
        factory = SearchViewModel.Factory(
            placesClient = Places.createClient(context)
        )
    )

    var isSearchOpen by remember { mutableStateOf(false) }
    var isSettingsOpen by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        TomTomMap(
            origin = origin,
            destination = destination,
            locations = points.toMapLocations(),
            locationProvider = locationProvider,
            onMapLongPress = onMapLongPress,
            modifier = Modifier
                .padding(8.dp)
                .clip(RoundedCornerShape(12.dp))
        )

        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement =
                Arrangement.spacedBy(8.dp, Alignment.Bottom),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 46.dp, end = 32.dp)
        ) {
            SettingsButton(
                onClick = { isSettingsOpen = true },
                iconSize = AppSizes.ButtonSize,
            )

            SearchButton(
                onClick = { isSearchOpen = true },
                iconSize = AppSizes.ButtonSize,
            )

            PlaybackControls(
                viewModel = playbackViewModel,
                simulationPoints = points,
            )
        }

        if (isSettingsOpen) {
            SettingsPanel(
                viewModel = settingsViewModel,
                onDismiss = { isSettingsOpen = false }
            )
        }

        if (isSearchOpen) {
            SearchPanel(
                viewModel = searchViewModel,
                onOriginSelected = onOriginSelected,
                onDestinationSelected = onDestinationSelected,
                onDismiss = { isSearchOpen = false }
            )
        }
    }
}
