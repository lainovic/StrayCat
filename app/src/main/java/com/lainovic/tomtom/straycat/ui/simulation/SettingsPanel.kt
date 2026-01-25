package com.lainovic.tomtom.straycat.ui.simulation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lainovic.tomtom.straycat.shared.update
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsPanel(
    viewModel: SettingsViewModel,
    onDismiss: () -> Unit
) {
    val config by viewModel.configuration.collectAsState()
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 48.dp)
        ) {
            Text(
                text = "Simulation Settings",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            SettingSwitch(
                label = "Realistic Timing",
                checked = config.useRealisticTiming,
                onCheckedChange = { value ->
                    viewModel.updateConfiguration(config.update { useRealisticTiming = value })
                }
            )

            if (!config.useRealisticTiming) {
                SettingSlider(
                    label = "Delay: ${config.delayBetweenEmissions.inWholeMilliseconds}ms",
                    value = config.delayBetweenEmissions.inWholeMilliseconds.toFloat(),
                    valueRange = 100f..5000f,
                    onValueChange = { value ->
                        viewModel.updateConfiguration(config.update {
                            delayBetweenEmissions = value.toLong().milliseconds
                        })
                    }
                )
            }

            SettingSwitch(
                label = "Loop Indefinitely",
                checked = config.loopIndefinitely,
                onCheckedChange = { value ->
                    viewModel.updateConfiguration(config.update { loopIndefinitely = value })
                }
            )

            SettingSlider(
                label = "Speed Multiplier: ${"%.1f".format(config.speedMultiplier)}x",
                value = config.speedMultiplier,
                valueRange = 0.1f..10f,
                onValueChange = { value ->
                    viewModel.updateConfiguration(config.update { speedMultiplier = value })
                }
            )

            SettingSlider(
                label = "Noise Level: ${config.noiseLevelInMeters.roundToInt()}m",
                value = config.noiseLevelInMeters,
                valueRange = 0f..50f,
                onValueChange = { value ->
                    viewModel.updateConfiguration(config.update { noiseLevelInMeters = value })
                }
            )
        }
    }
}

@Composable
private fun SettingSwitch(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
    ) {
        Text(text = label, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun SettingSlider(
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange
        )
    }
}
