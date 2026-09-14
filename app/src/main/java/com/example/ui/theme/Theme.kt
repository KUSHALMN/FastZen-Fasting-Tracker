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
            primary = palette.primaryColor,
            secondary = palette.secondaryColor,
            tertiary = ZenVioletTertiary,
            background = DarkMidnightBg,
            surface = DarkMidnightSurface,
            surfaceVariant = DarkMidnightCard,
            outline = DarkMidnightBorder,
            onPrimary = Color.White,
            onSecondary = Color.White,
            onTertiary = Color.White,
            onBackground = DarkTextPrimary,
            onSurface = DarkTextPrimary,
            onSurfaceVariant = DarkTextSecondary
        )
    } else {
        lightColorScheme(
            primary = palette.primaryColor,
            secondary = palette.secondaryColor,
            tertiary = ZenVioletTertiary,
            background = LightBg,
            surface = LightSurface,
            surfaceVariant = LightCard,
            outline = LightBorder,
            onPrimary = Color.White,
            onSecondary = Color.White,
            onTertiary = Color.White,
            onBackground = LightTextPrimary,
            onSurface = LightTextPrimary,
            onSurfaceVariant = LightTextSecondary
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
