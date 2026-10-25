package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val AuroraColorScheme = darkColorScheme(
    primary = AuroraMint,
    onPrimary = AuroraBackground,
    primaryContainer = AuroraSurfaceVariant,
    onPrimaryContainer = AuroraMint,
    secondary = AuroraViolet,
    onSecondary = AuroraBackground,
    secondaryContainer = AuroraSurfaceVariant,
    onSecondaryContainer = AuroraViolet,
    tertiary = AuroraMint,
    onTertiary = AuroraBackground,
    background = AuroraBackground,
    onBackground = AuroraTextPrimary,
    surface = AuroraSurface,
    onSurface = AuroraTextPrimary,
    surfaceVariant = AuroraSurfaceVariant,
    onSurfaceVariant = AuroraTextSecondary,
    outline = AuroraBorderSubtle,
    outlineVariant = AuroraBorder,
    error = AuroraError,
    onError = AuroraBackground
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = AuroraColorScheme,
        typography = Typography,
        content = content
    )
}
