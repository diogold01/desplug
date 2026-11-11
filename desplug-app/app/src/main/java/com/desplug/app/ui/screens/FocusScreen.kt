package com.desplug.app.ui.screens

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.desplug.app.InstalledAppItem
import com.desplug.app.data.model.QuickBlockState
import com.desplug.app.ui.components.AppIconView
import com.desplug.app.ui.components.DesplugCard
import com.desplug.app.ui.components.DesplugCardType
import com.desplug.app.ui.components.DesplugChip
import com.desplug.app.ui.components.DesplugGlowProgressBar
import com.desplug.app.ui.components.DesplugPrimaryButton
import com.desplug.app.ui.components.DesplugStatusPulse
import com.desplug.app.ui.components.ParticleCloudCanvas
import com.desplug.app.ui.components.StaggeredFadeUp
import com.desplug.app.ui.theme.DesplugBlue
import com.desplug.app.ui.theme.DesplugCardBorder
import com.desplug.app.ui.theme.DesplugRed
import com.desplug.app.ui.theme.DesplugTextBlue
import com.desplug.app.ui.theme.DesplugTextPrimary
import com.desplug.app.ui.theme.DesplugTextSecondary
import com.desplug.app.ui.theme.InterTightFamily
import com.desplug.app.util.PermissionHelper

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FocusScreen(
    state: QuickBlockState,
    currentTimeMillis: Long,
    installedApps: List<InstalledAppItem>,
    hasPin: Boolean,
    isAntiTamperEnabled: Boolean,
    hasAnyActiveBlock: Boolean,
    hasAccessibility: Boolean,
    hasUsageStats: Boolean,
    hasOverlay: Boolean,
    hasBatteryExemption: Boolean,
    onToggleAntiTamper: (Boolean) -> Unit,
    onStartQuickBlockClick: () -> Unit,
    onRequestUnlockClick: () -> Unit,
    onCancelUnlockClick: () -> Unit,
    onOpenPinSetup: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Header: "Foco" (46sp) + Chip de PIN
            item {
                StaggeredFadeUp(delayMillis = 0) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Foco",
                            fontFamily = InterTightFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 46.sp,
                            lineHeight = 46.sp,
                            letterSpacing = (-0.04).em,
                            color = DesplugTextPrimary
                        )

                        DesplugChip(
                            text = if (hasPin) "Alterar PIN" else "Definir PIN",
                            onClick = onOpenPinSetup
                        )
                    }
                }
            }

            // 2. Card Principal do Quick Block (Ativo / Espera de 5 min / Desativado)
            item {
                StaggeredFadeUp(delayMillis = 80) {
                    if (!state.isActive) {
                        DesplugCard(
                            type = DesplugCardType.DEFAULT,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(22.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    DesplugStatusPulse(color = DesplugBlue)
                                    Text(
                                        text = "Quick Block pronto",
                                        fontFamily = InterTightFamily,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 13.sp,
                                        color = DesplugBlue
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                    text = "Foco imediato.",
                                    fontFamily = InterTightFamily,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 34.sp,
                                    lineHeight = 36.sp,
                                    letterSpacing = (-0.04).em,
                                    color = DesplugTextPrimary
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "Bloqueie os aplicativos que tiram sua atenção agora. Para desativar antes do tempo, será exigido seu PIN + 5 minutos de espera obrigatória.",
                                    fontFamily = InterTightFamily,
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 14.sp,
                                    lineHeight = 20.sp,
                                    color = DesplugTextSecondary
                                )

                                Spacer(modifier = Modifier.height(20.dp))

                                DesplugPrimaryButton(
                                    text = "Configurar Quick Block",
                                    onClick = onStartQuickBlockClick
                                )
                            }
                        }
                    } else if (state.isCoolingDown(currentTimeMillis)) {
                        // Estado de Espera Obrigatória de 5 minutos (Card de Alerta #12060A)
                        val remaining = state.remainingCooldownSeconds(currentTimeMillis)
                        val progress = 1f - (remaining.toFloat() / state.unlockCooldownSeconds.toFloat().coerceAtLeast(1f))

                        DesplugCard(
                            type = DesplugCardType.ALERT,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(22.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    DesplugStatusPulse(color = DesplugRed)
                                    Text(
                                        text = "Espera obrigatória em andamento",
                                        fontFamily = InterTightFamily,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp,
                                        color = DesplugRed
                                    )
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                Text(
                                    text = "%02d:%02d".format(remaining / 60, remaining % 60),
                                    fontFamily = InterTightFamily,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 58.sp,
                                    lineHeight = 58.sp,
                                    letterSpacing = (-0.05).em,
                                    color = DesplugTextPrimary
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "O bloqueio continuará ativo até o fim da contagem. Se mudar de ideia, cancele abaixo para manter seu foco.",
                                    fontFamily = InterTightFamily,
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 13.sp,
                                    lineHeight = 19.sp,
                                    color = DesplugTextSecondary
                                )

                                Spacer(modifier = Modifier.height(18.dp))

                                DesplugGlowProgressBar(
                                    progressFraction = progress.coerceIn(0f, 1f),
                                    isAlert = true
                                )

                                Spacer(modifier = Modifier.height(20.dp))

                                DesplugPrimaryButton(
                                    text = "Cancelar e Manter Bloqueio",
                                    showArrow = false,
                                    onClick = onCancelUnlockClick
                                )
                            }
                        }
                    } else {
                        // Estado Quick Block Ativo Normal (Card Gradiente Azul)
                        val remDur = state.remainingDurationSeconds(currentTimeMillis)

                        DesplugCard(
                            type = DesplugCardType.HERO_GRADIENT,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(22.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    DesplugStatusPulse(color = Color.White)
                                    Text(
                                        text = "Quick Block Ativo",
                                        fontFamily = InterTightFamily,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp,
                                        color = DesplugTextBlue
                                    )
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                if (state.durationMinutes > 0) {
                                    Text(
                                        text = "%02d:%02d".format(remDur / 60, remDur % 60),
                                        fontFamily = InterTightFamily,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 58.sp,
                                        lineHeight = 58.sp,
                                        letterSpacing = (-0.05).em,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "restantes · ${state.targetPackages.size} app(s) bloqueado(s)",
                                        fontFamily = InterTightFamily,
                                        fontWeight = FontWeight.Normal,
                                        fontSize = 13.sp,
                                        color = DesplugTextBlue,
                                        modifier = Modifier.padding(top = 6.dp)
                                    )
                                } else {
                                    Text(
                                        text = "Foco contínuo",
                                        fontFamily = InterTightFamily,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 42.sp,
                                        lineHeight = 44.sp,
                                        letterSpacing = (-0.04).em,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Bloqueando ${state.targetPackages.size} aplicativo(s) até você desligar",
                                        fontFamily = InterTightFamily,
                                        fontWeight = FontWeight.Normal,
                                        fontSize = 13.sp,
                                        color = DesplugTextBlue,
                                        modifier = Modifier.padding(top = 6.dp)
                                    )
                                }

                                if (state.targetPackages.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(16.dp))
                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        state.targetPackages.take(8).forEach { pkg ->
                                            val label = appsMap[pkg]?.label ?: pkg
                                            AppIconView(
                                                packageName = pkg,
                                                appName = label,
                                                size = 32.dp
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(22.dp))

                                DesplugPrimaryButton(
                                    text = "Solicitar Desbloqueio (PIN + 5 min)",
                                    showArrow = false,
                                    onClick = onRequestUnlockClick
                                )
                            }
                        }
                    }
                }
            }

            // 3. Seção Proteção Anti-Burla
            item {
                StaggeredFadeUp(delayMillis = 160) {
                    val isProtectionReady = isAntiTamperEnabled && hasAccessibility
                    val isLockedByActiveBlock = isAntiTamperEnabled && hasAnyActiveBlock

                    DesplugCard(
                        type = DesplugCardType.DEFAULT,
                        modifier = Modifier.fillMaxWidth()
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
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    DesplugStatusPulse(
                                        color = if (isProtectionReady) DesplugBlue else DesplugRed
                                    )
                                    Text(
                                        text = "Impedir Desinstalação e Forçar Parada",
                                        fontFamily = InterTightFamily,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 15.sp,
                                        color = DesplugTextPrimary
                                    )
                                }

                                Switch(
                                    checked = isAntiTamperEnabled,
                                    enabled = !isLockedByActiveBlock,
                                    onCheckedChange = onToggleAntiTamper,
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = DesplugBlue,
                                        uncheckedThumbColor = DesplugTextSecondary,
                                        uncheckedTrackColor = DesplugCardBorder,
                                        disabledCheckedThumbColor = Color.White.copy(alpha = 0.8f),
                                        disabledCheckedTrackColor = DesplugBlue.copy(alpha = 0.6f)
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = when {
                                    isLockedByActiveBlock -> "Blindagem ativa agora! A chave fica travada e fecha tentativas de desinstalar ou abrir as configurações enquanto houver bloqueio em execução."
                                    isProtectionReady -> "Proteção pronta: impedirá a desinstalação e o acesso às configurações do Desplug assim que um bloqueio estiver ativo."
                                    else -> "Ative esta chave para blindar o Desplug contra desinstalação ou parada forçada durante seus horários de foco."
                                },
                                fontFamily = InterTightFamily,
                                fontWeight = FontWeight.Normal,
                                fontSize = 13.sp,
                                lineHeight = 18.sp,
                                color = if (isLockedByActiveBlock) DesplugBlue else DesplugTextSecondary
                            )
                        }
                    }
                }
            }

            // 4. Seção de Permissões Essenciais do Sistema
            item {
                StaggeredFadeUp(delayMillis = 240) {
                    DesplugCard(
                        type = if (!hasAccessibility || !hasUsageStats || !hasOverlay) {
                            DesplugCardType.ALERT
                        } else {
                            DesplugCardType.DEFAULT
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Text(
                                text = "PERMISSÕES DO SISTEMA",
                                fontFamily = InterTightFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                letterSpacing = 0.08.em,
                                color = DesplugTextSecondary
                            )

                            PermissionStatusRow(
                                title = "Serviço de Acessibilidade",
                                subtitle = "Detecta abertura de apps em milissegundos",
                                isGranted = hasAccessibility,
                                onActivate = {
                                    com.desplug.app.data.BlockPreferences.grantTemporarySettingsPass()
                                    PermissionHelper.openAccessibilitySettings(context)
                                }
                            )

                            PermissionStatusRow(
                                title = "Acesso ao Uso (Usage Stats)",
                                subtitle = "Mede o tempo diário de cada aplicativo",
                                isGranted = hasUsageStats,
                                onActivate = {
                                    com.desplug.app.data.BlockPreferences.grantTemporarySettingsPass()
                                    PermissionHelper.openUsageStatsSettings(context)
                                }
                            )

                            PermissionStatusRow(
                                title = "Exibir Sobre Outros Apps",
                                subtitle = "Abre a tela de bloqueio instantaneamente",
                                isGranted = hasOverlay,
                                onActivate = {
                                    com.desplug.app.data.BlockPreferences.grantTemporarySettingsPass()
                                    PermissionHelper.openOverlaySettings(context)
                                }
                            )

                            PermissionStatusRow(
                                title = "Bateria Sem Restrições",
                                subtitle = "Mantém o monitoramento ativo em 2º plano",
                                isGranted = hasBatteryExemption,
                                onActivate = {
                                    com.desplug.app.data.BlockPreferences.grantTemporarySettingsPass()
                                    PermissionHelper.requestIgnoreBatteryOptimizations(context)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PermissionStatusRow(
    title: String,
    subtitle: String,
    isGranted: Boolean,
    onActivate: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontFamily = InterTightFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = DesplugTextPrimary
            )
            Text(
                text = subtitle,
                fontFamily = InterTightFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 12.sp,
                color = DesplugTextSecondary
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        if (isGranted) {
            DesplugChip(
                text = "Ativa",
                isSelected = false
            )
        } else {
            DesplugChip(
                text = "Ativar",
                isSelected = true,
                onClick = onActivate
            )
        }
    }
}
