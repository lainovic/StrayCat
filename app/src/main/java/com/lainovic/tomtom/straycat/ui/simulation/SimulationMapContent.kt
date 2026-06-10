package com.lainovic.tomtom.straycat.ui.simulation

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.libraries.places.api.Places
import com.lainovic.tomtom.straycat.domain.location.TrackPoint
import com.lainovic.tomtom.straycat.domain.logging.Logger
import com.lainovic.tomtom.straycat.infrastructure.service.ServicePlaybackCommands
import com.lainovic.tomtom.straycat.infrastructure.service.SimulationService
import com.lainovic.tomtom.straycat.shared.toMapLocations
import com.lainovic.tomtom.straycat.ui.components.SearchButton
import com.lainovic.tomtom.straycat.ui.components.SettingsButton
import com.lainovic.tomtom.straycat.ui.components.TomTomMap
import com.lainovic.tomtom.straycat.ui.showToast
import com.lainovic.tomtom.straycat.ui.theme.AppSizes
import com.tomtom.sdk.location.LocationProvider

@Composable
fun SimulationMapContent(
    originProvider: () -> Location?,
    destinationProvider: () -> Location?,
    points: List<TrackPoint>,
    locationProvider: LocationProvider,
    logger: Logger,
    onOriginSelected: (Location, String) -> Unit,
    onDestinationSelected: (Location, String) -> Unit,
    onMapLongPress: (Location) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val controller = remember(context) {
        ServicePlaybackCommands(
            context = context,
            serviceClass = SimulationService::class.java,
            logger = logger,
        )
    }

    val playbackViewModel: PlaybackViewModel = viewModel(
        factory = PlaybackViewModel.Factory(controller, logger)
    )
    val settingsViewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModel.Factory()
    )
    val searchViewModel: SearchViewModel = viewModel(
        factory = SearchViewModel.Factory(placesClient = Places.createClient(context))
    )

    // State reads are pushed into each child — SimulationMapContent only recomposes
    // when its own params or local UI state (isSearchOpen/isSettingsOpen) change.

    var isSearchOpen by remember { mutableStateOf(false) }
    var isSettingsOpen by remember { mutableStateOf(false) }

    val mapLocations = remember(points) { points.toMapLocations() }

    Box(modifier = modifier.fillMaxSize()) {
        TomTomMap(
            originProvider = originProvider,
            destinationProvider = destinationProvider,
            locations = mapLocations,
            locationProvider = locationProvider,
            onMapLongPress = onMapLongPress,
            modifier = Modifier.padding(8.dp).clip(RoundedCornerShape(12.dp))
        )

        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.Bottom),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 46.dp, end = 32.dp)
        ) {
            SettingsButton(onClick = { isSettingsOpen = true }, iconSize = AppSizes.ButtonSize)
            SearchButton(onClick = { isSearchOpen = true }, iconSize = AppSizes.ButtonSize)
            PlaybackControlsSwitcher(
                simulationState = playbackViewModel.simulationState,
                progress = playbackViewModel.progress,
                onPlay = {
                    if (points.isNotEmpty()) playbackViewModel.startPlaying(points)
                    else context.showToast("Please set both origin and destination to start.")
                },
                onPauseOrResume = playbackViewModel::pauseResume,
                onStop = playbackViewModel::stopPlaying,
            )
        }

        if (isSettingsOpen) {
            SettingsPanel(
                configuration = settingsViewModel.configuration,
                onUpdateConfiguration = settingsViewModel::updateConfiguration,
                onDismiss = { isSettingsOpen = false },
            )
        }

        if (isSearchOpen) {
            SearchPanel(
                originQuery = searchViewModel.originQuery,
                originPredictions = searchViewModel.originPredictions,
                destinationQuery = searchViewModel.destinationQuery,
                destinationPredictions = searchViewModel.destinationPredictions,
                onOriginQueryChanged = searchViewModel::onOriginQueryChanged,
                onDestinationQueryChanged = searchViewModel::onDestinationQueryChanged,
                onOriginPredictionSelected = { prediction ->
                    searchViewModel.selectOrigin(prediction, onOriginSelected)
                },
                onDestinationPredictionSelected = { prediction ->
                    searchViewModel.selectDestination(prediction, onDestinationSelected)
                },
                onDismiss = { isSearchOpen = false },
            )
        }
    }
}
