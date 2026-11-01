package com.example.smarthome.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColors = darkColorScheme(
    primary = UjinBlue,
    secondary = UjinGreen,
    error = UjinRed,
    background = UjinSurface,
    surface = UjinSurface,
    surfaceVariant = UjinSurfaceVariant,
    onSurface = UjinOnSurface,
    onBackground = UjinOnSurface,
    onSurfaceVariant = Color(0xFF9AA4AE),
    outlineVariant = Color(0xFF2E353D),
)

private val LightColors = lightColorScheme(
    primary = UjinBlueDark,
    secondary = UjinGreen,
    error = UjinRed,
    background = Color.White,
    surface = Color.White,
    surfaceVariant = UjinLightGray,
    onSurface = UjinDarkGray,
    onBackground = UjinDarkGray,
    onSurfaceVariant = Color.Gray,
    outlineVariant = Color(0xFFE8EAED),
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
