package com.desplug.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.desplug.app.data.model.AppBlockRule
import com.desplug.app.data.model.QuickBlockState
import com.desplug.app.data.model.ScheduleRule
import com.desplug.app.ui.components.DesplugCard
import com.desplug.app.ui.components.DesplugCardType
import com.desplug.app.ui.components.DesplugChip
import com.desplug.app.ui.components.DesplugPrimaryButton
import com.desplug.app.ui.components.DesplugStatusPulse
import com.desplug.app.ui.components.HeroUsageRing
import com.desplug.app.ui.components.ParticleCloudCanvas
import com.desplug.app.ui.components.StaggeredFadeUp
import com.desplug.app.ui.theme.DesplugBlue
import com.desplug.app.ui.theme.DesplugRed
import com.desplug.app.ui.theme.DesplugTextBlue
import com.desplug.app.ui.theme.DesplugTextPrimary
import com.desplug.app.ui.theme.DesplugTextSecondary
import com.desplug.app.ui.theme.InterTightFamily
import java.util.Calendar

private val PinLockIcon = ImageVector.Builder(
    name = "PinLock",
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f
).apply {
    path(
        stroke = SolidColor(DesplugBlue),
        strokeLineWidth = 2.2f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round
    ) {
        // rect x="5" y="11" width="14" height="10" rx="2"
        moveTo(7f, 11f)
        horizontalLineTo(17f)
        curveTo(18.1f, 11f, 19f, 11.9f, 19f, 13f)
        verticalLineTo(19f)
        curveTo(19f, 20.1f, 18.1f, 21f, 17f, 21f)
        horizontalLineTo(7f)
        curveTo(5.9f, 21f, 5f, 20.1f, 5f, 19f)
        verticalLineTo(13f)
        curveTo(5f, 11.9f, 5.9f, 11f, 7f, 11f)
        close()
        // path d="M8 11V8a4 4 0 0 1 8 0v3"
        moveTo(8f, 11f)
        verticalLineTo(8f)
        curveTo(8f, 5.79f, 9.79f, 4f, 12f, 4f)
        curveTo(14.21f, 4f, 16f, 5.79f, 16f, 8f)
        verticalLineTo(11f)
    }
}.build()

