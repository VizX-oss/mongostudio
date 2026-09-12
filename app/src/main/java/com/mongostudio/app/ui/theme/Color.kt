package com.mongostudio.app.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// ============================================================================
// Google Material 3 Expressive — 30 Surface & Color Roles (Light Emerald Pine)
// ============================================================================
val M3LightPrimary = Color(0xFF006C4C)
val M3LightOnPrimary = Color(0xFFFFFFFF)
val M3LightPrimaryContainer = Color(0xFF8CF8C7)
val M3LightOnPrimaryContainer = Color(0xFF002114)

val M3LightSecondary = Color(0xFF4C6357)
val M3LightOnSecondary = Color(0xFFFFFFFF)
val M3LightSecondaryContainer = Color(0xFFCEE9D9)
val M3LightOnSecondaryContainer = Color(0xFF092016)

val M3LightTertiary = Color(0xFF3E6374)
val M3LightOnTertiary = Color(0xFFFFFFFF)
val M3LightTertiaryContainer = Color(0xFFC1E8FC)
val M3LightOnTertiaryContainer = Color(0xFF001F2A)

val M3LightError = Color(0xFFBA1A1A)
val M3LightOnError = Color(0xFFFFFFFF)
val M3LightErrorContainer = Color(0xFFFFDAD6)
val M3LightOnErrorContainer = Color(0xFF410002)

val M3LightBackground = Color(0xFFF5FBF5)
val M3LightOnBackground = Color(0xFF171D1A)
val M3LightSurface = Color(0xFFF5FBF5)
val M3LightOnSurface = Color(0xFF171D1A)
val M3LightSurfaceVariant = Color(0xFFDBE5DE)
val M3LightOnSurfaceVariant = Color(0xFF404944)

val M3LightOutline = Color(0xFF707974)
val M3LightOutlineVariant = Color(0xFFBFC9C2)

val M3LightSurfaceDim = Color(0xFFD6DCD6)
val M3LightSurfaceBright = Color(0xFFF5FBF5)
val M3LightSurfaceContainerLowest = Color(0xFFFFFFFF)
val M3LightSurfaceContainerLow = Color(0xFFEFF5EF)
val M3LightSurfaceContainer = Color(0xFFE9EFEA)
val M3LightSurfaceContainerHigh = Color(0xFFE3EAE4)
val M3LightSurfaceContainerHighest = Color(0xFFDEE4DE)

// ============================================================================
// Google Material 3 Expressive — 30 Surface & Color Roles (Dark Emerald Pine)
// ============================================================================
val M3DarkPrimary = Color(0xFF6FDBAC)
val M3DarkOnPrimary = Color(0xFF003825)
val M3DarkPrimaryContainer = Color(0xFF005138)
val M3DarkOnPrimaryContainer = Color(0xFF8CF8C7)

val M3DarkSecondary = Color(0xFFB3CCBD)
val M3DarkOnSecondary = Color(0xFF1F352A)
val M3DarkSecondaryContainer = Color(0xFF354B40)
val M3DarkOnSecondaryContainer = Color(0xFFCEE9D9)

val M3DarkTertiary = Color(0xFFA6CCE0)
val M3DarkOnTertiary = Color(0xFF083544)
val M3DarkTertiaryContainer = Color(0xFF254C5B)
val M3DarkOnTertiaryContainer = Color(0xFFC1E8FC)

val M3DarkError = Color(0xFFFFB4AB)
val M3DarkOnError = Color(0xFF690005)
val M3DarkErrorContainer = Color(0xFF93000A)
val M3DarkOnErrorContainer = Color(0xFFFFDAD6)

val M3DarkBackground = Color(0xFF0F1512)
val M3DarkOnBackground = Color(0xFFDEE4DE)
val M3DarkSurface = Color(0xFF0F1512)
val M3DarkOnSurface = Color(0xFFDEE4DE)
val M3DarkSurfaceVariant = Color(0xFF404944)
val M3DarkOnSurfaceVariant = Color(0xFFBFC9C2)

val M3DarkOutline = Color(0xFF8A938D)
val M3DarkOutlineVariant = Color(0xFF404944)

