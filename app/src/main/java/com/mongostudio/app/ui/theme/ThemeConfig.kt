package com.mongostudio.app.ui.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color

enum class AppThemeMode(val label: String) {
    FOLLOW_SYSTEM("Follow System"),
    LIGHT("Light"),
    DARK("Dark"),
    AMOLED("Pure AMOLED")
}

data class ThemeConfig(
    val themeMode: AppThemeMode = AppThemeMode.FOLLOW_SYSTEM,
    val palettePreset: ColorPalettePreset = ColorPalettePreset.EMERALD_PINE
)

val LocalThemeConfig = compositionLocalOf { ThemeConfig() }

/**
 * Smoothly interpolates ColorScheme transitions between Light, Dark, AMOLED, and Preset Palettes
 * preventing screen flicker on theme or color toggling (§5.4).
 */
@Composable
fun animateColorScheme(targetScheme: ColorScheme): ColorScheme {
    val animSpec = tween<Color>(durationMillis = 350)

    val primary by animateColorAsState(targetScheme.primary, animSpec, label = "animPrimary")
    val onPrimary by animateColorAsState(targetScheme.onPrimary, animSpec, label = "animOnPrimary")
    val primaryContainer by animateColorAsState(targetScheme.primaryContainer, animSpec, label = "animPrimaryContainer")
    val onPrimaryContainer by animateColorAsState(targetScheme.onPrimaryContainer, animSpec, label = "animOnPrimaryContainer")
    val secondary by animateColorAsState(targetScheme.secondary, animSpec, label = "animSecondary")
    val onSecondary by animateColorAsState(targetScheme.onSecondary, animSpec, label = "animOnSecondary")
    val secondaryContainer by animateColorAsState(targetScheme.secondaryContainer, animSpec, label = "animSecondaryContainer")
    val onSecondaryContainer by animateColorAsState(targetScheme.onSecondaryContainer, animSpec, label = "animOnSecondaryContainer")
    val tertiary by animateColorAsState(targetScheme.tertiary, animSpec, label = "animTertiary")
    val onTertiary by animateColorAsState(targetScheme.onTertiary, animSpec, label = "animOnTertiary")
    val background by animateColorAsState(targetScheme.background, animSpec, label = "animBackground")
    val onBackground by animateColorAsState(targetScheme.onBackground, animSpec, label = "animOnBackground")
    val surface by animateColorAsState(targetScheme.surface, animSpec, label = "animSurface")
    val onSurface by animateColorAsState(targetScheme.onSurface, animSpec, label = "animOnSurface")
    val surfaceVariant by animateColorAsState(targetScheme.surfaceVariant, animSpec, label = "animSurfaceVariant")
    val onSurfaceVariant by animateColorAsState(targetScheme.onSurfaceVariant, animSpec, label = "animOnSurfaceVariant")
    val surfaceContainerLowest by animateColorAsState(targetScheme.surfaceContainerLowest, animSpec, label = "animSurfaceContainerLowest")
    val surfaceContainerLow by animateColorAsState(targetScheme.surfaceContainerLow, animSpec, label = "animSurfaceContainerLow")
    val surfaceContainer by animateColorAsState(targetScheme.surfaceContainer, animSpec, label = "animSurfaceContainer")
    val surfaceContainerHigh by animateColorAsState(targetScheme.surfaceContainerHigh, animSpec, label = "animSurfaceContainerHigh")
    val surfaceContainerHighest by animateColorAsState(targetScheme.surfaceContainerHighest, animSpec, label = "animSurfaceContainerHighest")
    val outline by animateColorAsState(targetScheme.outline, animSpec, label = "animOutline")
    val outlineVariant by animateColorAsState(targetScheme.outlineVariant, animSpec, label = "animOutlineVariant")

    return targetScheme.copy(
        primary = primary,
        onPrimary = onPrimary,
        primaryContainer = primaryContainer,
        onPrimaryContainer = onPrimaryContainer,
        secondary = secondary,
        onSecondary = onSecondary,
        secondaryContainer = secondaryContainer,
        onSecondaryContainer = onSecondaryContainer,
        tertiary = tertiary,
        onTertiary = onTertiary,
        background = background,
        onBackground = onBackground,
        surface = surface,
        onSurface = onSurface,
        surfaceVariant = surfaceVariant,
        onSurfaceVariant = onSurfaceVariant,
        surfaceContainerLowest = surfaceContainerLowest,
        surfaceContainerLow = surfaceContainerLow,
        surfaceContainer = surfaceContainer,
        surfaceContainerHigh = surfaceContainerHigh,
        surfaceContainerHighest = surfaceContainerHighest,
        outline = outline,
        outlineVariant = outlineVariant
    )
}
