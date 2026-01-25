package com.lainovic.tomtom.straycat.ui.simulation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.lainovic.tomtom.straycat.domain.location.SimulationPoint
import com.lainovic.tomtom.straycat.ui.components.PlayButton
import com.lainovic.tomtom.straycat.ui.components.PlaybackControls
import com.lainovic.tomtom.straycat.ui.showToast
import com.lainovic.tomtom.straycat.ui.theme.AppSizes

@Composable
fun PlaybackControls(
    viewModel: PlaybackViewModel,
    simulationPoints: List<SimulationPoint>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isRunning by remember { mutableStateOf(false) }

    val onPlayClick = {
        if (simulationPoints.isNotEmpty()) {
            viewModel.startPlaying(simulationPoints)
            isRunning = true
        } else {
            context.showToast(
                "Please set both origin and destination to start."
            )
        }
    }

    if (isRunning) {
        PlaybackControls(
            viewModel = viewModel,
            onStop = { isRunning = false },
            modifier = modifier
        )
    } else {
        PlayButton(
            onClick = onPlayClick,
            iconSize = AppSizes.ButtonSize,
            modifier = modifier
        )
    }
}