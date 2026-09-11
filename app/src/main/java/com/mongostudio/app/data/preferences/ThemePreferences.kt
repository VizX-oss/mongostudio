package com.mongostudio.app.data.preferences

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class ThemeSettings(
    val followSystemTheme: Boolean = true,
    val isDarkMode: Boolean = true,
    val isAmoledMode: Boolean = false
)

class ThemePreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("mongostudio_theme_prefs", Context.MODE_PRIVATE)

    private val _themeSettings = MutableStateFlow(
        ThemeSettings(
            followSystemTheme = prefs.getBoolean(KEY_FOLLOW_SYSTEM, true),
            isDarkMode = prefs.getBoolean(KEY_DARK_MODE, true),
            isAmoledMode = prefs.getBoolean(KEY_AMOLED_MODE, false)
        )
    )
    val themeSettings: StateFlow<ThemeSettings> = _themeSettings.asStateFlow()

    fun setFollowSystemTheme(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_FOLLOW_SYSTEM, enabled).apply()
        _themeSettings.value = _themeSettings.value.copy(followSystemTheme = enabled)
    }

    fun setDarkMode(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DARK_MODE, enabled).apply()
        _themeSettings.value = _themeSettings.value.copy(isDarkMode = enabled)
    }

    fun setAmoledMode(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AMOLED_MODE, enabled).apply()
        _themeSettings.value = _themeSettings.value.copy(isAmoledMode = enabled)
    }

    companion object {
        private const val KEY_FOLLOW_SYSTEM = "follow_system_theme"
        private const val KEY_DARK_MODE = "is_dark_mode"
        private const val KEY_AMOLED_MODE = "is_amoled_mode"
    }
}
