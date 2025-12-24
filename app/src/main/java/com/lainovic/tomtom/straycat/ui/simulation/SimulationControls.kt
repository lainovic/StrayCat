package com.lainovic.tomtom.straycat.ui.simulation

import android.location.Location
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.lainovic.tomtom.straycat.ui.components.PlayerButtonState
import com.lainovic.tomtom.straycat.ui.player.LocationPlayerViewModel
import com.lainovic.tomtom.straycat.ui.showToast
import com.lainovic.tomtom.straycat.ui.toIconAndText

@Composable
internal fun SimulationControls(
    viewModel: LocationPlayerViewModel,
    locations: List<Location>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var isRunning by remember { mutableStateOf(false) }

    if (isRunning) {
        PlayerButtons(
            viewModel = viewModel,
            onPauseResumePlayer = {
                viewModel.pauseResume()
            },
            onStopPlayer = {
                viewModel.stop()
                isRunning = false
            },
            modifier = modifier
        )
    } else {
        PlayButton(
            onClick = {
                if (locations.isEmpty()) {
                    context.showToast("No locations to play")
                    return@PlayButton
                }
                viewModel.start(locations)
                isRunning = true
            },
            modifier = modifier
        )
    }
}

@Composable
private fun PlayerButtons(
    viewModel: LocationPlayerViewModel,
    onPauseResumePlayer: () -> Unit = {},
    onStopPlayer: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val startStopButtonState
            by viewModel.startStopButtonState.collectAsState()
    val pauseResumeButtonState
            by viewModel.pauseResumeButtonState.collectAsState()
    val progress by viewModel.progress.collectAsState()

    val buttonColor by animateColorAsState(
        targetValue = when (startStopButtonState) {
            PlayerButtonState.Start -> Color.Green
            PlayerButtonState.Stop -> Color.Red
            PlayerButtonState.Pause -> Color.Yellow
            else -> Color.Gray
        },
        animationSpec = tween(300)
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.End,
        modifier = modifier
    ) {
        LinearProgressIndicator(
            progress = { progress },
            color = Color(0xFF0066FF),
            modifier = Modifier
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
        )

        Column(
            horizontalAlignment = Alignment.End,
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(
                    onClick = onPauseResumePlayer,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    val (icon, text) = pauseResumeButtonState.toIconAndText()
                    Icon(
                        imageVector = icon,
                        contentDescription = text,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(32.dp)
                    )
                }
                IconButton(
                    onClick = onStopPlayer,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    val (icon, text) = startStopButtonState.toIconAndText()
                    Icon(
                        imageVector = icon,
                        contentDescription = text,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun PlayButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
            .size(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Icon(
            imageVector = Icons.Default.PlayArrow,
            contentDescription = "Play",
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.size(32.dp)
        )
    }
}