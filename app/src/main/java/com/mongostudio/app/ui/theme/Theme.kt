package com.mongostudio.app.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

@Composable
fun MongoStudioTheme(
    themeConfig: ThemeConfig = ThemeConfig(),
    content: @Composable () -> Unit
) {
    val systemInDark = isSystemInDarkTheme()
    val isDark = when (themeConfig.themeMode) {
        AppThemeMode.FOLLOW_SYSTEM -> systemInDark
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
        AppThemeMode.AMOLED -> true
    }

    val context = LocalContext.current
    val rawColorScheme = when {
        themeConfig.palettePreset == ColorPalettePreset.DYNAMIC && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (themeConfig.themeMode == AppThemeMode.AMOLED) {
                val dynDark = dynamicDarkColorScheme(context)
                createAmoledColorScheme(
                    dynDark.primary, dynDark.onPrimary, dynDark.primaryContainer, dynDark.onPrimaryContainer,
                    dynDark.secondary, dynDark.onSecondary, dynDark.tertiary, dynDark.onTertiary
                )
            } else if (isDark) {
                dynamicDarkColorScheme(context)
            } else {
                dynamicLightColorScheme(context)
            }
        }
        else -> {
            val palette = AppPalettes.getPreset(themeConfig.palettePreset)
            when {
                themeConfig.themeMode == AppThemeMode.AMOLED -> palette.amoled
                isDark -> palette.dark
                else -> palette.light
            }
        }
    }

    val animatedColorScheme = animateColorScheme(rawColorScheme)

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            window.statusBarColor = android.graphics.Color.TRANSPARENT
            window.navigationBarColor = android.graphics.Color.TRANSPARENT
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = !isDark
            insetsController.isAppearanceLightNavigationBars = !isDark
        }
    }

    CompositionLocalProvider(LocalThemeConfig provides themeConfig) {
        MaterialTheme(
            colorScheme = animatedColorScheme,
            typography = Typography,
            shapes = ExpressiveShapes,
            content = content
        )
    }
}

/**
 * Backward compatibility overload for legacy theme settings.
 */
@Composable
fun MongoStudioTheme(
    followSystemTheme: Boolean = true,
    darkTheme: Boolean = true,
    amoledMode: Boolean = false,
    palettePreset: ColorPalettePreset = ColorPalettePreset.EMERALD_PINE,
    content: @Composable () -> Unit
) {
    val themeMode = when {
        followSystemTheme -> AppThemeMode.FOLLOW_SYSTEM
        amoledMode -> AppThemeMode.AMOLED
        darkTheme -> AppThemeMode.DARK
        else -> AppThemeMode.LIGHT
    }
    MongoStudioTheme(
        themeConfig = ThemeConfig(themeMode = themeMode, palettePreset = palettePreset),
        content = content
    )
}
