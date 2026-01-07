package com.lainovic.tomtom.straycat.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = AppColors.Primary,
    secondary = AppColors.Primary,
    tertiary = AppColors.Primary,
    error = AppColors.Error,
    surface = AppColors.Surface,
    onPrimary = AppColors.OnPrimary,
    onError = AppColors.OnError
)

private val LightColorScheme = lightColorScheme(
    primary = AppColors.Primary,
    secondary = AppColors.Primary,
    tertiary = AppColors.Primary,
    error = AppColors.Error,
    surface = AppColors.Surface,
    onPrimary = AppColors.OnPrimary,
    onError = AppColors.OnError,
    surfaceContainerHigh = AppColors.Surface,
    surfaceContainerHighest = AppColors.ProgressTrack,
    primaryContainer = AppColors.Progress,
    surfaceVariant = AppColors.PrimaryDisabled,
    onSurfaceVariant = AppColors.OnDisabled
)

@Composable
fun StrayCatTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}