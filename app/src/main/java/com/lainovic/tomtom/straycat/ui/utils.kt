package com.lainovic.tomtom.straycat.ui

import android.content.Context
import android.location.Location
import android.widget.Toast
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.ui.graphics.vector.ImageVector
import com.lainovic.tomtom.straycat.domain.simulation.SimulationState
import com.lainovic.tomtom.straycat.ui.components.PlayResumeButtonState

fun Context.showToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

fun generateCatSound(): String {
    val sounds = listOf(
        "Meow", "Purr", "Mew", "Hiss", "Yowl", "Chirp", "Trill",
        "Growl", "Screech"
    )
    return sounds.random()
}

fun SimulationState.toPlayResumeButtonState(): PlayResumeButtonState =
    when (this) {
        is SimulationState.Idle -> PlayResumeButtonState.Play
        is SimulationState.Running -> PlayResumeButtonState.Pause
        is SimulationState.Paused -> PlayResumeButtonState.Play
        is SimulationState.Stopped -> PlayResumeButtonState.Play
        is SimulationState.Error -> PlayResumeButtonState.Play
    }

fun PlayResumeButtonState.toIconAndText(): Pair<ImageVector, String> =
    when (this) {
        is PlayResumeButtonState.Play -> Icons.Filled.PlayArrow to "Play"
        is PlayResumeButtonState.Pause -> Icons.Filled.Pause to "Pause"
    }

fun Location.prettyPrint() =
    "%.2f, %.2f".format(latitude, longitude)