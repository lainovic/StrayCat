package com.lainovic.tomtom.straycat.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.StrokeCap
import com.lainovic.tomtom.straycat.domain.simulation.SimulationState
import com.lainovic.tomtom.straycat.ui.simulation.SimulationPlayerViewModel
import com.lainovic.tomtom.straycat.ui.theme.AppColors
import com.lainovic.tomtom.straycat.ui.theme.AppSizes
import com.lainovic.tomtom.straycat.ui.toIconAndText
import com.lainovic.tomtom.straycat.ui.toPlayResumeButtonState

@Composable
fun PlaybackControls(
    viewModel: SimulationPlayerViewModel,
    onPauseOrResume: () -> Unit = {},
    onStop: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val progress by viewModel.progress.collectAsState()
    val simulationState by viewModel.simulationState.collectAsState()

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 300),
        label = "progress"
    )

    Surface(
        tonalElevation = AppSizes.TonalElevation,
        shadowElevation = AppSizes.ShadowElevation,
        color = AppColors.Surface,
        shape = RoundedCornerShape(AppSizes.ButtonCornerRadius),
        modifier = modifier
            .width(AppSizes.PlaybackControlsWidth)
            .height(AppSizes.PlaybackControlsHeight)
    ) {
        Box(contentAlignment = Alignment.Center) {
            LinearProgressIndicator(
                progress = { animatedProgress },
                color = AppColors.Progress,
                trackColor = AppColors.ProgressTrack,
                strokeCap = StrokeCap.Butt,
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(0.8f)
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(AppSizes.ButtonSpacing, Alignment.End),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxSize()
            ) {
                val isPauseResumeEnabled = simulationState is SimulationState.Running ||
                        simulationState is SimulationState.Paused

                val pauseResumeButtonColor by animateColorAsState(
                    targetValue = if (isPauseResumeEnabled) {
                        AppColors.Primary
                    } else {
                        AppColors.PrimaryDisabled
                    },
                    animationSpec = tween(300),
                    label = "pauseResumeColor"
                )

                val pauseResumeContentColor by animateColorAsState(
                    targetValue = if (isPauseResumeEnabled) {
                        AppColors.OnPrimary
                    } else {
                        AppColors.OnDisabled
                    },
                    animationSpec = tween(300),
                    label = "pauseResumeContentColor"
                )

                FilledIconButton(
                    onClick = {
                        viewModel.pauseOrResume()
                        onPauseOrResume()
                    },
                    enabled = isPauseResumeEnabled,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = pauseResumeButtonColor,
                        contentColor = pauseResumeContentColor,
                        disabledContainerColor = AppColors.PrimaryDisabled,
                        disabledContentColor = AppColors.OnDisabled
                    ),
                    modifier = Modifier.size(AppSizes.ButtonSize)
                ) {
                    val (icon, text) = simulationState.toPlayResumeButtonState().toIconAndText()
                    Icon(
                        imageVector = icon,
                        contentDescription = text,
                        modifier = Modifier.size(AppSizes.IconSize)
                    )
                }

                val isStopEnabled = simulationState is SimulationState.Running ||
                        simulationState is SimulationState.Paused

                FilledIconButton(
                    onClick = {
                        viewModel.stopPlaying()
                        onStop()
                    },
                    enabled = isStopEnabled,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = AppColors.Error,
                        contentColor = AppColors.OnError,
                        disabledContainerColor = AppColors.PrimaryDisabled,
                        disabledContentColor = AppColors.OnDisabled
                    ),
                    modifier = Modifier.size(AppSizes.ButtonSize)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Stop,
                        contentDescription = "Stop",
                        modifier = Modifier.size(AppSizes.IconSize)
                    )
                }
            }
        }
    }
}
