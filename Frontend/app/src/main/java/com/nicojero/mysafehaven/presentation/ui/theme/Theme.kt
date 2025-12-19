package com.nicojero.mysafehaven.presentation.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val RadarColorScheme = lightColorScheme(
    primary = Color(0xFF6B5D52),
    secondary = Color(0xFF8B7968),
    background = Color(0xFFE8DCD6),
    surface = Color(0xFFCEB7A1),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFF3E3731),
    onSurface = Color(0xFF3E3731)
)

private val RadarDarkColorScheme = darkColorScheme(
    primary = Color(0xFFD2C4B8),
    secondary = Color(0xFFB6A99C),
    background = Color(0xFF1E1B18),
    surface = Color(0xFF2A2622),
    onPrimary = Color(0xFF1E1B18),
    onSecondary = Color(0xFF1E1B18),
    onBackground = Color(0xFFE8DCD6),
    onSurface = Color(0xFFE8DCD6)
)

@Composable
fun MySafeHavenTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) {
        RadarDarkColorScheme
    } else {
        RadarColorScheme
    }

    MaterialTheme(
        colorScheme = colors,
        content = content
    )
}
