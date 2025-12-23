package com.lainovic.tomtom.straycat.ui

import android.content.Context
import android.widget.Toast
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.vector.ImageVector
import com.lainovic.tomtom.straycat.domain.service.CustomLocationProvider
import com.lainovic.tomtom.straycat.ui.components.PlayerButtonState
import com.tomtom.sdk.location.DefaultLocationProviderFactory
import com.tomtom.sdk.location.LocationProvider
import com.tomtom.sdk.location.LocationProviderConfig

fun Context.showToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

internal fun generateCatSound(): String {
    val sounds = listOf("Meow", "Purr", "Mew", "Hiss", "Yowl")
    return sounds.random()
}

internal fun PlayerButtonState.toIconAndText(): Pair<ImageVector, String> =
    when (this) {
        is PlayerButtonState.Start -> Icons.Filled.PlayArrow to "Start"
        is PlayerButtonState.Stop -> Icons.Filled.Stop to "Stop"
        is PlayerButtonState.Retry -> Icons.Filled.Refresh to "Retry"
        is PlayerButtonState.Pause -> Icons.Filled.Pause to "Pause"
        is PlayerButtonState.Resume -> Icons.Filled.PlayArrow to "Resume"
    }

@Composable
internal fun rememberCustomLocationProvider(
    context: Context,
    locationProviderConfig: LocationProviderConfig
): LocationProvider = remember {
    val defaultLocationProvider = DefaultLocationProviderFactory.create(
        context = context,
        config = locationProviderConfig
    )

    CustomLocationProvider(
        defaultLocationProvider = defaultLocationProvider,
    )
}