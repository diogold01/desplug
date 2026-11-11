package com.desplug.app.ui.screens

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
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
import com.desplug.app.ui.components.DesplugGlowProgressBar
import com.desplug.app.ui.components.ParticleCloudCanvas
import com.desplug.app.ui.components.StaggeredFadeUp
import com.desplug.app.ui.theme.DesplugBlue
import com.desplug.app.ui.theme.DesplugChipBorder
import com.desplug.app.ui.theme.DesplugRed
import com.desplug.app.ui.theme.DesplugTextPrimary
import com.desplug.app.ui.theme.DesplugTextSecondary
import com.desplug.app.ui.theme.InterTightFamily
import com.desplug.app.util.rememberIsReducedMotion

private val SearchIcon = ImageVector.Builder(
    name = "Search",
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

@Composable
fun LimitsScreen(
    installedApps: List<InstalledAppItem>,
    appRules: List<AppBlockRule>,
    usageMap: Map<String, Int>,
    resetHour: Int,
    onOpenResetHourDialog: () -> Unit,
    onAppClick: (InstalledAppItem) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    val rulesMap = remember(appRules) { appRules.associateBy { it.packageName } }

    val filteredApps = remember(installedApps, searchQuery, appRules, usageMap) {
        installedApps
            .filter {
                searchQuery.isBlank() ||
                    it.label.contains(searchQuery, ignoreCase = true) ||
                    it.packageName.contains(searchQuery, ignoreCase = true)
            }
            .sortedWith(
                compareByDescending<InstalledAppItem> { rulesMap.containsKey(it.packageName) }
                    .thenByDescending { usageMap.getOrDefault(it.packageName, 0) }
                    .thenBy { it.label.lowercase() }
            )
    }

    Box(modifier = modifier.fillMaxSize()) {
        // Nuvem de partículas sutil no topo/fundo da tela
        Box(
            modifier = Modifier.fillMaxWidth().height(300.dp),
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
            // 1. Linha de Título: "Limites" (46sp) + Chip "Zera às XX:00"
            item {
                StaggeredFadeUp(delayMillis = 0) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Limites",
                            fontFamily = InterTightFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 46.sp,
                            lineHeight = 46.sp,
                            letterSpacing = (-0.04).em,
                            color = DesplugTextPrimary
                        )

                        DesplugChip(
                            text = "Zera às %02d:00".format(resetHour),
                            onClick = onOpenResetHourDialog
                        )
                    }
                }
            }

            // 2. Busca de App em Formato Pílula (altura 50dp, raio 25dp, borda #232A3B)
            item {
                StaggeredFadeUp(delayMillis = 80) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp, bottom = 4.dp)
                            .height(50.dp)
                            .clip(RoundedCornerShape(25.dp))
                            .border(1.dp, DesplugChipBorder, RoundedCornerShape(25.dp))
                            .padding(horizontal = 18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = SearchIcon,
                            contentDescription = null,
                            tint = DesplugTextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                        Box(modifier = Modifier.weight(1f)) {
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = "Buscar app",
                                    fontFamily = InterTightFamily,
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 15.sp,
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
                                    fontSize = 15.sp,
                                    color = DesplugTextPrimary
                                ),
                                cursorBrush = SolidColor(DesplugBlue),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            // 3. Lista de Cards dos Aplicativos
            itemsIndexed(items = filteredApps, key = { _, app -> app.packageName }) { index, appItem ->
                val rule = rulesMap[appItem.packageName]
                val usedMinutes = usageMap.getOrDefault(appItem.packageName, 0)
                val isBlocked = rule?.isAlwaysBlocked == true ||
                    (rule != null && rule.dailyLimitMinutes > 0 && usedMinutes >= rule.dailyLimitMinutes)

                StaggeredFadeUp(delayMillis = (160 + (index.coerceAtMost(6) * 60))) {
                    LimitAppCard(
                        appItem = appItem,
                        rule = rule,
                        usedMinutes = usedMinutes,
                        isBlocked = isBlocked,
                        onClick = { onAppClick(appItem) }
                    )
                }
            }
        }
    }
}

@Composable
private fun LimitAppCard(
    appItem: InstalledAppItem,
    rule: AppBlockRule?,
    usedMinutes: Int,
    isBlocked: Boolean,
    onClick: () -> Unit
) {
    val cardType = if (isBlocked) DesplugCardType.ALERT else DesplugCardType.DEFAULT

    DesplugCard(
        type = cardType,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp)
        ) {
            // Linha Superior: Ícone do App + Nome + Status / Chip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    AppIconView(
                        packageName = appItem.packageName,
                        appName = appItem.label,
                        size = 36.dp
                    )
                    Column {
                        Text(
                            text = appItem.label,
                            fontFamily = InterTightFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 15.sp,
                            maxLines = 1,
                            color = DesplugTextPrimary
                        )
                        if (rule == null) {
                            Text(
                                text = "Sem limite · $usedMinutes min hoje",
                                fontFamily = InterTightFamily,
                                fontWeight = FontWeight.Normal,
                                fontSize = 13.sp,
                                color = DesplugTextSecondary,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                }

                // Lado Direito
                if (rule != null) {
                    if (isBlocked) {
                        BlinkingBlockedBadge()
                    } else {
                        DesplugChip(
                            text = "Editar",
                            onClick = onClick
                        )
                    }
                } else {
                    DesplugChip(
                        text = "+ Limitar",
                        isSelected = true,
                        onClick = onClick
                    )
                }
            }

            // Se o app tiver regra, exibe os números gigantes e a barra de progresso com glow
            if (rule != null) {
                val limitMinutes = rule.dailyLimitMinutes
                val progress = if (limitMinutes > 0) {
                    (usedMinutes.toFloat() / limitMinutes.toFloat()).coerceIn(0f, 1f)
                } else {
                    1f
                }

                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Text(
                        text = usedMinutes.toString(),
                        fontFamily = InterTightFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 46.sp,
                        lineHeight = 46.sp,
                        letterSpacing = (-0.05).em,
                        color = if (isBlocked) DesplugRed else DesplugTextPrimary
                    )
                    Text(
                        text = if (rule.isAlwaysBlocked) "/ Bloqueado sempre" else "/ $limitMinutes min",
                        fontFamily = InterTightFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 14.sp,
                        color = DesplugTextSecondary,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                DesplugGlowProgressBar(
                    progressFraction = progress,
                    isAlert = isBlocked
                )
            }
        }
    }
}

@Composable
private fun BlinkingBlockedBadge() {
    val isReducedMotion = rememberIsReducedMotion()
    val transition = rememberInfiniteTransition(label = "blockedBlink")

    val alpha by if (!isReducedMotion) {
        transition.animateFloat(
            initialValue = 1f,
            targetValue = 0.5f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 800),
                repeatMode = RepeatMode.Reverse
            ),
            label = "blinkAlpha"
        )
    } else {
        remember { androidx.compose.runtime.mutableFloatStateOf(1f) }
    }

    Text(
        text = "Bloqueado",
        fontFamily = InterTightFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
        color = DesplugRed.copy(alpha = alpha)
    )
}
