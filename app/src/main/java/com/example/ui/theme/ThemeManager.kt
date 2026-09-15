package com.example.ui.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

enum class ZenThemePalette(
    val title: String,
    val darkPrimary: Color,
    val darkSecondary: Color,
    val darkContainer: Color,
    val darkOnContainer: Color,
    val lightPrimary: Color,
    val lightSecondary: Color,
    val lightContainer: Color,
    val lightOnContainer: Color
) {
    ZEN_VIOLET(
        title = "Zen Violet (Original)",
        darkPrimary = ZenVioletPrimaryDark,
        darkSecondary = ZenVioletSecondaryDark,
        darkContainer = ZenVioletContainerDark,
        darkOnContainer = ZenVioletOnContainerDark,
        lightPrimary = ZenVioletPrimaryLight,
        lightSecondary = ZenVioletSecondaryLight,
        lightContainer = ZenVioletContainerLight,
        lightOnContainer = ZenVioletOnContainerLight
    ),
    FOREST_ZEN(
        title = "Forest Green",
        darkPrimary = ForestPrimaryDark,
        darkSecondary = ForestSecondaryDark,
        darkContainer = ForestContainerDark,
        darkOnContainer = ForestOnContainerDark,
        lightPrimary = ForestPrimaryLight,
        lightSecondary = ForestSecondaryLight,
        lightContainer = ForestContainerLight,
        lightOnContainer = ForestOnContainerLight
    ),
    SUNSET_ZEN(
        title = "Sunset Amber",
        darkPrimary = SunsetPrimaryDark,
        darkSecondary = SunsetSecondaryDark,
        darkContainer = SunsetContainerDark,
        darkOnContainer = SunsetOnContainerDark,
        lightPrimary = SunsetPrimaryLight,
        lightSecondary = SunsetSecondaryLight,
        lightContainer = SunsetContainerLight,
        lightOnContainer = SunsetOnContainerLight
    ),
    OCEAN_ZEN(
        title = "Ocean Blue",
        darkPrimary = OceanPrimaryDark,
        darkSecondary = OceanSecondaryDark,
        darkContainer = OceanContainerDark,
        darkOnContainer = OceanOnContainerDark,
        lightPrimary = OceanPrimaryLight,
        lightSecondary = OceanSecondaryLight,
        lightContainer = OceanContainerLight,
        lightOnContainer = OceanOnContainerLight
    );

    val primaryColor: Color get() = if (ThemeManager.isDarkMode) darkPrimary else lightPrimary
    val secondaryColor: Color get() = if (ThemeManager.isDarkMode) darkSecondary else lightSecondary
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
