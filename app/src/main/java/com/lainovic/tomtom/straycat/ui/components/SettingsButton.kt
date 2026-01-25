package com.lainovic.tomtom.straycat.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import com.lainovic.tomtom.straycat.ui.theme.AppColors
import com.lainovic.tomtom.straycat.ui.theme.AppSizes

@Composable
fun SettingsButton(
    onClick: () -> Unit,
    iconSize: Dp = AppSizes.ButtonSize,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = CircleShape,
        tonalElevation = AppSizes.TonalElevation,
        shadowElevation = AppSizes.ShadowElevation,
        color = Color.Transparent,
        modifier = modifier.size(iconSize)
    ) {
        FilledIconButton(
            onClick = onClick,
            shape = CircleShape,
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = AppColors.SurfaceTranslucent,
                contentColor = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier.size(iconSize)
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings",
                modifier = Modifier.size(iconSize * 0.5f)
            )
        }
    }
}
