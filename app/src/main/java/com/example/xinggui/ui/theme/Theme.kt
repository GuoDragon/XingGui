package com.example.xinggui.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = IosBlue,
    secondary = StarCyan,
    tertiary = StarGold,
    background = IosGroupedBackground,
    surface = IosCard,
    surfaceVariant = IosCardMuted,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = IosTextPrimary,
    onSurface = IosTextPrimary,
    onSurfaceVariant = IosTextSecondary,
    outline = IosSeparator,
    error = IosRed
)

private val DarkColors = darkColorScheme(
    primary = StarBlue,
    secondary = StarCyan,
    tertiary = StarGold
)

@Composable
fun XingGuiTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = Typography,
        content = content
    )
}
