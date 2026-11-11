package com.desplug.app.service

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import com.desplug.app.data.BlockPreferences

/**
 * Serviço de Acessibilidade do Desplug.
 *
 * Escuta eventos do sistema operacional em milissegundos. Ao detectar a abertura de um aplicativo,
 * avalia se ele ultrapassou o limite diário de minutos ou se possui bloqueio contínuo e dispara
 * a tela de sobreposição imediatamente.
 */
class DesplugAccessibilityService : AccessibilityService() {

    override fun onServiceConnected() {
        super.onServiceConnected()
        MonitorForegroundService.startServiceIfNeeded(applicationContext)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return

        val packageName = event.packageName?.toString() ?: return

        // Ignora pacotes de sistema e o próprio app
        if (packageName == this.packageName ||
            packageName == "com.android.systemui"
        ) {
            return
        }

        val (shouldBlock, reason) = BlockPreferences.evaluateBlockStatus(applicationContext, packageName)
        if (shouldBlock) {
            BlockPreferences.triggerLockScreen(applicationContext, packageName, reason)
        }
    }

    override fun onInterrupt() {
        // Nenhuma ação necessária
    }
}
