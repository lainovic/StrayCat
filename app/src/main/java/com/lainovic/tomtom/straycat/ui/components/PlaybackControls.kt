package com.lainovic.tomtom.straycat.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import com.lainovic.tomtom.straycat.domain.simulation.SimulationState
import com.lainovic.tomtom.straycat.ui.theme.AppColors
import com.lainovic.tomtom.straycat.ui.theme.AppSizes
import com.lainovic.tomtom.straycat.ui.toIconAndText
import com.lainovic.tomtom.straycat.ui.toPlayPauseButtonState

@Composable
fun PlaybackControls(
    progress: Float,
    simulationState: SimulationState,
    onPauseOrResume: () -> Unit,
    onStop: () -> Unit,
    onScrubStart: () -> Unit,
    onScrub: (Float) -> Unit,
    onScrubEnd: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 300),
        label = "progress"
    )

    Surface(
        tonalElevation = AppSizes.TonalElevation,
        color = AppColors.SurfaceTranslucent,
        shape = RoundedCornerShape(AppSizes.ButtonCornerRadius),
        modifier = modifier
            .width(AppSizes.PlaybackControlsWidth)
            .height(AppSizes.PlaybackControlsHeight)
    ) {
        val seekEnabled = simulationState is SimulationState.Running
        var widthPx by remember { mutableIntStateOf(0) }
        var lastFraction by remember { mutableFloatStateOf(progress) }

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.onSizeChanged { widthPx = it.width }
        ) {
            LinearProgressIndicator(
                progress = { animatedProgress },
                color = AppColors.Primary,
                trackColor = AppColors.ProgressTrack.copy(alpha = 0f),
                strokeCap = StrokeCap.Butt,
                modifier = Modifier.fillMaxSize()
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(AppSizes.ButtonSpacing, Alignment.End),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxSize()
            ) {
                val isPauseResumeEnabled = simulationState is SimulationState.Running ||
                        simulationState is SimulationState.Paused

                val pauseResumeButtonColor by animateColorAsState(
                    targetValue = if (isPauseResumeEnabled) Color.Transparent else AppColors.PrimaryDisabled,
                    animationSpec = tween(300),
                    label = "pauseResumeColor"
                )

                val pauseResumeContentColor by animateColorAsState(
                    targetValue = if (isPauseResumeEnabled) MaterialTheme.colorScheme.primary else AppColors.OnDisabled,
                    animationSpec = tween(300),
                    label = "pauseResumeContentColor"
                )

                FilledIconButton(
                    onClick = onPauseOrResume,
                    enabled = isPauseResumeEnabled,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = pauseResumeButtonColor,
                        contentColor = pauseResumeContentColor,
                        disabledContainerColor = AppColors.PrimaryDisabled,
                        disabledContentColor = AppColors.OnDisabled
                    ),
                    modifier = Modifier.size(AppSizes.ButtonSize)
                ) {
                    val (icon, text) = simulationState.toPlayPauseButtonState().toIconAndText()
                    Icon(imageVector = icon, contentDescription = text, modifier = Modifier.size(AppSizes.IconSize))
                }

                val isStopEnabled = simulationState is SimulationState.Running ||
                        simulationState is SimulationState.Paused

                FilledIconButton(
                    onClick = onStop,
                    enabled = isStopEnabled,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = Color.Transparent,
                        contentColor = AppColors.Error,
                        disabledContainerColor = AppColors.PrimaryDisabled,
                        disabledContentColor = AppColors.OnDisabled
                    ),
                    modifier = Modifier.size(AppSizes.ButtonSize)
                ) {
                    Icon(imageVector = Icons.Filled.Stop, contentDescription = "Stop", modifier = Modifier.size(AppSizes.IconSize))
                }
            }

            if (seekEnabled && widthPx > 0) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectTapGestures { offset ->
                                val f = (offset.x / widthPx).coerceIn(0f, 1f)
                                onScrubStart()
                                onScrubEnd(f)
                            }
                        }
                        .pointerInput(Unit) {
                            detectHorizontalDragGestures(
                                onDragStart = { onScrubStart() },
                                onDragEnd = { onScrubEnd(lastFraction) },
                                onHorizontalDrag = { change, _ ->
                                    lastFraction = (change.position.x / widthPx).coerceIn(0f, 1f)
                                    onScrub(lastFraction)
                                }
                            )
                        }
                )
            }
        }
    }
}
