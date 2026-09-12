package com.mongostudio.app.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

enum class ColorPalettePreset(val displayName: String) {
    EMERALD_PINE("Emerald Pine"),
    VIOLET("Violet Expressive"),
    OCEAN_TEAL("Ocean Teal"),
    SUNSET_AMBER("Sunset Amber"),
    CRIMSON_BLOOM("Crimson Bloom"),
    NORDIC_SLATE("Nordic Slate"),
    DYNAMIC("Dynamic Monet")
}

data class PaletteTheme(
    val light: ColorScheme,
    val dark: ColorScheme,
    val amoled: ColorScheme,
    val primaryColor: Color
)

object AppPalettes {
    // 1. Emerald Pine (Signature MongoDB / MongoStudio Default)
    val EmeraldPine = PaletteTheme(
        primaryColor = Color(0xFF006C4C),
        light = lightColorScheme(
            primary = Color(0xFF006C4C),
            onPrimary = Color.White,
            primaryContainer = Color(0xFF8CF8C7),
            onPrimaryContainer = Color(0xFF002114),
            secondary = Color(0xFF4C6357),
            onSecondary = Color.White,
            secondaryContainer = Color(0xFFCEE9D9),
            onSecondaryContainer = Color(0xFF092016),
            tertiary = Color(0xFF3E6374),
            onTertiary = Color.White,
            tertiaryContainer = Color(0xFFC1E8FC),
            onTertiaryContainer = Color(0xFF001F2A),
            background = Color(0xFFF5FBF5),
            onBackground = Color(0xFF171D1A),
            surface = Color(0xFFF5FBF5),
            onSurface = Color(0xFF171D1A),
            surfaceVariant = Color(0xFFDBE5DE),
            onSurfaceVariant = Color(0xFF404944),
            surfaceContainerLowest = Color(0xFFFFFFFF),
            surfaceContainerLow = Color(0xFFEFF5EF),
            surfaceContainer = Color(0xFFE9EFEA),
            surfaceContainerHigh = Color(0xFFE3EAE4),
            surfaceContainerHighest = Color(0xFFDEE4DE),
            outline = Color(0xFF707974),
            outlineVariant = Color(0xFFBFC9C2)
        ),
        dark = darkColorScheme(
            primary = Color(0xFF6FDBAC),
            onPrimary = Color(0xFF003825),
            primaryContainer = Color(0xFF005138),
            onPrimaryContainer = Color(0xFF8CF8C7),
            secondary = Color(0xFFB3CCBD),
            onSecondary = Color(0xFF1F352A),
            secondaryContainer = Color(0xFF354B40),
            onSecondaryContainer = Color(0xFFCEE9D9),
            tertiary = Color(0xFFA6CCE0),
            onTertiary = Color(0xFF083544),
            tertiaryContainer = Color(0xFF254C5B),
            onTertiaryContainer = Color(0xFFC1E8FC),
            background = Color(0xFF0F1512),
            onBackground = Color(0xFFDEE4DE),
            surface = Color(0xFF0F1512),
            onSurface = Color(0xFFDEE4DE),
            surfaceVariant = Color(0xFF404944),
            onSurfaceVariant = Color(0xFFBFC9C2),
            surfaceContainerLowest = Color(0xFF0A0F0D),
            surfaceContainerLow = Color(0xFF171D1A),
            surfaceContainer = Color(0xFF1B211E),
            surfaceContainerHigh = Color(0xFF252C28),
            surfaceContainerHighest = Color(0xFF303733),
            outline = Color(0xFF8A938D),
            outlineVariant = Color(0xFF404944)
        ),
        amoled = createAmoledColorScheme(
            primary = Color(0xFF6FDBAC),
            onPrimary = Color(0xFF003825),
            primaryContainer = Color(0xFF003825),
            onPrimaryContainer = Color(0xFF8CF8C7),
            secondary = Color(0xFFB3CCBD),
            onSecondary = Color(0xFF1F352A),
            tertiary = Color(0xFFA6CCE0),
            onTertiary = Color(0xFF083544)
        )
    )

