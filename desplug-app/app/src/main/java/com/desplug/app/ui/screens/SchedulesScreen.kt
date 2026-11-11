package com.desplug.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.desplug.app.InstalledAppItem
import com.desplug.app.data.model.ScheduleRule
import com.desplug.app.ui.components.AppIconView
import com.desplug.app.ui.components.DesplugCard
import com.desplug.app.ui.components.DesplugCardType
import com.desplug.app.ui.components.DesplugChip
import com.desplug.app.ui.components.DesplugPrimaryButton
import com.desplug.app.ui.components.DesplugStatusPulse
import com.desplug.app.ui.components.ParticleCloudCanvas
import com.desplug.app.ui.components.StaggeredFadeUp
import com.desplug.app.ui.theme.DesplugBlue
import com.desplug.app.ui.theme.DesplugTextBlue
import com.desplug.app.ui.theme.DesplugTextPrimary
import com.desplug.app.ui.theme.DesplugTextSecondary
import com.desplug.app.ui.theme.InterTightFamily
import java.util.Calendar

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SchedulesScreen(
    rules: List<ScheduleRule>,
    installedApps: List<InstalledAppItem>,
    onToggleRule: (ScheduleRule) -> Unit,
    onEditRule: (ScheduleRule) -> Unit,
    onAddNewRule: () -> Unit,
    modifier: Modifier = Modifier
) {
    val nowCalendar = Calendar.getInstance()
    val appsMap = installedApps.associateBy { it.packageName }

    Box(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            contentAlignment = Alignment.Center
        ) {
            ParticleCloudCanvas()
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 140.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 1. Header: "Horários" (46sp) + Chip "+ Nova Regra"
            item {
                StaggeredFadeUp(delayMillis = 0) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Horários",
                            fontFamily = InterTightFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 46.sp,
                            lineHeight = 46.sp,
                            letterSpacing = (-0.04).em,
                            color = DesplugTextPrimary
                        )

                        DesplugChip(
                            text = "+ Nova Regra",
                            isSelected = true,
                            onClick = onAddNewRule
                        )
                    }
                }
            }

            if (rules.isEmpty()) {
                item {
                    StaggeredFadeUp(
                        delayMillis = 100,
                        modifier = Modifier.padding(top = 12.dp)
                    ) {
                        DesplugCard(
                            type = DesplugCardType.DEFAULT,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(22.dp)
                            ) {
                                Text(
                                    text = "Rotina protegida",
                                    fontFamily = InterTightFamily,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 28.sp,
                                    letterSpacing = (-0.03).em,
                                    color = DesplugTextPrimary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Programe janelas automáticas como Foco Noturno (22:00 – 06:00) ou Horário de Estudo/Trabalho para bloquear apps nos dias escolhidos.",
                                    fontFamily = InterTightFamily,
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 14.sp,
                                    lineHeight = 20.sp,
                                    color = DesplugTextSecondary
                                )
                                Spacer(modifier = Modifier.height(20.dp))
                                DesplugPrimaryButton(
                                    text = "Criar Regra de Horário",
                                    onClick = onAddNewRule
                                )
                            }
                        }
                    }
                }
            } else {
                itemsIndexed(rules, key = { _, r -> r.id }) { index, rule ->
                    val isCurrentlyActive = rule.isEnabled && rule.isCurrentlyActive(nowCalendar)

                    StaggeredFadeUp(delayMillis = 100 + (index.coerceAtMost(6) * 70)) {
                        DesplugCard(
                            type = if (isCurrentlyActive) DesplugCardType.HERO_GRADIENT else DesplugCardType.DEFAULT,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onEditRule(rule) }
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        if (isCurrentlyActive) {
                                            DesplugStatusPulse(color = Color.White)
                                        }
                                        Text(
                                            text = rule.name,
                                            fontFamily = InterTightFamily,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 16.sp,
                                            maxLines = 1,
                                            color = Color.White
                                        )
                                    }

                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        DesplugChip(
                                            text = when {
                                                isCurrentlyActive -> "Ativo agora"
                                                rule.isEnabled -> "Ligado"
                                                else -> "Desligado"
                                            },
                                            isSelected = rule.isEnabled,
                                            onClick = { onToggleRule(rule) }
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                Text(
                                    text = "%02d:%02d – %02d:%02d".format(
                                        rule.startHour,
                                        rule.startMinute,
                                        rule.endHour,
                                        rule.endMinute
                                    ),
                                    fontFamily = InterTightFamily,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 38.sp,
                                    lineHeight = 40.sp,
                                    letterSpacing = (-0.05).em,
                                    color = Color.White
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = "${rule.formattedDaysOfWeek()} · ${rule.targetPackages.size} app(s)",
                                    fontFamily = InterTightFamily,
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 13.sp,
                                    color = if (isCurrentlyActive) DesplugTextBlue else DesplugTextSecondary
                                )

                                if (rule.targetPackages.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(14.dp))
                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        rule.targetPackages.take(7).forEach { pkg ->
                                            val label = appsMap[pkg]?.label ?: pkg
                                            AppIconView(
                                                packageName = pkg,
                                                appName = label,
                                                size = 30.dp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
