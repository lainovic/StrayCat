package com.lainovic.tomtom.straycat.ui.simulation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.lainovic.tomtom.straycat.domain.simulation.SimulationState
import com.lainovic.tomtom.straycat.ui.components.PlayButton
import com.lainovic.tomtom.straycat.ui.components.PlaybackControls
import com.lainovic.tomtom.straycat.ui.theme.AppSizes
import kotlinx.coroutines.flow.StateFlow

@Composable
fun PlaybackControlsSwitcher(
    simulationState: StateFlow<SimulationState>,
    progress: StateFlow<Float>,
    onPlay: () -> Unit,
    onPauseOrResume: () -> Unit,
    onStop: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by simulationState.collectAsState()
    val progressValue by progress.collectAsState()

    val isRunning = state is SimulationState.Running || state is SimulationState.Paused

    if (isRunning) {
        PlaybackControls(
            progress = progressValue,
            simulationState = state,
            onPauseOrResume = onPauseOrResume,
            onStop = onStop,
            modifier = modifier,
        )
    } else {
        PlayButton(
            onClick = onPlay,
            iconSize = AppSizes.ButtonSize,
            modifier = modifier,
        )
    }
}