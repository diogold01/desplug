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
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
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
import com.desplug.app.ui.theme.DesplugBackground
import com.desplug.app.ui.theme.DesplugBlue
import com.desplug.app.ui.theme.DesplugChipBorder
import com.desplug.app.ui.theme.DesplugRed
import com.desplug.app.ui.theme.DesplugSheetBg
import com.desplug.app.ui.theme.DesplugSheetBorder
import com.desplug.app.ui.theme.DesplugSheetHandle
import com.desplug.app.ui.theme.DesplugTextPrimary
import com.desplug.app.ui.theme.DesplugTextSecondary
import com.desplug.app.ui.theme.InterTightFamily
import com.desplug.app.util.rememberIsReducedMotion
import java.util.Calendar

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ScheduleRuleSheet(
    initialRule: ScheduleRule,
    isNew: Boolean,
    installedApps: List<InstalledAppItem>,
    onDismiss: () -> Unit,
    onSave: (ScheduleRule) -> Unit,
    onDelete: (String) -> Unit
) {
    BackHandler(onBack = onDismiss)

    val isReducedMotion = rememberIsReducedMotion()
    val density = LocalDensity.current
    val slideStartPx = with(density) { 800.dp.toPx() }
    val sheetOffsetY = remember { Animatable(if (isReducedMotion) 0f else slideStartPx) }

    LaunchedEffect(Unit, isReducedMotion) {
        if (!isReducedMotion) {
            sheetOffsetY.animateTo(
                targetValue = 0f,
                animationSpec = tween(
                    durationMillis = 900,
                    easing = CubicBezierEasing(0.2f, 0.9f, 0.2f, 1.0f)
                )
            )
        }
    }

    var name by remember { mutableStateOf(initialRule.name) }
    var startHour by remember { mutableIntStateOf(initialRule.startHour) }
    var startMinute by remember { mutableIntStateOf(initialRule.startMinute) }
    var endHour by remember { mutableIntStateOf(initialRule.endHour) }
    var endMinute by remember { mutableIntStateOf(initialRule.endMinute) }
    var selectedDays by remember { mutableStateOf(initialRule.daysOfWeek) }
    var selectedPackages by remember { mutableStateOf(initialRule.targetPackages) }
    var appSearch by remember { mutableStateOf("") }

    val daysOrder = listOf(
        Calendar.MONDAY to "S",
        Calendar.TUESDAY to "T",
        Calendar.WEDNESDAY to "Q",
        Calendar.THURSDAY to "Q",
        Calendar.FRIDAY to "S",
        Calendar.SATURDAY to "S",
        Calendar.SUNDAY to "D"
    )

    val sortedApps = remember(installedApps, selectedPackages, appSearch) {
        val filtered = if (appSearch.isBlank()) {
            installedApps
        } else {
            installedApps.filter {
                it.label.contains(appSearch, ignoreCase = true) ||
                    it.packageName.contains(appSearch, ignoreCase = true)
            }
        }
        filtered.sortedWith(
            compareByDescending<InstalledAppItem> { selectedPackages.contains(it.packageName) }
                .thenBy { it.label.lowercase() }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DesplugBackground.copy(alpha = 0.88f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onDismiss() }
    ) {
        val sheetShape = RoundedCornerShape(topStart = 36.dp, topEnd = 36.dp)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(top = 48.dp)
                .graphicsLayer { translationY = sheetOffsetY.value }
                .clip(sheetShape)
                .background(DesplugSheetBg)
                .border(1.dp, DesplugSheetBorder, sheetShape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { /* Consome clique */ }
                .navigationBarsPadding()
                .padding(start = 24.dp, end = 24.dp, top = 12.dp, bottom = 16.dp)
        ) {
            // Alça superior
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 18.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(DesplugSheetHandle)
            )

            Text(
                text = if (isNew) "Nova Regra" else "Editar Horário",
                fontFamily = InterTightFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 32.sp,
                lineHeight = 34.sp,
                letterSpacing = (-0.04).em,
                color = DesplugTextPrimary
            )

            // Campo Nome da Regra
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 14.dp)
                    .height(46.dp)
                    .clip(RoundedCornerShape(23.dp))
                    .border(1.dp, DesplugChipBorder, RoundedCornerShape(23.dp))
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (name.isEmpty()) {
                    Text(
                        text = "Nome da regra (ex: Foco Noturno)",
                        fontFamily = InterTightFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 14.sp,
                        color = DesplugTextSecondary
                    )
                }
                BasicTextField(
                    value = name,
                    onValueChange = { name = it },
                    singleLine = true,
                    textStyle = TextStyle(
                        fontFamily = InterTightFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 15.sp,
                        color = DesplugTextPrimary
                    ),
                    cursorBrush = SolidColor(DesplugBlue),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Atalhos de Janela de Horário
            FlowRow(
                modifier = Modifier.padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DesplugChip(
                    text = "Noturno 22h–06h",
                    isSelected = startHour == 22 && endHour == 6,
                    onClick = {
                        name = "Foco Noturno"
                        startHour = 22; startMinute = 0
                        endHour = 6; endMinute = 0
                    }
                )
                DesplugChip(
                    text = "Manhã 08h–12h",
                    isSelected = startHour == 8 && endHour == 12,
                    onClick = {
                        if (name == "Foco Noturno") name = "Foco da Manhã"
                        startHour = 8; startMinute = 0
                        endHour = 12; endMinute = 0
                    }
                )
                DesplugChip(
                    text = "Tarde 14h–18h",
                    isSelected = startHour == 14 && endHour == 18,
                    onClick = {
                        if (name == "Foco Noturno") name = "Foco da Tarde"
                        startHour = 14; startMinute = 0
                        endHour = 18; endMinute = 0
                    }
                )
            }

            // Seletores de Hora de Início e Término
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TimeStepperCard(
                    label = "INÍCIO",
                    hour = startHour,
                    minute = startMinute,
                    onIncrementHour = { startHour = (startHour + 1) % 24 },
                    onToggleMinute = { startMinute = if (startMinute == 0) 30 else 0 },
                    modifier = Modifier.weight(1f)
                )
                TimeStepperCard(
                    label = "TÉRMINO",
                    hour = endHour,
                    minute = endMinute,
                    onIncrementHour = { endHour = (endHour + 1) % 24 },
                    onToggleMinute = { endMinute = if (endMinute == 0) 30 else 0 },
                    modifier = Modifier.weight(1f)
                )
            }

            // Dias da Semana
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                daysOrder.forEach { (calDay, dayLabel) ->
                    val isSelected = selectedDays.contains(calDay)
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) Color.White else Color.Transparent)
                            .border(
                                1.dp,
                                if (isSelected) Color.White else DesplugChipBorder,
                                CircleShape
                            )
                            .clickable {
                                selectedDays = if (isSelected) {
                                    if (selectedDays.size > 1) selectedDays - calDay else selectedDays
                                } else {
                                    selectedDays + calDay
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = dayLabel,
                            fontFamily = InterTightFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = if (isSelected) Color.Black else DesplugTextPrimary
                        )
                    }
                }
            }

            // Cabeçalho de Apps
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "APPS RESTRITOS",
                    fontFamily = InterTightFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    letterSpacing = 0.08.em,
                    color = DesplugTextSecondary
                )
                Text(
                    text = "${selectedPackages.size} selecionados",
                    fontFamily = InterTightFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = DesplugBlue
                )
            }

            // Busca rápida de app
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp)
                    .height(40.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .border(1.dp, DesplugChipBorder, RoundedCornerShape(20.dp))
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (appSearch.isEmpty()) {
                    Text(
                        text = "Filtrar aplicativos...",
                        fontFamily = InterTightFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 13.sp,
                        color = DesplugTextSecondary
                    )
                }
                BasicTextField(
                    value = appSearch,
                    onValueChange = { appSearch = it },
                    singleLine = true,
                    textStyle = TextStyle(
                        fontFamily = InterTightFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 13.sp,
                        color = DesplugTextPrimary
                    ),
                    cursorBrush = SolidColor(DesplugBlue),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Lista de Apps
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                items(sortedApps, key = { it.packageName }) { app ->
                    val isChecked = selectedPackages.contains(app.packageName)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .clickable {
                                selectedPackages = if (isChecked) {
                                    selectedPackages - app.packageName
                                } else {
                                    selectedPackages + app.packageName
                                }
                            },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            AppIconView(
                                packageName = app.packageName,
                                appName = app.label,
                                size = 32.dp
                            )
                            Text(
                                text = app.label,
                                fontFamily = InterTightFamily,
                                fontWeight = FontWeight.Normal,
                                fontSize = 15.sp,
                                maxLines = 1,
                                color = DesplugTextPrimary
                            )
                        }
                        QuickBlockCheckCircle(isChecked = isChecked)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            DesplugPrimaryButton(
                text = "Salvar Regra de Horário",
                onClick = {
                    val updated = initialRule.copy(
                        name = if (name.isBlank()) "Regra de Horário" else name.trim(),
                        startHour = startHour,
                        startMinute = startMinute,
                        endHour = endHour,
                        endMinute = endMinute,
                        daysOfWeek = selectedDays,
                        targetPackages = selectedPackages
                    )
                    onSave(updated)
                }
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                if (!isNew) {
                    Box(
                        modifier = Modifier
                            .height(42.dp)
                            .clickable { onDelete(initialRule.id) }
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Excluir Regra",
                            fontFamily = InterTightFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp,
                            color = DesplugRed
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .height(42.dp)
                        .clickable { onDismiss() }
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Cancelar",
                        fontFamily = InterTightFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        color = DesplugTextSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun TimeStepperCard(
    label: String,
    hour: Int,
    minute: Int,
    onIncrementHour: () -> Unit,
    onToggleMinute: () -> Unit,
    modifier: Modifier = Modifier
) {
    DesplugCard(
        type = DesplugCardType.DEFAULT,
        cornerRadius = 22.dp,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                fontFamily = InterTightFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp,
                letterSpacing = 0.08.em,
                color = DesplugTextSecondary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "%02d".format(hour),
                    fontFamily = InterTightFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 30.sp,
                    letterSpacing = (-0.04).em,
                    color = DesplugTextPrimary,
                    modifier = Modifier.clickable { onIncrementHour() }
                )
                Text(
                    text = ":",
                    fontFamily = InterTightFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 26.sp,
                    color = DesplugTextSecondary
                )
                Text(
                    text = "%02d".format(minute),
                    fontFamily = InterTightFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 30.sp,
                    letterSpacing = (-0.04).em,
                    color = DesplugBlue,
                    modifier = Modifier.clickable { onToggleMinute() }
                )
            }
            Text(
                text = "Toque para ajustar",
                fontFamily = InterTightFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 11.sp,
                color = DesplugTextSecondary,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}