    // 2. Violet Expressive (MD3 Expressive Base)
    val Violet = PaletteTheme(
        primaryColor = Color(0xFF6750A4),
        light = lightColorScheme(
            primary = Color(0xFF6750A4),
            onPrimary = Color.White,
            primaryContainer = Color(0xFFEADDFF),
            onPrimaryContainer = Color(0xFF21005D),
            secondary = Color(0xFF625B71),
            onSecondary = Color.White,
            secondaryContainer = Color(0xFFE8DEF8),
            onSecondaryContainer = Color(0xFF1D192B),
            tertiary = Color(0xFF7D5260),
            onTertiary = Color.White,
            tertiaryContainer = Color(0xFFFFD8E4),
            onTertiaryContainer = Color(0xFF31111D),
            background = Color(0xFFFEF7FF),
            onBackground = Color(0xFF1D1B20),
            surface = Color(0xFFFEF7FF),
            onSurface = Color(0xFF1D1B20),
            surfaceVariant = Color(0xFFE7E0EB),
            onSurfaceVariant = Color(0xFF49454F),
            surfaceContainerLowest = Color(0xFFFFFFFF),
            surfaceContainerLow = Color(0xFFF7F2FA),
            surfaceContainer = Color(0xFFF3EDF7),
            surfaceContainerHigh = Color(0xFFECE6F0),
            surfaceContainerHighest = Color(0xFFE6E0E9),
            outline = Color(0xFF79747E),
            outlineVariant = Color(0xFFCAC4D0)
        ),
        dark = darkColorScheme(
            primary = Color(0xFFD0BCFF),
            onPrimary = Color(0xFF381E72),
            primaryContainer = Color(0xFF4F378B),
            onPrimaryContainer = Color(0xFFEADDFF),
            secondary = Color(0xFFCCC2DC),
            onSecondary = Color(0xFF332D41),
            secondaryContainer = Color(0xFF4A4458),
            onSecondaryContainer = Color(0xFFE8DEF8),
            tertiary = Color(0xFFEFB8C8),
            onTertiary = Color(0xFF492532),
            tertiaryContainer = Color(0xFF633B48),
            onTertiaryContainer = Color(0xFFFFD8E4),
            background = Color(0xFF141218),
            onBackground = Color(0xFFE6E0E9),
            surface = Color(0xFF141218),
            onSurface = Color(0xFFE6E0E9),
            surfaceVariant = Color(0xFF49454F),
            onSurfaceVariant = Color(0xFFCAC4D0),
            surfaceContainerLowest = Color(0xFF0F0D13),
            surfaceContainerLow = Color(0xFF1D1B20),
            surfaceContainer = Color(0xFF211F26),
            surfaceContainerHigh = Color(0xFF2B2930),
            surfaceContainerHighest = Color(0xFF36343B),
            outline = Color(0xFF938F99),
            outlineVariant = Color(0xFF49454F)
        ),
        amoled = createAmoledColorScheme(
            primary = Color(0xFFD0BCFF),
            onPrimary = Color(0xFF381E72),
            primaryContainer = Color(0xFF381E72),
            onPrimaryContainer = Color(0xFFEADDFF),
            secondary = Color(0xFFCCC2DC),
            onSecondary = Color(0xFF332D41),
            tertiary = Color(0xFFEFB8C8),
            onTertiary = Color(0xFF492532)
        )
    )