val M3DarkSurfaceDim = Color(0xFF0F1512)
val M3DarkSurfaceBright = Color(0xFF353C37)
val M3DarkSurfaceContainerLowest = Color(0xFF0A0F0D)
val M3DarkSurfaceContainerLow = Color(0xFF171D1A)
val M3DarkSurfaceContainer = Color(0xFF1B211E)
val M3DarkSurfaceContainerHigh = Color(0xFF252C28)
val M3DarkSurfaceContainerHighest = Color(0xFF303733)

// ============================================================================
// Google Material 3 Expressive — AMOLED Pitch Black (#000000) Tokens
// ============================================================================
val AmoledBackground = Color(0xFF000000)
val AmoledSurface = Color(0xFF000000)
val AmoledOnSurface = Color(0xFFF0EDF2)
val AmoledSurfaceContainerLowest = Color(0xFF000000)
val AmoledSurfaceContainerLow = Color(0xFF0A0A0A)
val AmoledSurfaceContainer = Color(0xFF121212)
val AmoledSurfaceContainerHigh = Color(0xFF1C1C1E)
val AmoledSurfaceContainerHighest = Color(0xFF262628)
val AmoledOutline = Color(0xFF38383A)
val AmoledOutlineVariant = Color(0xFF222224)

/**
 * Creates pure AMOLED pitch-black color scheme with high contrast container tiers.
 */
fun createAmoledColorScheme(
    primary: Color,
    onPrimary: Color,
    primaryContainer: Color,
    onPrimaryContainer: Color,
    secondary: Color,
    onSecondary: Color,
    tertiary: Color,
    onTertiary: Color
) = darkColorScheme(
    primary = primary,
    onPrimary = onPrimary,
    primaryContainer = primaryContainer,
    onPrimaryContainer = onPrimaryContainer,
    secondary = secondary,
    onSecondary = onSecondary,
    tertiary = tertiary,
    onTertiary = onTertiary,
    background = AmoledBackground,
    onBackground = AmoledOnSurface,
    surface = AmoledSurface,
    onSurface = AmoledOnSurface,
    surfaceVariant = Color(0xFF1A1A1A),
    onSurfaceVariant = Color(0xFFCAC4D0),
    surfaceDim = AmoledBackground,
    surfaceBright = Color(0xFF2A2A2A),
    surfaceContainerLowest = AmoledSurfaceContainerLowest,
    surfaceContainerLow = AmoledSurfaceContainerLow,
    surfaceContainer = AmoledSurfaceContainer,
    surfaceContainerHigh = AmoledSurfaceContainerHigh,
    surfaceContainerHighest = AmoledSurfaceContainerHighest,
    outline = AmoledOutline,
    outlineVariant = AmoledOutlineVariant
)

val ExpressiveLightColorScheme = lightColorScheme(
    primary = M3LightPrimary,
    onPrimary = M3LightOnPrimary,
    primaryContainer = M3LightPrimaryContainer,
    onPrimaryContainer = M3LightOnPrimaryContainer,
    secondary = M3LightSecondary,
    onSecondary = M3LightOnSecondary,
    secondaryContainer = M3LightSecondaryContainer,
    onSecondaryContainer = M3LightOnSecondaryContainer,
    tertiary = M3LightTertiary,
    onTertiary = M3LightOnTertiary,
    tertiaryContainer = M3LightTertiaryContainer,
    onTertiaryContainer = M3LightOnTertiaryContainer,
    error = M3LightError,
    onError = M3LightOnError,
    errorContainer = M3LightErrorContainer,
    onErrorContainer = M3LightOnErrorContainer,
    background = M3LightBackground,
    onBackground = M3LightOnBackground,
    surface = M3LightSurface,
    onSurface = M3LightOnSurface,
    surfaceVariant = M3LightSurfaceVariant,
    onSurfaceVariant = M3LightOnSurfaceVariant,
    outline = M3LightOutline,
    outlineVariant = M3LightOutlineVariant,
    surfaceContainerLowest = M3LightSurfaceContainerLowest,
    surfaceContainerLow = M3LightSurfaceContainerLow,
    surfaceContainer = M3LightSurfaceContainer,
    surfaceContainerHigh = M3LightSurfaceContainerHigh,
    surfaceContainerHighest = M3LightSurfaceContainerHighest
)

