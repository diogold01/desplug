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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.desplug.app.InstalledAppItem
import com.desplug.app.data.model.AppBlockRule
import com.desplug.app.ui.components.AppIconView
import com.desplug.app.ui.components.DesplugCard
import com.desplug.app.ui.components.DesplugCardType
import com.desplug.app.ui.components.DesplugChip
import com.desplug.app.ui.components.DesplugPrimaryButton
import com.desplug.app.ui.theme.DesplugBackground
import com.desplug.app.ui.theme.DesplugBlue
import com.desplug.app.ui.theme.DesplugCardBg
import com.desplug.app.ui.theme.DesplugCardBorder
import com.desplug.app.ui.theme.DesplugChipBorder
import com.desplug.app.ui.theme.DesplugRed
import com.desplug.app.ui.theme.DesplugSheetBg
import com.desplug.app.ui.theme.DesplugSheetBorder
import com.desplug.app.ui.theme.DesplugSheetHandle
import com.desplug.app.ui.theme.DesplugTextPrimary
import com.desplug.app.ui.theme.DesplugTextSecondary
import com.desplug.app.ui.theme.DesplugTrackBg
import com.desplug.app.ui.theme.InterTightFamily
import com.desplug.app.util.rememberIsReducedMotion

@Composable
fun AppLimitConfigSheet(
    appItem: InstalledAppItem,
    existingRule: AppBlockRule?,
    usedMinutesToday: Int,
    onDismiss: () -> Unit,
    onSave: (AppBlockRule?) -> Unit
) {
    BackHandler(onBack = onDismiss)

    val isReducedMotion = rememberIsReducedMotion()
    val density = LocalDensity.current
    val slideStartPx = with(density) { 600.dp.toPx() }
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

    var isAlwaysBlocked by remember {
        mutableStateOf(existingRule?.isAlwaysBlocked == true)
    }

    var limitMinutes by remember {
        mutableFloatStateOf(
            if (existingRule != null && existingRule.dailyLimitMinutes > 0) {
                existingRule.dailyLimitMinutes.toFloat()
            } else {
                30f
            }
        )
    }

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
                .padding(start = 24.dp, end = 24.dp, top = 12.dp, bottom = 32.dp)
                .verticalScroll(rememberScrollState())
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

            // Cabeçalho do App
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.padding(bottom = 20.dp)
            ) {
                AppIconView(
                    packageName = appItem.packageName,
                    appName = appItem.label,
                    size = 48.dp
                )
                Column {
                    Text(
                        text = appItem.label,
                        fontFamily = InterTightFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 20.sp,
                        color = DesplugTextPrimary
                    )
                    Text(
                        text = "Usado hoje: $usedMinutesToday minuto(s)",
                        fontFamily = InterTightFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 13.sp,
                        color = DesplugTextSecondary,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            // Card de Configuração de Minutos Diários
            DesplugCard(
                type = DesplugCardType.DEFAULT,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "LIMITE DIÁRIO",
                        fontFamily = InterTightFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        letterSpacing = 0.08.em,
                        color = DesplugTextSecondary
                    )

                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(top = 10.dp, bottom = 12.dp)
                    ) {
                        Text(
                            text = limitMinutes.toInt().toString(),
                            fontFamily = InterTightFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 46.sp,
                            lineHeight = 46.sp,
                            letterSpacing = (-0.05).em,
                            color = if (isAlwaysBlocked) DesplugTextSecondary else DesplugBlue
                        )
                        Text(
                            text = "minutos por dia",
                            fontFamily = InterTightFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 14.sp,
                            color = DesplugTextSecondary,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }

                    // Chips de Presets Rápidos
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(15f, 30f, 60f, 120f, 240f).forEach { preset ->
                            val label = if (preset >= 60f) "${(preset / 60).toInt()}h" else "${preset.toInt()}m"
                            DesplugChip(
                                text = label,
                                isSelected = !isAlwaysBlocked && limitMinutes.toInt() == preset.toInt(),
                                onClick = {
                                    isAlwaysBlocked = false
                                    limitMinutes = preset
                                }
                            )
                        }
                    }

                    Slider(
                        value = limitMinutes,
                        onValueChange = {
                            isAlwaysBlocked = false
                            limitMinutes = it
                        },
                        valueRange = 5f..240f,
                        steps = 46,
                        colors = SliderDefaults.colors(
                            thumbColor = DesplugBlue,
                            activeTrackColor = DesplugBlue,
                            inactiveTrackColor = DesplugTrackBg
                        ),
                        enabled = !isAlwaysBlocked
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Card de Bloqueio Imediato Total
            DesplugCard(
                type = if (isAlwaysBlocked) DesplugCardType.ALERT else DesplugCardType.DEFAULT,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isAlwaysBlocked = !isAlwaysBlocked }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Bloqueio Imediato Total",
                            fontFamily = InterTightFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp,
                            color = if (isAlwaysBlocked) DesplugRed else DesplugTextPrimary
                        )
                        Text(
                            text = "Não permite abrir o app em nenhum momento do dia.",
                            fontFamily = InterTightFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 13.sp,
                            color = DesplugTextSecondary,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                    Switch(
                        checked = isAlwaysBlocked,
                        onCheckedChange = { isAlwaysBlocked = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = DesplugRed,
                            uncheckedThumbColor = DesplugTextSecondary,
                            uncheckedTrackColor = DesplugCardBorder
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Botão Principal: Salvar Regra
            DesplugPrimaryButton(
                text = "Salvar Limite",
                onClick = {
                    val rule = AppBlockRule(
                        packageName = appItem.packageName,
                        appName = appItem.label,
                        dailyLimitMinutes = if (!isAlwaysBlocked) limitMinutes.toInt() else 0,
                        isAlwaysBlocked = isAlwaysBlocked
                    )
                    onSave(rule)
                }
            )

            // Botão Remover Limite (caso já exista)
            if (existingRule != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                        .height(44.dp)
                        .clickable { onSave(null) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Remover Limite do App",
                        fontFamily = InterTightFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        color = DesplugRed
                    )
                }
            }

            // Botão Cancelar
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
