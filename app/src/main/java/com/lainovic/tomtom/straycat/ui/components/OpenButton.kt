package com.lainovic.tomtom.straycat.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Loop
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import com.lainovic.tomtom.straycat.ui.theme.AppColors
import com.lainovic.tomtom.straycat.ui.theme.AppSizes

@Composable
fun OpenButton(
    onClick: () -> Unit,
    iconSize: Dp = AppSizes.ButtonSize,
    isOpened: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = CircleShape,
        tonalElevation = AppSizes.TonalElevation,
        shadowElevation = AppSizes.ShadowElevation,
        modifier = modifier.size(iconSize)
    ) {
        FilledIconButton(
            onClick = onClick,
            shape = CircleShape,
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = AppColors.Primary,
                contentColor = AppColors.OnPrimary
            ),
            modifier = Modifier.size(iconSize)
        ) {
            Icon(
                imageVector = if (!isOpened)
                    Icons.Filled.Search
                else
                    Icons.Default.Close,
                contentDescription = if (!isOpened) "Open menu" else "Close menu",
                modifier = Modifier.size(iconSize * 0.5f)
            )
        }
    }
}