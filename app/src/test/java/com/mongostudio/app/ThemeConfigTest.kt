package com.mongostudio.app

import androidx.compose.ui.graphics.Color
import com.mongostudio.app.data.preferences.ThemeSettings
import com.mongostudio.app.ui.theme.*
import org.junit.Assert.*
import org.junit.Test

class ThemeConfigTest {

    @Test
    fun testDynamicPaletteIsFirst() {
        assertEquals(ColorPalettePreset.DYNAMIC, ColorPalettePreset.entries.first())
    }

    @Test
    fun testAllPalettePresetsHaveValidColorSchemes() {
        for (preset in ColorPalettePreset.entries) {
            val theme = AppPalettes.getPreset(preset)
            assertNotNull("Light scheme should not be null for $preset", theme.light)
            assertNotNull("Dark scheme should not be null for $preset", theme.dark)
            assertNotNull("Amoled scheme should not be null for $preset", theme.amoled)
            assertNotEquals("Primary color should not be unspecified for $preset", Color.Unspecified, theme.primaryColor)
        }
    }

    @Test
    fun testAmoledColorSchemesArePureBlack() {
        val pureBlack = Color(0xFF000000)
        for (preset in ColorPalettePreset.entries) {
            val amoledScheme = AppPalettes.getPreset(preset).amoled
            assertEquals("AMOLED background must be pure black for $preset", pureBlack, amoledScheme.background)
            assertEquals("AMOLED surface must be pure black for $preset", pureBlack, amoledScheme.surface)
            assertEquals("AMOLED surfaceContainerLowest must be pure black for $preset", pureBlack, amoledScheme.surfaceContainerLowest)
        }
    }

    @Test
    fun testThemeSettingsToThemeConfigMapping() {
        // System theme
        val sysSettings = ThemeSettings(followSystemTheme = true)
        assertEquals(AppThemeMode.FOLLOW_SYSTEM, sysSettings.themeMode)

        // Light mode
        val lightSettings = ThemeSettings(followSystemTheme = false, isDarkMode = false, isAmoledMode = false)
        assertEquals(AppThemeMode.LIGHT, lightSettings.themeMode)

        // Dark mode
        val darkSettings = ThemeSettings(followSystemTheme = false, isDarkMode = true, isAmoledMode = false)
        assertEquals(AppThemeMode.DARK, darkSettings.themeMode)

        // AMOLED mode
        val amoledSettings = ThemeSettings(followSystemTheme = false, isDarkMode = true, isAmoledMode = true)
        assertEquals(AppThemeMode.AMOLED, amoledSettings.themeMode)
    }

    @Test
    fun testExpressiveShapesAvailable() {
        assertNotNull(PillShape)
        assertNotNull(SquircleSmall)
        assertNotNull(SquircleMedium)
        assertNotNull(SquircleLarge)
        assertNotNull(AsymmetricCardShape)
        assertNotNull(AsymmetricHeroShape)
        assertNotNull(TicketShape)
    }
}