@Composable
fun HomeScreen(
    hasPinConfigured: Boolean,
    allPermissionsGranted: Boolean,
    isBlockingEnabled: Boolean,
    totalAttemptsToday: Int,
    appRules: List<AppBlockRule>,
    usageMap: Map<String, Int>,
    scheduleRules: List<ScheduleRule>,
    quickBlockState: QuickBlockState,
    selectedQuickDurationMinutes: Int,
    onSelectQuickDuration: (Int) -> Unit,
    onOpenQuickBlockSheet: () -> Unit,
    onOpenPinSetup: () -> Unit,
    onTogglePrimarySchedule: (ScheduleRule?) -> Unit,
    onNavigateToSchedules: () -> Unit,
    onNavigateToFocus: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Cálculo do tempo usado e limite para o Anel Hero da Tela Início
    val limitedRules = appRules.filter { it.dailyLimitMinutes > 0 }
    val totalUsedMinutes = if (limitedRules.isNotEmpty()) {
        limitedRules.sumOf { usageMap[it.packageName] ?: 0 }
    } else {
        usageMap.values.sum()
    }
    val totalLimitMinutes = if (limitedRules.isNotEmpty()) {
        limitedRules.sumOf { it.dailyLimitMinutes }
    } else {
        180 // Referência padrão de 3h quando nenhum limite individual foi definido
    }

    val usedTimeFormatted = formatHeroDuration(totalUsedMinutes)
    val progressFraction = if (totalLimitMinutes > 0) {
        (totalUsedMinutes.toFloat() / totalLimitMinutes.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }
    val percentageInt = if (totalLimitMinutes > 0) {
        ((totalUsedMinutes * 100) / totalLimitMinutes).coerceAtMost(999)
    } else {
        0
    }
    val ringSubtitle = "usados hoje · $percentageInt% do limite"

    // Regra de horário em destaque (ex: Foco Noturno)
    val primarySchedule = scheduleRules.firstOrNull()
    val nowCal = Calendar.getInstance()
    val isScheduleActiveNow = primarySchedule != null &&
        primarySchedule.isEnabled &&
        primarySchedule.isCurrentlyActive(nowCal)

    val protectionActive = allPermissionsGranted && isBlockingEnabled

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 24.dp, end = 24.dp, top = 56.dp, bottom = 104.dp)
    ) {
        // 1. Linha de Topo: "Desplug" + Chip "PIN ativo"
        StaggeredFadeUp(delayMillis = 0) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Desplug",
                    fontFamily = InterTightFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 19.sp,
                    letterSpacing = (-0.03).em,
                    color = DesplugTextPrimary
                )

                DesplugChip(
                    text = if (hasPinConfigured) "PIN ativo" else "Definir PIN",
                    onClick = onOpenPinSetup,
                    leadingIcon = {
                        Icon(
                            imageVector = PinLockIcon,
                            contentDescription = null,
                            tint = DesplugBlue,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                )
            }
        }

        // 2. Status "Proteção ativa" + Título "Foco protegido."
        StaggeredFadeUp(
            delayMillis = 100,
            modifier = Modifier.padding(top = 26.dp)
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .padding(bottom = 12.dp)
                        .clickable {
                            if (!protectionActive) onNavigateToFocus()
                        }
                ) {
                    DesplugStatusPulse(
                        color = if (protectionActive) DesplugBlue else DesplugRed
                    )
                    Text(
                        text = if (protectionActive) "Proteção ativa" else "Permissões pendentes · Ajustar",
                        fontFamily = InterTightFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        color = if (protectionActive) DesplugBlue else DesplugRed
                    )
                }

                Text(
                    text = if (quickBlockState.isActive) "Quick Block\nativo." else "Foco\nprotegido.",
                    fontFamily = InterTightFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 50.sp,
                    lineHeight = 50.sp,
                    letterSpacing = (-0.04).em,
                    color = DesplugTextPrimary
                )
            }
        }

        // 3. Anel Hero (232dp) + Nuvem de Partículas (380dp)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(236.dp)
                .padding(top = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            ParticleCloudCanvas()
            HeroUsageRing(
                usedTimeText = usedTimeFormatted,
                subtitleText = ringSubtitle,
                progressFraction = progressFraction
            )
        }

        // 4. Grid 2 Colunas: Tentativas Bloqueadas (Gradiente) + Foco Noturno (Card)
        StaggeredFadeUp(delayMillis = 250) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Card de Destaque Azul (Tentativas Bloqueadas)
                DesplugCard(
                    type = DesplugCardType.HERO_GRADIENT,
                    modifier = Modifier
                        .weight(1f)
                        .height(126.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 18.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = totalAttemptsToday.toString(),
                            fontFamily = InterTightFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 52.sp,
                            lineHeight = 52.sp,
                            letterSpacing = (-0.05).em,
                            color = Color.White
                        )
                        Text(
                            text = "tentativas bloqueadas",
                            fontFamily = InterTightFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 13.sp,
                            lineHeight = 16.sp,
                            color = DesplugTextBlue
                        )
                    }
                }

                // Card Foco Noturno / Regra de Horário Principal
                DesplugCard(
                    type = DesplugCardType.DEFAULT,
                    modifier = Modifier
                        .weight(1f)
                        .height(126.dp)
                        .clickable { onNavigateToSchedules() }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 18.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = primarySchedule?.name ?: "Foco Noturno",
                                fontFamily = InterTightFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp,
                                maxLines = 1,
                                color = DesplugTextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = primarySchedule?.formattedTimeWindow()?.replace(" às ", " – ")
                                    ?: "22:00 – 06:00",
                                fontFamily = InterTightFamily,
                                fontWeight = FontWeight.Normal,
                                fontSize = 13.sp,
                                color = DesplugTextSecondary
                            )
                        }

                        val isScheduleEnabled = primarySchedule?.isEnabled == true
                        val statusText = when {
                            isScheduleActiveNow -> "Ativo"
                            isScheduleEnabled -> "Ligado"
                            else -> "Desligado"
                        }

                        DesplugChip(
                            text = statusText,
                            isSelected = isScheduleEnabled,
                            onClick = { onTogglePrimarySchedule(primarySchedule) }
                        )
                    }
                }
            }
        }

        // 5. Chips de Duração Rápida do Quick Block (15 min, 25 min, 1 h, 2 h)
        StaggeredFadeUp(
            delayMillis = 350,
            modifier = Modifier.padding(top = 14.dp, bottom = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val quickOptions = listOf(
                    15 to "15 min",
                    25 to "25 min",
                    60 to "1 h",
                    120 to "2 h"
                )
                quickOptions.forEach { (minutes, label) ->
                    DesplugChip(
                        text = label,
                        isSelected = selectedQuickDurationMinutes == minutes,
                        onClick = { onSelectQuickDuration(minutes) }
                    )
                }
            }
        }

        // 6. Botão Pílula Branca Principal ("Ativar Quick Block ->")
        StaggeredFadeUp(delayMillis = 450) {
            DesplugPrimaryButton(
                text = if (quickBlockState.isActive) {
                    "Ver Quick Block Ativo"
                } else {
                    "Ativar Quick Block"
                },
                onClick = {
                    if (quickBlockState.isActive) {
                        onNavigateToFocus()
                    } else {
                        onOpenQuickBlockSheet()
                    }
                }
            )
        }
    }
}

private fun formatHeroDuration(totalMinutes: Int): String {
    if (totalMinutes <= 0) return "0m"
    val hours = totalMinutes / 60
    val mins = totalMinutes % 60
    return if (hours > 0) {
        "${hours}h %02d".format(mins)
    } else {
        "${mins}m"
    }
}
