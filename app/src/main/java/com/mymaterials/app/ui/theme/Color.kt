package com.mymaterials.app.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// Light Colors (iOS Grouped Style - هادئة جداً)
val IosBackgroundLight = Color(0xFFF2F2F7)
val IosCardLight = Color(0xFFFFFFFF)
val IosCardSecondaryLight = Color(0xFFE5E5EA)
val IosTextPrimaryLight = Color(0xFF000000)
val IosTextSecondaryLight = Color(0xFF6C6C70)
val IosSeparatorLight = Color(0xFFC6C6C8)
val IosBlueLight = Color(0xFF007AFF)
val IosGreenLight = Color(0xFF34C759)
val IosOrangeLight = Color(0xFFFF9500)
val IosGrayLight = Color(0xFF8E8E93)

// Dark Colors (iOS Dark Grouped Style - هادئة ومريحة للعين)
val IosBackgroundDark = Color(0xFF121214)
val IosCardDark = Color(0xFF1C1C1E)
val IosCardSecondaryDark = Color(0xFF2C2C2E)
val IosTextPrimaryDark = Color(0xFFFFFFFF)
val IosTextSecondaryDark = Color(0xFF8E8E93)
val IosSeparatorDark = Color(0xFF38383A)
val IosBlueDark = Color(0xFF0A84FF)
val IosGreenDark = Color(0xFF30D158)
val IosOrangeDark = Color(0xFFFF9F0A)
val IosGrayDark = Color(0xFF8E8E93)

// Static references preserved for backwards compatibility
val IosBackground = IosBackgroundLight
val IosCard = IosCardLight
val IosBlue = IosBlueLight
val IosGreen = IosGreenLight
val IosOrange = IosOrangeLight
val IosGray = IosGrayLight
val IosSeparator = IosSeparatorLight
val IosTextPrimary = IosTextPrimaryLight
val IosTextSecondary = IosTextSecondaryLight

data class AppColors(
    val background: Color,
    val card: Color,
    val cardSecondary: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val separator: Color,
    val blue: Color,
    val green: Color,
    val orange: Color,
    val gray: Color,
    val isDark: Boolean
)

val LightAppColors = AppColors(
    background = IosBackgroundLight,
    card = IosCardLight,
    cardSecondary = IosCardSecondaryLight,
    textPrimary = IosTextPrimaryLight,
    textSecondary = IosTextSecondaryLight,
    separator = IosSeparatorLight,
    blue = IosBlueLight,
    green = IosGreenLight,
    orange = IosOrangeLight,
    gray = IosGrayLight,
    isDark = false
)

val DarkAppColors = AppColors(
    background = IosBackgroundDark,
    card = IosCardDark,
    cardSecondary = IosCardSecondaryDark,
    textPrimary = IosTextPrimaryDark,
    textSecondary = IosTextSecondaryDark,
    separator = IosSeparatorDark,
    blue = IosBlueDark,
    green = IosGreenDark,
    orange = IosOrangeDark,
    gray = IosGrayDark,
    isDark = true
)

val LocalAppColors = staticCompositionLocalOf { LightAppColors }

object AppTheme {
    val colors: AppColors
        @Composable
        @ReadOnlyComposable
        get() = LocalAppColors.current
}
