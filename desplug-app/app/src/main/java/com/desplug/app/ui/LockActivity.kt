package com.desplug.app.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.desplug.app.ui.components.AppIconView
import com.desplug.app.ui.components.DesplugCard
import com.desplug.app.ui.components.DesplugCardType
import com.desplug.app.ui.components.DesplugPrimaryButton
import com.desplug.app.ui.components.ParticleCloudCanvas
import com.desplug.app.ui.components.StaggeredFadeUp
import com.desplug.app.ui.theme.DesplugBackground
import com.desplug.app.ui.theme.DesplugRed
import com.desplug.app.ui.theme.DesplugRedCardBg
import com.desplug.app.ui.theme.DesplugRedCardBorder
import com.desplug.app.ui.theme.DesplugTextPrimary
import com.desplug.app.ui.theme.DesplugTextSecondary
import com.desplug.app.ui.theme.DesplugTheme
import com.desplug.app.ui.theme.InterTightFamily

/**
 * Tela de Bloqueio em tela cheia sobreposta instantaneamente com visual premium do Desplug.
 */
class LockActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        renderLockScreen(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        renderLockScreen(intent)
    }

    private fun renderLockScreen(currentIntent: Intent?) {
        val blockedPackage = currentIntent?.getStringExtra(EXTRA_BLOCKED_PACKAGE).orEmpty()
        val blockReason = currentIntent?.getStringExtra(EXTRA_BLOCK_REASON)
        val attemptCount = currentIntent?.getIntExtra(EXTRA_ATTEMPT_COUNT, 1) ?: 1

        setContent {
            DesplugTheme(darkTheme = true) {
                BackHandler(enabled = true) {
                    goToHomeScreen()
                }
                LockScreenContent(
                    blockedPackage = blockedPackage,
                    reason = blockReason,
                    attemptCount = attemptCount,
                    onGoHome = { goToHomeScreen() }
                )
            }
        }
    }

    private fun goToHomeScreen() {
        val homeIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(homeIntent)
        finish()
    }

    companion object {
        const val EXTRA_BLOCKED_PACKAGE = "extra_blocked_package"
        const val EXTRA_BLOCK_REASON = "extra_block_reason"
        const val EXTRA_ATTEMPT_COUNT = "extra_attempt_count"
    }
}

@Composable
private fun LockScreenContent(
    blockedPackage: String,
    reason: String?,
    attemptCount: Int,
    onGoHome: () -> Unit
) {
    val context = LocalContext.current
    val appLabel = remember(blockedPackage) {
        if (blockedPackage.isBlank()) {
            "Este aplicativo"
        } else {
            try {
                val pm = context.packageManager
                val appInfo = pm.getApplicationInfo(blockedPackage, 0)
                pm.getApplicationLabel(appInfo).toString()
            } catch (_: Exception) {
                blockedPackage
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = DesplugBackground
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // Nuvem de partículas de fundo
            ParticleCloudCanvas()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(horizontal = 28.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                StaggeredFadeUp(delayMillis = 0) {
                    AppIconView(
                        packageName = blockedPackage,
                        appName = appLabel,
                        size = 72.dp
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                StaggeredFadeUp(delayMillis = 80) {
                    Text(
                        text = "Foco Protegido",
                        fontFamily = InterTightFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 42.sp,
                        lineHeight = 44.sp,
                        letterSpacing = (-0.04).em,
                        color = DesplugTextPrimary,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                StaggeredFadeUp(delayMillis = 160) {
                    Text(
                        text = reason ?: "$appLabel está bloqueado pelo Desplug neste momento para proteger o seu tempo.",
                        fontFamily = InterTightFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 15.sp,
                        lineHeight = 22.sp,
                        color = DesplugTextSecondary,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Card de Alerta de Tentativas Diárias
                StaggeredFadeUp(delayMillis = 240) {
                    val shape = RoundedCornerShape(20.dp)
                    Box(
                        modifier = Modifier
                            .clip(shape)
                            .background(DesplugRedCardBg)
                            .border(1.dp, DesplugRedCardBorder, shape)
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "✋",
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (attemptCount == 1) {
                                    "Você tentou abrir 1 vez hoje"
                                } else {
                                    "Você tentou abrir $attemptCount vezes hoje"
                                },
                                fontFamily = InterTightFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = DesplugRed
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))

                StaggeredFadeUp(delayMillis = 320) {
                    DesplugPrimaryButton(
                        text = "Voltar à Tela Inicial",
                        onClick = onGoHome
                    )
                }
            }
        }
    }
}
