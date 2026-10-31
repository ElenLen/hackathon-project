package com.example.smarthome.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColors = darkColorScheme(
    primary = UjinBlue,
    secondary = UjinGreen,
    error = UjinRed,
    background = UjinSurface,
    surface = UjinSurface,
    surfaceVariant = UjinSurfaceVariant,
    onSurface = UjinOnSurface,
    onBackground = UjinOnSurface,
)

private val LightColors = lightColorScheme(
    primary = UjinBlueDark,
    secondary = UjinGreen,
    error = UjinRed,
)

@Composable
fun UjinTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = UjinTypography,
        content = content,
    )
}
