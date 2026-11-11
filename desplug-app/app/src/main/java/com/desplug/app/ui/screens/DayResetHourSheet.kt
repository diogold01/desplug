package com.desplug.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.desplug.app.ui.components.DesplugCard
import com.desplug.app.ui.components.DesplugCardType
import com.desplug.app.ui.components.DesplugPrimaryButton
import com.desplug.app.ui.theme.DesplugBackground
import com.desplug.app.ui.theme.DesplugBlue
import com.desplug.app.ui.theme.DesplugSheetBg
import com.desplug.app.ui.theme.DesplugSheetBorder
import com.desplug.app.ui.theme.DesplugSheetHandle
import com.desplug.app.ui.theme.DesplugTextPrimary
import com.desplug.app.ui.theme.DesplugTextSecondary
import com.desplug.app.ui.theme.InterTightFamily
import com.desplug.app.util.rememberIsReducedMotion

@Composable
fun DayResetHourSheet(
    currentHour: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    BackHandler(onBack = onDismiss)

    val isReducedMotion = rememberIsReducedMotion()
    val density = LocalDensity.current
    val slideStartPx = with(density) { 500.dp.toPx() }
    val sheetOffsetY = remember { Animatable(if (isReducedMotion) 0f else slideStartPx) }

    LaunchedEffect(Unit, isReducedMotion) {
        if (!isReducedMotion) {
            sheetOffsetY.animateTo(
                targetValue = 0f,
                animationSpec = tween(
                    durationMillis = 800,
                    easing = CubicBezierEasing(0.2f, 0.9f, 0.2f, 1.0f)
                )
            )
        }
    }

    var selectedHour by remember { mutableIntStateOf(currentHour) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DesplugBackground.copy(alpha = 0.85f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onDismiss() },
        contentAlignment = Alignment.BottomCenter
    ) {
        val sheetShape = RoundedCornerShape(topStart = 36.dp, topEnd = 36.dp)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer { translationY = sheetOffsetY.value }
                .clip(sheetShape)
                .background(DesplugSheetBg)
                .border(1.dp, DesplugSheetBorder, sheetShape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { /* Consome toque */ }
                .navigationBarsPadding()
                .padding(start = 24.dp, end = 24.dp, top = 12.dp, bottom = 28.dp)
        ) {
            // Alça superior
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 20.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(DesplugSheetHandle)
            )

            Text(
                text = "Horário de Reinício",
                fontFamily = InterTightFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 32.sp,
                lineHeight = 36.sp,
                letterSpacing = (-0.03).em,
                color = DesplugTextPrimary
            )

            Text(
                text = "A que horas o seu dia reinicia para zerar o contador diário de tempo de uso?",
                fontFamily = InterTightFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                color = DesplugTextSecondary,
                modifier = Modifier.padding(top = 8.dp, bottom = 20.dp)
            )

            val options = listOf(
                0 to ("00:00" to "Meia-noite (Padrão)"),
                4 to ("04:00" to "Madrugada"),
                6 to ("06:00" to "Início da manhã")
            )

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                options.forEach { (hour, pair) ->
                    val (hourStr, desc) = pair
                    val isSelected = selectedHour == hour

                    DesplugCard(
                        type = if (isSelected) DesplugCardType.DEFAULT else DesplugCardType.DEFAULT,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedHour = hour }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = hourStr,
                                    fontFamily = InterTightFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 18.sp,
                                    color = if (isSelected) DesplugBlue else DesplugTextPrimary
                                )
                                Text(
                                    text = desc,
                                    fontFamily = InterTightFamily,
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 13.sp,
                                    color = DesplugTextSecondary,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                            QuickBlockCheckCircle(isChecked = isSelected)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            DesplugPrimaryButton(
                text = "Confirmar Reinício",
                onClick = { onConfirm(selectedHour) }
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
                    .height(44.dp)
                    .clickable { onDismiss() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Cancelar",
                    fontFamily = InterTightFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp,
                    color = DesplugTextSecondary
                )
            }
        }
    }
}
