package com.solu8i.compcheck.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AppColorScheme = lightColorScheme(
    primary = NeutralAccent,
    onPrimary = Color.White,
    primaryContainer = WhiteCard,
    onPrimaryContainer = TextPrimary,
    secondary = NeutralAccent,
    onSecondary = Color.White,
    background = WhitePrimary,
    onBackground = TextPrimary,
    surface = WhiteSurface,
    onSurface = TextPrimary,
    surfaceVariant = WhiteCard,
    onSurfaceVariant = TextSecondary,
    outline = WhiteBorder,
    error = ErrorRed,
    onError = Color.White
)

private val AppDarkColorScheme = darkColorScheme(
    primary = Color(0xFF90CAF9),
    onPrimary = Color(0xFF0D1B2A),
    primaryContainer = Color(0xFF1E3A5F),
    onPrimaryContainer = Color(0xFFD6E9FF),
    secondary = Color(0xFFB0BEC5),
    onSecondary = Color(0xFF172027),
    background = Color(0xFF101418),
    onBackground = Color(0xFFE5E7EB),
    surface = Color(0xFF171C21),
    onSurface = Color(0xFFE5E7EB),
    surfaceVariant = Color(0xFF252C33),
    onSurfaceVariant = Color(0xFFB8C1CC),
    outline = Color(0xFF64707D),
    error = Color(0xFFFF8A80),
    onError = Color(0xFF3B0805)
)

@Composable
fun ScanMTTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) AppDarkColorScheme else AppColorScheme,
        typography = Typography,
        content = content
    )
}