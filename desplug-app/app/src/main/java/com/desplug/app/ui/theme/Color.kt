package com.desplug.app.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Cores base do Desplug Redesign (Mockup HTML)
val DesplugBackground = Color(0xFF000000)
val DesplugCardBg = Color(0xFF07090F)
val DesplugCardBorder = Color(0xFF151A26)

val DesplugBlue = Color(0xFF4C7DFF)
val DesplugBlueMuted = Color(0xFF9DB6FF)
val DesplugBlueGlow = Color(0x994C7DFF) // 60% alpha

val DesplugTextPrimary = Color(0xFFFFFFFF)
val DesplugTextSecondary = Color(0xFF8A93A6)
val DesplugTextBlue = Color(0xFFC9D6FF)

val DesplugRed = Color(0xFFFF5468)
val DesplugRedCardBg = Color(0xFF12060A)
val DesplugRedCardBorder = Color(0xFF3A1620)

val DesplugTrackBg = Color(0xFF12182A)
val DesplugRingTrackBg = Color(0xFF10162A)
val DesplugChipBorder = Color(0xFF232A3B)

val DesplugNavBg = Color(0xEB0C0E16) // rgba(12, 14, 22, 0.92)
val DesplugNavBorder = Color(0xFF1A2030)

val DesplugSheetBg = Color(0xFF07090F)
val DesplugSheetBorder = Color(0xFF1A2030)
val DesplugSheetHandle = Color(0xFF2B3347)

// Degradê a 160° para o Card de Bloqueios/Destaque
val DesplugHeroGradient = Brush.linearGradient(
    colors = listOf(
        Color(0xFF1A4BFF),
        Color(0xFF08216E),
        Color(0xFF02040C)
    )
)

// Legacy colors for backwards compatibility during migration
val Purple80 = DesplugBlue
val PurpleGrey80 = DesplugTextSecondary
val Pink80 = DesplugRed
val Purple40 = DesplugBlue
val PurpleGrey40 = DesplugTextSecondary
val Pink40 = DesplugRed
