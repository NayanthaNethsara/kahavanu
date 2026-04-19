package com.kahavanu.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = PrimaryBlue,
    secondary = SecondaryGreen,
    background = BackgroundGray,
    surface = SurfaceWhite,
)

private val DarkColors = darkColorScheme(
    primary = PrimaryBlue,
    secondary = SecondaryGreen,
    background = DarkBackground,
    surface = DarkSurface,
)

@Composable
fun KahavanuTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography,
        content = content,
    )
}