    // 3. Ocean Teal
    val OceanTeal = PaletteTheme(
        primaryColor = Color(0xFF006874),
        light = lightColorScheme(
            primary = Color(0xFF006874),
            onPrimary = Color.White,
            primaryContainer = Color(0xFF97F0FF),
            onPrimaryContainer = Color(0xFF001F24),
            secondary = Color(0xFF4A6267),
            onSecondary = Color.White,
            secondaryContainer = Color(0xFFCDE7ED),
            onSecondaryContainer = Color(0xFF051F23),
            tertiary = Color(0xFF525E7D),
            onTertiary = Color.White,
            tertiaryContainer = Color(0xFFDAE2FF),
            onTertiaryContainer = Color(0xFF0E1A37),
            background = Color(0xFFF4FAFB),
            onBackground = Color(0xFF161D1E),
            surface = Color(0xFFF4FAFB),
            onSurface = Color(0xFF161D1E),
            surfaceVariant = Color(0xFFDBE4E6),
            onSurfaceVariant = Color(0xFF3F484A),
            surfaceContainerLowest = Color(0xFFFFFFFF),
            surfaceContainerLow = Color(0xFFEEF5F6),
            surfaceContainer = Color(0xFFE8EFF0),
            surfaceContainerHigh = Color(0xFFE2EAEB),
            surfaceContainerHighest = Color(0xFFDCE4E5),
            outline = Color(0xFF6F797B),
            outlineVariant = Color(0xFFBFC8CA)
        ),
        dark = darkColorScheme(
            primary = Color(0xFF4ED8E8),
            onPrimary = Color(0xFF00363D),
            primaryContainer = Color(0xFF004F58),
            onPrimaryContainer = Color(0xFF97F0FF),
            secondary = Color(0xFFB1CBD0),
            onSecondary = Color(0xFF1C3438),
            secondaryContainer = Color(0xFF334B4F),
            onSecondaryContainer = Color(0xFFCDE7ED),
            tertiary = Color(0xFFBAC6EA),
            onTertiary = Color(0xFF24304D),
            tertiaryContainer = Color(0xFF3B4664),
            onTertiaryContainer = Color(0xFFDAE2FF),
            background = Color(0xFF0E1415),
            onBackground = Color(0xFFDEE4E5),
            surface = Color(0xFF0E1415),
            onSurface = Color(0xFFDEE4E5),
            surfaceVariant = Color(0xFF3F484A),
            onSurfaceVariant = Color(0xFFBFC8CA),
            surfaceContainerLowest = Color(0xFF090F10),
            surfaceContainerLow = Color(0xFF161D1E),
            surfaceContainer = Color(0xFF1A2122),
            surfaceContainerHigh = Color(0xFF252B2C),
            surfaceContainerHighest = Color(0xFF303637),
            outline = Color(0xFF899295),
            outlineVariant = Color(0xFF3F484A)
        ),
        amoled = createAmoledColorScheme(
            primary = Color(0xFF4ED8E8),
            onPrimary = Color(0xFF00363D),
            primaryContainer = Color(0xFF004F58),
            onPrimaryContainer = Color(0xFF97F0FF),
            secondary = Color(0xFFB1CBD0),
            onSecondary = Color(0xFF1C3438),
            tertiary = Color(0xFFBAC6EA),
            onTertiary = Color(0xFF24304D)
        )
    )

    // 4. Sunset Amber
    val SunsetAmber = PaletteTheme(
        primaryColor = Color(0xFF8E4E00),
        light = lightColorScheme(
            primary = Color(0xFF8E4E00),
            onPrimary = Color.White,
            primaryContainer = Color(0xFFFFDCC1),
            onPrimaryContainer = Color(0xFF2E1500),
            secondary = Color(0xFF735946),
            onSecondary = Color.White,
            secondaryContainer = Color(0xFFFFDCC1),
            onSecondaryContainer = Color(0xFF2A1709),
            tertiary = Color(0xFF5D6236),
            onTertiary = Color.White,
            tertiaryContainer = Color(0xFFE2E7B0),
            onTertiaryContainer = Color(0xFF1A1E00),
            background = Color(0xFFFFF8F5),
            onBackground = Color(0xFF221A14),
            surface = Color(0xFFFFF8F5),
            onSurface = Color(0xFF221A14),
            surfaceVariant = Color(0xFFF2DFD1),
            onSurfaceVariant = Color(0xFF51443B),
            surfaceContainerLowest = Color(0xFFFFFFFF),
            surfaceContainerLow = Color(0xFFFFF1E9),
            surfaceContainer = Color(0xFFFCEBE1),
            surfaceContainerHigh = Color(0xFFF6E5DC),
            surfaceContainerHighest = Color(0xFFF0DFD6),
            outline = Color(0xFF84746A),
            outlineVariant = Color(0xFFD6C3B6)
        ),
        dark = darkColorScheme(
            primary = Color(0xFFFFB77C),
            onPrimary = Color(0xFF4C2700),
            primaryContainer = Color(0xFF6C3A00),
            onPrimaryContainer = Color(0xFFFFDCC1),
            secondary = Color(0xFFE2BFA9),
            onSecondary = Color(0xFF412B1B),
            secondaryContainer = Color(0xFF5A4130),
            onSecondaryContainer = Color(0xFFFFDCC1),
            tertiary = Color(0xFFC6CB96),
            onTertiary = Color(0xFF2F330C),
            tertiaryContainer = Color(0xFF454A20),
            onTertiaryContainer = Color(0xFFE2E7B0),
            background = Color(0xFF19120C),
            onBackground = Color(0xFFEFE0D7),
            surface = Color(0xFF19120C),
            onSurface = Color(0xFFEFE0D7),
            surfaceVariant = Color(0xFF51443B),
            onSurfaceVariant = Color(0xFFD6C3B6),
            surfaceContainerLowest = Color(0xFF140D08),
            surfaceContainerLow = Color(0xFF221A14),
            surfaceContainer = Color(0xFF261E18),
            surfaceContainerHigh = Color(0xFF312822),
            surfaceContainerHighest = Color(0xFF3C332C),
            outline = Color(0xFF9E8E82),
            outlineVariant = Color(0xFF51443B)
        ),
        amoled = createAmoledColorScheme(
            primary = Color(0xFFFFB77C),
            onPrimary = Color(0xFF4C2700),
            primaryContainer = Color(0xFF6C3A00),
            onPrimaryContainer = Color(0xFFFFDCC1),
            secondary = Color(0xFFE2BFA9),
            onSecondary = Color(0xFF412B1B),
            tertiary = Color(0xFFC6CB96),
            onTertiary = Color(0xFF2F330C)
        )
    )

