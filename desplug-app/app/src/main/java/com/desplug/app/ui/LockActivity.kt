package com.desplug.app.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.desplug.app.ui.theme.DesplugTheme

/**
 * Tela de Bloqueio sobreposta instantaneamente quando um app restrito é aberto
 * ou quando o limite diário de uso é atingido.
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

        setContent {
            DesplugTheme(darkTheme = true) {
                // Impede o retorno ao app bloqueado pela tecla Voltar
                BackHandler(enabled = true) {
                    goToHomeScreen()
                }
                LockScreenContent(
                    blockedPackage = blockedPackage,
                    reason = blockReason,
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
    }
}

@Composable
private fun LockScreenContent(
    blockedPackage: String,
    reason: String?,
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
        color = Color(0xFF0D1117)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp, vertical = 48.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1F2937)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🔒",
                    fontSize = 42.sp
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "Limite de Foco Atingido",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = reason ?: "$appLabel está bloqueado pelo Desplug neste momento para proteger a sua rotina.",
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFF9CA3AF),
                textAlign = TextAlign.Center,
                lineHeight = 24.sp
            )

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = onGoHome,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF10B981),
                    contentColor = Color(0xFF052E16)
                )
            ) {
                Text(
                    text = "Voltar à Tela Inicial",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
