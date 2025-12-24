package com.lainovic.tomtom.straycat.ui.player

import android.location.Location
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.lainovic.tomtom.straycat.ui.components.PlayerButtonState
import com.lainovic.tomtom.straycat.ui.showToast
import com.lainovic.tomtom.straycat.ui.toIconAndText

@Composable
internal fun PlayerControls(
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
        )
        Column(
            horizontalAlignment = Alignment.End,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Button(
                    onClick = onPauseResumePlayer,
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 6.dp,
                        pressedElevation = 8.dp
                    ),
//                colors = ButtonDefaults.buttonColors(
//                    contentColor = buttonColor
//                )
                ) {
                    val (icon, text) = pauseResumeButtonState.toIconAndText()
                    Icon(
                        imageVector = icon, contentDescription = text,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Button(
                    onClick = onStopPlayer,
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 6.dp,
                        pressedElevation = 8.dp
                    ),
                ) {
                    val (icon, text) = startStopButtonState.toIconAndText()
                    Icon(
                        imageVector = icon, contentDescription = text,
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
    Button(
        onClick = onClick,
        shape = CircleShape,
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 6.dp,
            pressedElevation = 8.dp
        ),
        modifier = modifier
    ) {
        Icon(
            imageVector = Icons.Default.PlayArrow,
            contentDescription = "Play",
            modifier = Modifier.size(32.dp)
        )
    }
}