    // 5. Crimson Bloom
    val CrimsonBloom = PaletteTheme(
        primaryColor = Color(0xFF9E2A43),
        light = lightColorScheme(
            primary = Color(0xFF9E2A43),
            onPrimary = Color.White,
            primaryContainer = Color(0xFFFFD9DD),
            onPrimaryContainer = Color(0xFF400010),
            secondary = Color(0xFF75565B),
            onSecondary = Color.White,
            secondaryContainer = Color(0xFFFFD9DD),
            onSecondaryContainer = Color(0xFF2B1519),
            tertiary = Color(0xFF795831),
            onTertiary = Color.White,
            tertiaryContainer = Color(0xFFFFDDB9),
            onTertiaryContainer = Color(0xFF2C1600),
            background = Color(0xFFFFF8F7),
            onBackground = Color(0xFF22191B),
            surface = Color(0xFFFFF8F7),
            onSurface = Color(0xFF22191B),
            surfaceVariant = Color(0xFFF3DDE0),
            onSurfaceVariant = Color(0xFF524345),
            surfaceContainerLowest = Color(0xFFFFFFFF),
            surfaceContainerLow = Color(0xFFFFF0F1),
            surfaceContainer = Color(0xFFFCEAEB),
            surfaceContainerHigh = Color(0xFFF6E4E6),
            surfaceContainerHighest = Color(0xFFF0DFE1),
            outline = Color(0xFF847375),
            outlineVariant = Color(0xFFD6C2C4)
        ),
        dark = darkColorScheme(
            primary = Color(0xFFFFB2BC),
            onPrimary = Color(0xFF60001D),
            primaryContainer = Color(0xFF80102E),
            onPrimaryContainer = Color(0xFFFFD9DD),
            secondary = Color(0xFFE5BDC1),
            onSecondary = Color(0xFF43292E),
            secondaryContainer = Color(0xFF5C3F44),
            onSecondaryContainer = Color(0xFFFFD9DD),
            tertiary = Color(0xFFEBBF90),
            onTertiary = Color(0xFF452B07),
            tertiaryContainer = Color(0xFF5F411B),
            onTertiaryContainer = Color(0xFFFFDDB9),
            background = Color(0xFF1A1113),
            onBackground = Color(0xFFEFE0E1),
            surface = Color(0xFF1A1113),
            onSurface = Color(0xFFEFE0E1),
            surfaceVariant = Color(0xFF524345),
            onSurfaceVariant = Color(0xFFD6C2C4),
            surfaceContainerLowest = Color(0xFF140C0E),
            surfaceContainerLow = Color(0xFF22191B),
            surfaceContainer = Color(0xFF261D1F),
            surfaceContainerHigh = Color(0xFF312829),
            surfaceContainerHighest = Color(0xFF3D3234),
            outline = Color(0xFF9F8C8F),
            outlineVariant = Color(0xFF524345)
        ),
        amoled = createAmoledColorScheme(
            primary = Color(0xFFFFB2BC),
            onPrimary = Color(0xFF60001D),
            primaryContainer = Color(0xFF80102E),
            onPrimaryContainer = Color(0xFFFFD9DD),
            secondary = Color(0xFFE5BDC1),
            onSecondary = Color(0xFF43292E),
            tertiary = Color(0xFFEBBF90),
            onTertiary = Color(0xFF452B07)
        )
    )

