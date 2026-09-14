package com.example.ui.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

enum class ZenThemePalette(
    val title: String,
    val primaryColor: Color,
    val secondaryColor: Color
) {
    ZEN_VIOLET("Zen Violet (Original)", ZenVioletPrimary, ZenVioletSecondary),
    FOREST_ZEN("Forest Green", ForestPrimary, ForestSecondary),
    SUNSET_ZEN("Sunset Amber", SunsetPrimary, SunsetSecondary),
    OCEAN_ZEN("Ocean Blue", OceanPrimary, OceanSecondary)
}

object ThemeManager {
    var currentPalette by mutableStateOf(ZenThemePalette.ZEN_VIOLET)
    var isDarkMode by mutableStateOf(true)

    fun toggleDarkMode() {
        isDarkMode = !isDarkMode
    }

    fun setPalette(palette: ZenThemePalette) {
        currentPalette = palette
    }
}
