package com.desplug.app.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = DesplugBlue,
    onPrimary = Color.White,
    primaryContainer = DesplugCardBg,
    onPrimaryContainer = Color.White,
    secondary = DesplugBlueMuted,
    onSecondary = Color.Black,
    background = DesplugBackground,
    onBackground = DesplugTextPrimary,
    surface = DesplugCardBg,
    onSurface = DesplugTextPrimary,
    surfaceVariant = DesplugTrackBg,
    onSurfaceVariant = DesplugTextSecondary,
    outline = DesplugCardBorder,
    error = DesplugRed,
    onError = Color.White
)

@Composable
fun DesplugTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false, // Desativado para manter o preto puro #000 do design oficial
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            window.statusBarColor = DesplugBackground.toArgb()
            window.navigationBarColor = DesplugBackground.toArgb()
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = false
            insetsController.isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = DesplugTypography,
        content = content
    )
}
