package com.mymaterials.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.mymaterials.app.data.preferences.ThemeMode

private fun createLightColorScheme(colors: AppColors) = lightColorScheme(
    primary = colors.blue,
    onPrimary = Color.White,
    background = colors.background,
    onBackground = colors.textPrimary,
    surface = colors.card,
    onSurface = colors.textPrimary,
    surfaceVariant = colors.cardSecondary,
    onSurfaceVariant = colors.textSecondary,
    outline = colors.separator
)

private fun createDarkColorScheme(colors: AppColors) = darkColorScheme(
    primary = colors.blue,
    onPrimary = Color.White,
    background = colors.background,
    onBackground = colors.textPrimary,
    surface = colors.card,
    onSurface = colors.textPrimary,
    surfaceVariant = colors.cardSecondary,
    onSurfaceVariant = colors.textSecondary,
    outline = colors.separator
)

@Composable
fun MyMaterialsTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val isSystemDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemDark
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    val appColors = if (isDark) DarkAppColors else LightAppColors
    val colorScheme = if (isDark) {
        createDarkColorScheme(appColors)
    } else {
        createLightColorScheme(appColors)
    }

    // إجبار اتجاه الواجهات دائماً على RTL مع توفير ألوان AppTheme
    CompositionLocalProvider(
        LocalAppColors provides appColors,
        LocalLayoutDirection provides LayoutDirection.Rtl
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            content = content
        )
    }
}
