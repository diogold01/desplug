package com.desplug.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.desplug.app.InstalledAppItem
import com.desplug.app.ui.components.AppIconView
import com.desplug.app.ui.components.DesplugChip
import com.desplug.app.ui.components.DesplugPrimaryButton
import com.desplug.app.ui.components.ParticleCloudCanvas
import com.desplug.app.ui.theme.DesplugBackground
import com.desplug.app.ui.theme.DesplugBlue
import com.desplug.app.ui.theme.DesplugChipBorder
import com.desplug.app.ui.theme.DesplugSheetBg
import com.desplug.app.ui.theme.DesplugSheetBorder
import com.desplug.app.ui.theme.DesplugSheetHandle
import com.desplug.app.ui.theme.DesplugTextPrimary
import com.desplug.app.ui.theme.DesplugTextSecondary
import com.desplug.app.ui.theme.InterTightFamily
import com.desplug.app.util.rememberIsReducedMotion

private val CheckMarkIcon = ImageVector.Builder(
    name = "CheckMark",
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f
).apply {
    path(
        stroke = SolidColor(Color.White),
        strokeLineWidth = 3.2f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round
    ) {
        moveTo(5f, 12f)
        lineTo(10f, 17f)
        lineTo(19f, 7f)
    }
}.build()

private val SearchLensIcon = ImageVector.Builder(
    name = "SearchLens",
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f
).apply {
    path(
        stroke = SolidColor(DesplugTextSecondary),
        strokeLineWidth = 2f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round
    ) {
        moveTo(11f, 18f)
        curveTo(14.87f, 18f, 18f, 14.87f, 18f, 11f)
        curveTo(18f, 7.13f, 14.87f, 4f, 11f, 4f)
        curveTo(7.13f, 4f, 4f, 7.13f, 4f, 11f)
        curveTo(4f, 14.87f, 7.13f, 18f, 11f, 18f)
        close()
        moveTo(20f, 20f)
        lineTo(16f, 16f)
    }
}.build()

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun QuickBlockSheet(
    initialDurationMinutes: Int,
    initialSelectedPackages: Set<String>,
    installedApps: List<InstalledAppItem>,
    onStartQuickBlock: (durationMinutes: Int, targetPackages: Set<String>) -> Unit,
    onDismiss: () -> Unit
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
                    durationMillis = 1000,
                    easing = CubicBezierEasing(0.2f, 0.9f, 0.2f, 1.0f)
                )
            )
        }
    }

    var durationMinutes by remember { mutableIntStateOf(initialDurationMinutes) }
    var selectedPackages by remember { mutableStateOf(initialSelectedPackages) }
    var searchQuery by remember { mutableStateOf("") }

    val sortedAndFilteredApps = remember(installedApps, selectedPackages, searchQuery) {
        val base = if (searchQuery.isBlank()) {
            installedApps
        } else {
            installedApps.filter {
                it.label.contains(searchQuery, ignoreCase = true) ||
                    it.packageName.contains(searchQuery, ignoreCase = true)
            }
        }
        base.sortedWith(
            compareByDescending<InstalledAppItem> { selectedPackages.contains(it.packageName) }
                .thenBy { it.label.lowercase() }
        )
    }

    val durationButtonSuffix = when (durationMinutes) {
        15 -> "15 min"
        25 -> "25 min"
        60 -> "1 h"
        120 -> "2 h"
        0 -> "Até desligar"
        else -> "$durationMinutes min"
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DesplugBackground.copy(alpha = 0.92f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onDismiss() }
    ) {
        // Fundo com nuvem de partículas e marca d'água Desplug (igual ao QuickBlock.dc.html)
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            ParticleCloudCanvas()
        }

        Text(
            text = "Desplug",
            fontFamily = InterTightFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 46.sp,
            letterSpacing = (-0.04).em,
            color = DesplugTextPrimary.copy(alpha = 0.3f),
            modifier = Modifier
                .statusBarsPadding()
                .padding(top = 16.dp, start = 24.dp)
        )

        // Sheet com raio 36dp no topo
        val sheetShape = RoundedCornerShape(topStart = 36.dp, topEnd = 36.dp)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(top = 56.dp)
                .graphicsLayer {
                    translationY = sheetOffsetY.value
                }
                .clip(sheetShape)
                .background(DesplugSheetBg)
                .border(1.dp, DesplugSheetBorder, sheetShape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { /* Consome clique para não fechar ao tocar dentro do Sheet */ }
                .navigationBarsPadding()
                .padding(start = 24.dp, end = 24.dp, top = 12.dp, bottom = 16.dp)
        ) {
            // Alça superior (40x4dp, #2B3347)
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 22.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(DesplugSheetHandle)
            )

            Text(
                text = "Quick Block",
                fontFamily = InterTightFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 36.sp,
                lineHeight = 38.sp,
                letterSpacing = (-0.04).em,
                color = DesplugTextPrimary
            )

            Text(
                text = "Bloqueie distrações agora.",
                fontFamily = InterTightFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 15.sp,
                color = DesplugTextSecondary,
                modifier = Modifier.padding(top = 8.dp)
            )

            // Seção DURAÇÃO
            Text(
                text = "DURAÇÃO",
                fontFamily = InterTightFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                letterSpacing = 0.08.em,
                color = DesplugTextSecondary,
                modifier = Modifier.padding(top = 24.dp, bottom = 10.dp)
            )

            val durationOptions = listOf(
                15 to "15 min",
                25 to "25 min · Pomodoro",
                60 to "1 h",
                120 to "2 h",
                0 to "Até desligar"
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                durationOptions.forEach { (mins, label) ->
                    DesplugChip(
                        text = label,
                        isSelected = durationMinutes == mins,
                        onClick = { durationMinutes = mins }
                    )
                }
            }

            // Seção APPS A BLOQUEAR
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "APPS A BLOQUEAR",
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

            // Busca rápida de app em formato pílula compacta
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp)
                    .height(42.dp)
                    .clip(RoundedCornerShape(21.dp))
                    .border(1.dp, DesplugChipBorder, RoundedCornerShape(21.dp))
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = SearchLensIcon,
                    contentDescription = null,
                    tint = DesplugTextSecondary,
                    modifier = Modifier.size(16.dp)
                )
                Box(modifier = Modifier.weight(1f)) {
                    if (searchQuery.isEmpty()) {
                        Text(
                            text = "Buscar aplicativo...",
                            fontFamily = InterTightFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 14.sp,
                            color = DesplugTextSecondary
                        )
                    }
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        singleLine = true,
                        textStyle = TextStyle(
                            fontFamily = InterTightFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 14.sp,
                            color = DesplugTextPrimary
                        ),
                        cursorBrush = SolidColor(DesplugBlue),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Lista de Apps (altura de linha 58dp como no mockup)
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                items(sortedAndFilteredApps, key = { it.packageName }) { app ->
                    val isChecked = selectedPackages.contains(app.packageName)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(58.dp)
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
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            AppIconView(
                                packageName = app.packageName,
                                appName = app.label,
                                size = 36.dp
                            )
                            Text(
                                text = app.label,
                                fontFamily = InterTightFamily,
                                fontWeight = FontWeight.Normal,
                                fontSize = 16.sp,
                                maxLines = 1,
                                color = DesplugTextPrimary
                            )
                        }

                        QuickBlockCheckCircle(isChecked = isChecked)
                    }
                }
            }

            // Rodapé do Sheet
            Text(
                text = "Sair antes exige PIN + 5 min de espera",
                fontFamily = InterTightFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 13.sp,
                color = DesplugTextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, bottom = 14.dp)
            )

            DesplugPrimaryButton(
                text = "Iniciar bloqueio · $durationButtonSuffix",
                enabled = selectedPackages.isNotEmpty(),
                onClick = {
                    onStartQuickBlock(durationMinutes, selectedPackages)
                }
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
                    .height(48.dp)
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

@Composable
fun QuickBlockCheckCircle(
    isChecked: Boolean,
    modifier: Modifier = Modifier
) {
    if (isChecked) {
        Box(
            modifier = modifier.size(26.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(26.dp)) {
                drawContext.canvas.nativeCanvas.apply {
                    val paint = android.graphics.Paint().apply {
                        color = DesplugBlue.toArgb()
                        isAntiAlias = true
                        setShadowLayer(
                            14.dp.toPx(),
                            0f,
                            0f,
                            android.graphics.Color.argb(153, 76, 125, 255) // rgba(76, 125, 255, 0.6)
                        )
                    }
                    drawCircle(
                        size.width / 2f,
                        size.height / 2f,
                        13.dp.toPx(),
                        paint
                    )
                }
            }
            Icon(
                imageVector = CheckMarkIcon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(14.dp)
            )
        }
    } else {
        Box(
            modifier = modifier
                .size(26.dp)
                .clip(CircleShape)
                .border(1.5.dp, DesplugSheetHandle, CircleShape)
        )
    }
}
