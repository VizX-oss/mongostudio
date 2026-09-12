package com.mongostudio.app.data.preferences

import android.content.Context
import android.content.SharedPreferences
import com.mongostudio.app.ui.theme.AppThemeMode
import com.mongostudio.app.ui.theme.ColorPalettePreset
import com.mongostudio.app.ui.theme.ThemeConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class ThemeSettings(
    val followSystemTheme: Boolean = true,
    val isDarkMode: Boolean = true,
    val isAmoledMode: Boolean = false,
    val palettePreset: ColorPalettePreset = ColorPalettePreset.DYNAMIC
) {
    val themeMode: AppThemeMode
        get() = when {
            followSystemTheme -> AppThemeMode.FOLLOW_SYSTEM
            isAmoledMode -> AppThemeMode.AMOLED
            isDarkMode -> AppThemeMode.DARK
            else -> AppThemeMode.LIGHT
        }

    fun toThemeConfig(): ThemeConfig = ThemeConfig(
        themeMode = themeMode,
        palettePreset = palettePreset,
        isAmoledMode = isAmoledMode
    )
}

class ThemePreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("mongostudio_theme_prefs", Context.MODE_PRIVATE)

    private val _themeSettings = MutableStateFlow(
        ThemeSettings(
            followSystemTheme = prefs.getBoolean(KEY_FOLLOW_SYSTEM, true),
            isDarkMode = prefs.getBoolean(KEY_DARK_MODE, true),
            isAmoledMode = prefs.getBoolean(KEY_AMOLED_MODE, false),
            palettePreset = loadPalettePreset()
        )
    )
    val themeSettings: StateFlow<ThemeSettings> = _themeSettings.asStateFlow()

    private fun loadPalettePreset(): ColorPalettePreset {
        val savedName = prefs.getString(KEY_PALETTE_PRESET, ColorPalettePreset.DYNAMIC.name)
        return try {
            ColorPalettePreset.valueOf(savedName ?: ColorPalettePreset.DYNAMIC.name)
        } catch (_: Exception) {
            ColorPalettePreset.DYNAMIC
        }
    }

    fun setFollowSystemTheme(enabled: Boolean) {
        val newPreset = if (enabled) ColorPalettePreset.DYNAMIC else _themeSettings.value.palettePreset
        prefs.edit()
            .putBoolean(KEY_FOLLOW_SYSTEM, enabled)
            .putString(KEY_PALETTE_PRESET, newPreset.name)
            .apply()
        _themeSettings.value = _themeSettings.value.copy(
            followSystemTheme = enabled,
            palettePreset = newPreset
        )
    }

    fun setDarkMode(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DARK_MODE, enabled).apply()
        _themeSettings.value = _themeSettings.value.copy(isDarkMode = enabled)
    }

    fun setAmoledMode(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AMOLED_MODE, enabled).apply()
        _themeSettings.value = _themeSettings.value.copy(isAmoledMode = enabled)
    }

    fun setPalettePreset(preset: ColorPalettePreset) {
        val newFollowSystem = (preset == ColorPalettePreset.DYNAMIC)
        prefs.edit()
            .putString(KEY_PALETTE_PRESET, preset.name)
            .putBoolean(KEY_FOLLOW_SYSTEM, newFollowSystem)
            .apply()
        _themeSettings.value = _themeSettings.value.copy(
            palettePreset = preset,
            followSystemTheme = newFollowSystem
        )
    }

    fun setThemeMode(mode: AppThemeMode) {
        when (mode) {
            AppThemeMode.FOLLOW_SYSTEM -> {
                setFollowSystemTheme(true)
            }
            AppThemeMode.LIGHT -> {
                setFollowSystemTheme(false)
                setDarkMode(false)
                setAmoledMode(false)
            }
            AppThemeMode.DARK -> {
                setFollowSystemTheme(false)
                setDarkMode(true)
                setAmoledMode(false)
            }
            AppThemeMode.AMOLED -> {
                setFollowSystemTheme(false)
                setDarkMode(true)
                setAmoledMode(true)
            }
        }
    }

    companion object {
        private const val KEY_FOLLOW_SYSTEM = "follow_system_theme"
        private const val KEY_DARK_MODE = "is_dark_mode"
        private const val KEY_AMOLED_MODE = "is_amoled_mode"
        private const val KEY_PALETTE_PRESET = "palette_preset"
    }
}
