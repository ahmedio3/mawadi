package com.mymaterials.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = IosBlue,
    onPrimary = IosCard,
    background = IosBackground,
    onBackground = IosTextPrimary,
    surface = IosCard,
    onSurface = IosTextPrimary,
    surfaceVariant = IosBackground,
    outline = IosSeparator
)

@Composable
fun MyMaterialsTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = AppTypography,
        content = content
    )
}
