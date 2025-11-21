package com.nicojero.mysafehaven.presentation.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val RadarColorScheme = lightColorScheme(
    primary = Color(0xFF6B5D52),
    secondary = Color(0xFF8B7968),
    background = Color(0xFFE8DDD3),
    surface = Color(0xFF6B5D52),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFF3E3731),
    onSurface = Color.White
)

@Composable
fun MySafeHavenTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = RadarColorScheme,
        content = content
    )
}
