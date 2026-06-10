package com.lainovic.tomtom.straycat.ui

import android.content.Context
import android.location.Location
import android.widget.Toast
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.ui.graphics.vector.ImageVector
import com.lainovic.tomtom.straycat.domain.simulation.SimulationState
import com.lainovic.tomtom.straycat.ui.components.PlayPauseButtonState

fun Context.showToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

fun SimulationState.toPlayPauseButtonState(): PlayPauseButtonState =
    when (this) {
        is SimulationState.Idle -> PlayPauseButtonState.Play
        is SimulationState.Running -> PlayPauseButtonState.Pause
        is SimulationState.Paused -> PlayPauseButtonState.Play
        is SimulationState.Stopped -> PlayPauseButtonState.Play
        is SimulationState.Error -> PlayPauseButtonState.Play
    }

fun PlayPauseButtonState.toIconAndText(): Pair<ImageVector, String> =
    when (this) {
        is PlayPauseButtonState.Play -> Icons.Filled.PlayArrow to "Play"
        is PlayPauseButtonState.Pause -> Icons.Filled.Pause to "Pause"
    }

fun Location.prettyPrint() =
    "%.2f, %.2f".format(latitude, longitude)