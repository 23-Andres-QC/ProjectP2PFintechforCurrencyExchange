package com.example.p2p.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AppColorScheme = lightColorScheme(

    primary          = Primary,
    onPrimary        = Color.White,
    primaryContainer = Color(0xFFDCEBFF),
    onPrimaryContainer = PrimaryDark,

    secondary        = PrimaryLight,
    onSecondary      = Color.White,
    secondaryContainer = Color(0xFFE7F0FF),
    onSecondaryContainer = PrimaryDark,

    tertiary         = PrimaryMint,
    onTertiary       = PrimaryDark,
    tertiaryContainer = Color(0xFFE0F2FE),
    onTertiaryContainer = Color(0xFF075985),

    error            = DangerColor,
    onError          = Color.White,
    errorContainer   = Color(0xFFFFE4E4),
    onErrorContainer = Color(0xFF7F1D1D),

    background   = BackgroundApp,
    onBackground = TextMain,
    surface      = SurfaceColor,
    onSurface    = TextMain,

    surfaceVariant   = SurfaceElevated,
    onSurfaceVariant = TextSecond,

    outline        = BorderColor,
    outlineVariant = DividerColor,

    scrim = OverlayDark,

    inverseSurface   = DarkSurface,
    inverseOnSurface = Color.White,
    inversePrimary   = PrimaryLight,

    surfaceTint = Primary,
)

@Composable
fun P2PTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AppColorScheme,
        typography  = Typography,
        content     = content
    )
}