    // 6. Nordic Slate
    val NordicSlate = PaletteTheme(
        primaryColor = Color(0xFF355CA8),
        light = lightColorScheme(
            primary = Color(0xFF355CA8),
            onPrimary = Color.White,
            primaryContainer = Color(0xFFD9E2FF),
            onPrimaryContainer = Color(0xFF001945),
            secondary = Color(0xFF575E71),
            onSecondary = Color.White,
            secondaryContainer = Color(0xFFDBE2F9),
            onSecondaryContainer = Color(0xFF141B2C),
            tertiary = Color(0xFF715574),
            onTertiary = Color.White,
            tertiaryContainer = Color(0xFFFCD7FD),
            onTertiaryContainer = Color(0xFF2A132E),
            background = Color(0xFFF9F9FF),
            onBackground = Color(0xFF1A1B20),
            surface = Color(0xFFF9F9FF),
            onSurface = Color(0xFF1A1B20),
            surfaceVariant = Color(0xFFE1E2EC),
            onSurfaceVariant = Color(0xFF44464F),
            surfaceContainerLowest = Color(0xFFFFFFFF),
            surfaceContainerLow = Color(0xFFF3F3FA),
            surfaceContainer = Color(0xFFEDEDF4),
            surfaceContainerHigh = Color(0xFFE7E8EE),
            surfaceContainerHighest = Color(0xFFE2E2E9),
            outline = Color(0xFF757780),
            outlineVariant = Color(0xFFC5C6D0)
        ),
        dark = darkColorScheme(
            primary = Color(0xFFAFC6FF),
            onPrimary = Color(0xFF002D6D),
            primaryContainer = Color(0xFF17438F),
            onPrimaryContainer = Color(0xFFD9E2FF),
            secondary = Color(0xFFBFC6DC),
            onSecondary = Color(0xFF293041),
            secondaryContainer = Color(0xFF3F4759),
            onSecondaryContainer = Color(0xFFDBE2F9),
            tertiary = Color(0xFFDFBDE0),
            onTertiary = Color(0xFF402844),
            tertiaryContainer = Color(0xFF583E5B),
            onTertiaryContainer = Color(0xFFFCD7FD),
            background = Color(0xFF121318),
            onBackground = Color(0xFFE3E2E9),
            surface = Color(0xFF121318),
            onSurface = Color(0xFFE3E2E9),
            surfaceVariant = Color(0xFF44464F),
            onSurfaceVariant = Color(0xFFC5C6D0),
            surfaceContainerLowest = Color(0xFF0C0E13),
            surfaceContainerLow = Color(0xFF1A1B20),
            surfaceContainer = Color(0xFF1E1F25),
            surfaceContainerHigh = Color(0xFF282A2F),
            surfaceContainerHighest = Color(0xFF33353A),
            outline = Color(0xFF8F909A),
            outlineVariant = Color(0xFF44464F)
        ),
        amoled = createAmoledColorScheme(
            primary = Color(0xFFAFC6FF),
            onPrimary = Color(0xFF002D6D),
            primaryContainer = Color(0xFF17438F),
            onPrimaryContainer = Color(0xFFD9E2FF),
            secondary = Color(0xFFBFC6DC),
            onSecondary = Color(0xFF293041),
            tertiary = Color(0xFFDFBDE0),
            onTertiary = Color(0xFF402844)
        )
    )

    fun getPreset(preset: ColorPalettePreset): PaletteTheme = when (preset) {
        ColorPalettePreset.EMERALD_PINE -> EmeraldPine
        ColorPalettePreset.VIOLET -> Violet
        ColorPalettePreset.OCEAN_TEAL -> OceanTeal
        ColorPalettePreset.SUNSET_AMBER -> SunsetAmber
        ColorPalettePreset.CRIMSON_BLOOM -> CrimsonBloom
        ColorPalettePreset.NORDIC_SLATE -> NordicSlate
        ColorPalettePreset.DYNAMIC -> EmeraldPine // Fallback if dynamic unavailable
    }
}
