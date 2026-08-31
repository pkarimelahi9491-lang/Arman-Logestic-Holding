package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = NavyPrimary,
    onPrimary = Color.White,
    primaryContainer = NavyLight,
    onPrimaryContainer = NavyDark,
    secondary = GoldPrimary,
    onSecondary = Color.White,
    secondaryContainer = GoldLight,
    onSecondaryContainer = Color(0xFF78350F),
    tertiary = EmeraldPrimary,
    onTertiary = Color.White,
    tertiaryContainer = EmeraldLight,
    onTertiaryContainer = EmeraldDark,
    background = CanvasBgTop,
    onBackground = Slate900,
    surface = GlassSurfaceLight,
    onSurface = Slate900,
    surfaceVariant = GlassSurfacePure,
    onSurfaceVariant = Slate700,
    surfaceContainer = Color(0xEBFFFFFF),
    surfaceContainerHigh = Color(0xF5FFFFFF),
    surfaceContainerHighest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xDCF8FAFC),
    surfaceContainerLowest = Color(0xC8F1F5F9),
    outline = GlassBorderSubtle,
    outlineVariant = GlassBorderLight,
    error = CrimsonError,
    onError = Color.White,
    errorContainer = CrimsonLight,
    onErrorContainer = Color(0xFF7F1D1D)
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF93C5FD),
    onPrimary = NavyDark,
    primaryContainer = NavySurface,
    onPrimaryContainer = Color(0xFFDBEAFE),
    secondary = GoldAccent,
    onSecondary = Color(0xFF78350F),
    secondaryContainer = Color(0xFF451A03),
    onSecondaryContainer = GoldLight,
    tertiary = EmeraldAccent,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFF064E3B),
    onTertiaryContainer = EmeraldLight,
    background = CanvasDarkTop,
    onBackground = Color(0xFFF1F5F9),
    surface = GlassSurfaceDark,
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = GlassSurfaceDarker,
    onSurfaceVariant = Color(0xFFCBD5E1),
    surfaceContainer = Color(0xD90F1E36),
    surfaceContainerHigh = Color(0xE6132440),
    surfaceContainerHighest = Color(0xF2162B4C),
    surfaceContainerLow = Color(0xB8091426),
    surfaceContainerLowest = Color(0x99040810),
    outline = GlassBorderDark,
    outlineVariant = Color(0x2693C5FD),
    error = Color(0xFFF87171),
    onError = Color(0xFF7F1D1D),
    errorContainer = Color(0xFF991B1B),
    onErrorContainer = Color(0xFFFEE2E2)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
