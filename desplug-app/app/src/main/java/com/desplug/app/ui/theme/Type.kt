package com.desplug.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.desplug.app.R

val InterTightFamily = FontFamily(
    Font(R.font.inter_tight, FontWeight.Normal),
    Font(R.font.inter_tight, FontWeight.Medium),
    Font(R.font.inter_tight, FontWeight.SemiBold),
    Font(R.font.inter_tight, FontWeight.Bold)
)

val DesplugTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = InterTightFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 50.sp,
        lineHeight = 52.sp,
        letterSpacing = (-0.04).em,
        color = DesplugTextPrimary
    ),
    displayMedium = TextStyle(
        fontFamily = InterTightFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 46.sp,
        lineHeight = 48.sp,
        letterSpacing = (-0.04).em,
        color = DesplugTextPrimary
    ),
    headlineLarge = TextStyle(
        fontFamily = InterTightFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 36.sp,
        lineHeight = 40.sp,
        letterSpacing = (-0.03).em,
        color = DesplugTextPrimary
    ),
    titleLarge = TextStyle(
        fontFamily = InterTightFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 19.sp,
        lineHeight = 24.sp,
        letterSpacing = (-0.03).em,
        color = DesplugTextPrimary
    ),
    titleMedium = TextStyle(
        fontFamily = InterTightFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 15.sp,
        lineHeight = 20.sp,
        color = DesplugTextPrimary
    ),
    bodyLarge = TextStyle(
        fontFamily = InterTightFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.sp,
        color = DesplugTextPrimary
    ),
    bodyMedium = TextStyle(
        fontFamily = InterTightFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        color = DesplugTextSecondary
    ),
    labelLarge = TextStyle(
        fontFamily = InterTightFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 20.sp,
        color = DesplugTextPrimary
    ),
    labelMedium = TextStyle(
        fontFamily = InterTightFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        lineHeight = 16.sp,
        color = DesplugTextPrimary
    ),
    labelSmall = TextStyle(
        fontFamily = InterTightFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.08.em,
        color = DesplugTextSecondary
    )
)

// Legacy alias
val Typography = DesplugTypography