val ExpressiveDarkColorScheme = darkColorScheme(
    primary = M3DarkPrimary,
    onPrimary = M3DarkOnPrimary,
    primaryContainer = M3DarkPrimaryContainer,
    onPrimaryContainer = M3DarkOnPrimaryContainer,
    secondary = M3DarkSecondary,
    onSecondary = M3DarkOnSecondary,
    secondaryContainer = M3DarkSecondaryContainer,
    onSecondaryContainer = M3DarkOnSecondaryContainer,
    tertiary = M3DarkTertiary,
    onTertiary = M3DarkOnTertiary,
    tertiaryContainer = M3DarkTertiaryContainer,
    onTertiaryContainer = M3DarkOnTertiaryContainer,
    error = M3DarkError,
    onError = M3DarkOnError,
    errorContainer = M3DarkErrorContainer,
    onErrorContainer = M3DarkOnErrorContainer,
    background = M3DarkBackground,
    onBackground = M3DarkOnBackground,
    surface = M3DarkSurface,
    onSurface = M3DarkOnSurface,
    surfaceVariant = M3DarkSurfaceVariant,
    onSurfaceVariant = M3DarkOnSurfaceVariant,
    outline = M3DarkOutline,
    outlineVariant = M3DarkOutlineVariant,
    surfaceContainerLowest = M3DarkSurfaceContainerLowest,
    surfaceContainerLow = M3DarkSurfaceContainerLow,
    surfaceContainer = M3DarkSurfaceContainer,
    surfaceContainerHigh = M3DarkSurfaceContainerHigh,
    surfaceContainerHighest = M3DarkSurfaceContainerHighest
)

val ExpressiveAmoledColorScheme = createAmoledColorScheme(
    primary = M3DarkPrimary,
    onPrimary = M3DarkOnPrimary,
    primaryContainer = Color(0xFF003825),
    onPrimaryContainer = Color(0xFF8CF8C7),
    secondary = M3DarkSecondary,
    onSecondary = M3DarkOnSecondary,
    tertiary = M3DarkTertiary,
    onTertiary = M3DarkOnTertiary
)

// ============================================================================
// Legacy & Compatibility Tokens (keeps tests and specific UI styles functioning)
// ============================================================================
val BackgroundDark = M3DarkBackground
val SurfaceDark = M3DarkSurface
val SurfaceContainerLowest = M3DarkSurfaceContainerLowest
val SurfaceContainerLow = M3DarkSurfaceContainerLow
val SurfaceContainer = M3DarkSurfaceContainer
val SurfaceContainerHigh = M3DarkSurfaceContainerHigh
val SurfaceContainerHighest = M3DarkSurfaceContainerHighest

val CardDark = SurfaceContainer
val CardBorderDark = M3DarkOutlineVariant
val CardBorderSubtle = M3DarkOutlineVariant.copy(alpha = 0.5f)
val HeaderDark = M3DarkSurface

val EmeraldPrimary = M3DarkPrimary
val EmeraldVibrant = Color(0xFF00F5A0)
val EmeraldLight = Color(0xFF34D399)
val EmeraldDark = Color(0xFF059669)
val EmeraldContainer = M3DarkPrimaryContainer
val EmeraldGlow = Color(0x4010B981)

val CyanAccent = Color(0xFF06B6D4)
val CyanGlow = Color(0x4006B6D4)
val SkyAccent = Color(0xFF38BDF8)
val SkyContainer = Color(0xFF0C4A6E)
val AmberAccent = Color(0xFFF59E0B)
val AmberContainer = Color(0xFF78350F)
val PurpleAccent = Color(0xFFA855F7)
val PurpleContainer = Color(0xFF581C87)
val RoseAccent = Color(0xFFF43F5E)
val RoseContainer = Color(0xFF881337)
val CoralAccent = Color(0xFFFF6B6B)
val IndigoAccent = Color(0xFF6366F1)

val TextPrimary = M3DarkOnSurface
val TextSecondary = M3DarkOnSurfaceVariant
val TextMuted = Color(0xFF8A938D)
val TextOnPrimary = M3DarkOnPrimary

// JSON Syntax Highlighting
val JsonKeyColor = Color(0xFF38BDF8)       // Vivid Sky Blue
val JsonStringColor = Color(0xFF34D399)    // Emerald Mint
val JsonNumberColor = Color(0xFFFBBF24)    // Sunburst Amber
val JsonBooleanColor = Color(0xFFA78BFA)   // Electric Lilac
val JsonNullColor = Color(0xFFFB7185)      // Coral Rose
val JsonBracketColor = Color(0xFF94A3B8)   // Slate
