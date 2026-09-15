package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun FastZenTheme(
    palette: ZenThemePalette = ThemeManager.currentPalette,
    darkTheme: Boolean = ThemeManager.isDarkMode,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = palette.darkPrimary,
            onPrimary = Color.White,
            primaryContainer = palette.darkContainer,
            onPrimaryContainer = palette.darkOnContainer,
            secondary = palette.darkSecondary,
            onSecondary = Color.White,
            secondaryContainer = palette.darkContainer.copy(alpha = 0.7f),
            onSecondaryContainer = palette.darkOnContainer,
            tertiary = palette.darkPrimary,
            onTertiary = Color.White,
            tertiaryContainer = palette.darkContainer,
            onTertiaryContainer = palette.darkOnContainer,
            background = DarkMidnightBg,
            onBackground = DarkTextPrimary,
            surface = DarkMidnightSurface,
            onSurface = DarkTextPrimary,
            surfaceVariant = DarkMidnightCard,
            onSurfaceVariant = DarkTextSecondary,
            surfaceTint = palette.darkPrimary,
            outline = DarkMidnightBorder,
            outlineVariant = DarkMidnightBorderSubtle,
            surfaceContainerLowest = Color(0xFF06090F),
            surfaceContainerLow = Color(0xFF0D131F),
            surfaceContainer = Color(0xFF111827),
            surfaceContainerHigh = Color(0xFF1F2937),
            surfaceContainerHighest = Color(0xFF374151)
        )
    } else {
        lightColorScheme(
            primary = palette.lightPrimary,
            onPrimary = Color.White,
            primaryContainer = palette.lightContainer,
            onPrimaryContainer = palette.lightOnContainer,
            secondary = palette.lightSecondary,
            onSecondary = Color.White,
            secondaryContainer = palette.lightContainer.copy(alpha = 0.6f),
            onSecondaryContainer = palette.lightOnContainer,
            tertiary = palette.lightPrimary,
            onTertiary = Color.White,
            tertiaryContainer = palette.lightContainer,
            onTertiaryContainer = palette.lightOnContainer,
            background = LightBg,
            onBackground = LightTextPrimary,
            surface = LightSurface,
            onSurface = LightTextPrimary,
            surfaceVariant = LightBorderSubtle,
            onSurfaceVariant = LightTextSecondary,
            surfaceTint = palette.lightPrimary,
            outline = LightBorder,
            outlineVariant = LightBorderSubtle,
            surfaceContainerLowest = Color(0xFFFFFFFF),
            surfaceContainerLow = Color(0xFFF8FAFC),
            surfaceContainer = Color(0xFFF1F5F9),
            surfaceContainerHigh = Color(0xFFE2E8F0),
            surfaceContainerHighest = Color(0xFFCBD5E1)
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
