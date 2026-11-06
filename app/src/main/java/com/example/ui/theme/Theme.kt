package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val GameBlastColorScheme = darkColorScheme(
    primary = NeonRed,
    onPrimary = Color.White,
    primaryContainer = NeonRedDim,
    onPrimaryContainer = Color.White,
    secondary = CyberCyan,
    onSecondary = CyberDark,
    secondaryContainer = CyberCyanDim,
    onSecondaryContainer = Color.White,
    tertiary = NeuralPurple,
    onTertiary = Color.White,
    background = CyberDark,
    onBackground = TextPrimary,
    surface = CyberSurface,
    onSurface = TextPrimary,
    surfaceVariant = CyberSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = CyberSurfaceBorder,
    outlineVariant = CyberSurfaceBorder.copy(alpha = 0.5f),
    error = NeonRed,
    onError = Color.White
)

@Composable
fun GameBlastTheme(
    darkTheme: Boolean = true, // Gaming theme defaults to dark aesthetic
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = GameBlastColorScheme,
        typography = Typography,
        content = content
    )
}

// Backward compatibility alias for template references
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    GameBlastTheme(darkTheme = true, content = content)
}
