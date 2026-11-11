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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
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
import com.desplug.app.ui.components.ModernLimitSlider
import com.desplug.app.ui.components.interpolateLimitColor
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
                .navigationBarsPadding()
                .padding(start = 24.dp, end = 24.dp, top = 12.dp, bottom = 28.dp)
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

            // Card de Configuração de Minutos Diários (Modern Limit Slider)
            val currentProgress = ((limitMinutes - 5f) / (240f - 5f)).coerceIn(0f, 1f)
            val currentColor = interpolateLimitColor(currentProgress)
            val dayPercent = ((limitMinutes / 1440f) * 100).toInt().coerceAtLeast(1)

            DesplugCard(
                type = DesplugCardType.DEFAULT,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    // Brilho ambiente no topo do card (radial-gradient sutil com a cor atual)
                    if (!isAlwaysBlocked) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            currentColor.copy(alpha = 0.16f),
                                            Color.Transparent
                                        ),
                                        center = Offset(200f, 0f),
                                        radius = 500f
                                    )
                                )
                        )
                    }

                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "LIMITE DIÁRIO",
                            fontFamily = InterTightFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            letterSpacing = 0.14.em,
                            color = Color(0xFF8B8B99)
                        )

                        // Número Grande com gradiente branco -> currentColor
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.padding(top = 10.dp, bottom = 4.dp)
                        ) {
                            if (isAlwaysBlocked) {
                                Text(
                                    text = limitMinutes.toInt().toString(),
                                    fontFamily = InterTightFamily,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 64.sp,
                                    lineHeight = 64.sp,
                                    letterSpacing = (-0.02).em,
                                    color = DesplugTextSecondary
                                )
                            } else {
                                Text(
                                    text = limitMinutes.toInt().toString(),
                                    fontFamily = InterTightFamily,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 64.sp,
                                    lineHeight = 64.sp,
                                    letterSpacing = (-0.02).em,
                                    style = TextStyle(
                                        brush = Brush.verticalGradient(
                                            colors = listOf(Color.White, currentColor)
                                        )
                                    )
                                )
                            }
                            Text(
                                text = "minutos por dia",
                                fontFamily = InterTightFamily,
                                fontWeight = FontWeight.Normal,
                                fontSize = 15.sp,
                                color = Color(0xFF9A9AA8),
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }

                        // Hint: "X% do seu dia no app"
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 18.dp)
                        ) {
                            Text(
                                text = "$dayPercent%",
                                fontFamily = InterTightFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                color = if (isAlwaysBlocked) DesplugTextSecondary else currentColor
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "do seu dia no app",
                                fontFamily = InterTightFamily,
                                fontSize = 13.sp,
                                color = Color(0xFF7A7A88)
                            )
                        }

                        // Chips de Presets Rápidos Sincronizados (15m, 30m, 1h, 2h, 4h)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(
                                15f to "15m",
                                30f to "30m",
                                60f to "1h",
                                120f to "2h",
                                240f to "4h"
                            ).forEach { (preset, label) ->
                                val isSelected = !isAlwaysBlocked && limitMinutes.toInt() == preset.toInt()
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(36.dp)
                                        .clip(RoundedCornerShape(999.dp))
                                        .then(
                                            if (isSelected) {
                                                Modifier
                                                    .shadow(
                                                        elevation = 12.dp,
                                                        shape = RoundedCornerShape(999.dp),
                                                        spotColor = currentColor,
                                                        ambientColor = currentColor
                                                    )
                                                    .background(Color.White)
                                            } else {
                                                Modifier
                                                    .background(Color.White.copy(alpha = 0.03f))
                                                    .border(
                                                        width = 1.dp,
                                                        color = Color.White.copy(alpha = 0.10f),
                                                        shape = RoundedCornerShape(999.dp)
                                                    )
                                            }
                                        )
                                        .clickable {
                                            isAlwaysBlocked = false
                                            limitMinutes = preset
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        fontFamily = InterTightFamily,
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                                        fontSize = 13.sp,
                                        color = if (isSelected) Color(0xFF0B0B10) else Color(0xFFE8E8F0)
                                    )
                                }
                            }
                        }

                        // Slider Moderno com Gradiente Fixo, Pontilhados e Thumb Halo
                        ModernLimitSlider(
                            value = limitMinutes,
                            onValueChange = {
                                isAlwaysBlocked = false
                                limitMinutes = it
                            },
                            min = 5f,
                            max = 240f,
                            step = 5f,
                            enabled = !isAlwaysBlocked,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
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
