package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = GoldPrimary,
    onPrimary = Color(0xFF1E1702),
    primaryContainer = GoldContainer,
    onPrimaryContainer = GoldLight,
    secondary = GoldLight,
    onSecondary = Color(0xFF1A1A1A),
    background = BlackPrimary,
    onBackground = TextWhite,
    surface = BlackSecondary,
    onSurface = TextWhite,
    surfaceVariant = BlackCard,
    onSurfaceVariant = TextMuted,
    outline = BlackBorder,
    error = ExpenseRed,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = GoldDark,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFF3D0),
    onPrimaryContainer = Color(0xFF4C3A00),
    secondary = GoldPrimary,
    onSecondary = Color.Black,
    background = LightBg,
    onBackground = LightText,
    surface = LightSurface,
    onSurface = LightText,
    surfaceVariant = LightCard,
    onSurfaceVariant = LightMutedText,
    outline = LightBorder,
    error = ExpenseRed,
    onError = Color.White
)

@Composable
fun GoldLedgerTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
