package com.desplug.app.service

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.desplug.app.data.BlockPreferences
import com.desplug.app.util.PermissionHelper

/**
 * Serviço de Acessibilidade do Desplug.
 *
 * 1. Detecta a abertura de aplicativos bloqueados em milissegundos.
 * 2. Proteção Anti-Burla (Etapa 6): Enquanto houver um bloqueio ativo, impede que o usuário
 *    entre nas telas de Configurações do próprio Desplug (Forçar parada, Desinstalar,
 *    Desativar Acessibilidade ou Remover Administrador do Dispositivo).
 */
class DesplugAccessibilityService : AccessibilityService() {

    private val settingsPackages = setOf(
        "com.android.settings",
        "com.google.android.packageinstaller",
        "com.android.packageinstaller",
        "com.samsung.android.packageinstaller",
        "com.google.android.permissioncontroller",
        "com.android.permissioncontroller",
        "com.miui.securitycenter"
    )

    override fun onServiceConnected() {
        super.onServiceConnected()
        MonitorForegroundService.startServiceIfNeeded(applicationContext)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        val eventType = event.eventType
        if (eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED &&
            eventType != AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
        ) {
            return
        }

        val packageName = event.packageName?.toString() ?: return

        // Ignora a própria interface do Desplug e SystemUI
        if (packageName == this.packageName || packageName == "com.android.systemui") {
            return
        }

        // 1. Verificação de Anti-Burla nas Configurações do Android
        if (settingsPackages.contains(packageName)) {
            if (shouldBlockSettingsTampering(event)) {
                performGlobalAction(GLOBAL_ACTION_BACK)
                performGlobalAction(GLOBAL_ACTION_HOME)
                BlockPreferences.triggerLockScreen(
                    applicationContext,
                    packageName,
                    "Proteção Anti-Burla Ativa: Não é permitido alterar permissões, forçar parada ou desinstalar o Desplug enquanto um bloqueio estiver em execução."
                )
            }
            return
        }

        // 2. Verificação normal de abertura de apps bloqueados (apenas em mudança de janela)
        if (eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val (shouldBlock, reason) = BlockPreferences.evaluateBlockStatus(applicationContext, packageName)
            if (shouldBlock) {
                BlockPreferences.triggerLockScreen(applicationContext, packageName, reason)
            }
        }
    }

    /**
     * Inspeciona se a tela atual das Configurações do Android é a página de detalhes/permissões/admin
     * do próprio Desplug durante um bloqueio ativo.
     */
    private fun shouldBlockSettingsTampering(event: AccessibilityEvent): Boolean {
        val context = applicationContext
        if (!BlockPreferences.isAntiTamperEnabled(context)) return false
        if (BlockPreferences.hasTemporarySettingsPass()) return false
        if (!BlockPreferences.hasAnyActiveProtectionNow(context)) return false

        val className = event.className?.toString().orEmpty()
        val eventText = event.text.joinToString(" ")

        // Se o Device Admin ainda NÃO está ativo, sempre permite a tela DeviceAdminAdd para que o usuário possa ativá-lo
        if (className.contains("DeviceAdminAdd", ignoreCase = true) &&
            !PermissionHelper.isDeviceAdminActive(context)
        ) {
            return false
        }

        // Se o próprio evento já indica tela de desinstalação ou detalhes do Desplug
        if (eventText.contains("Desplug", ignoreCase = true) &&
            (className.contains("InstalledAppDetails", ignoreCase = true) ||
                className.contains("DeviceAdminAdd", ignoreCase = true) ||
                className.contains("UninstallerActivity", ignoreCase = true) ||
                className.contains("AlertDialog", ignoreCase = true))
        ) {
            return true
        }

        val rootNode = rootInActiveWindow ?: return false
        return try {
            val texts = mutableListOf<String>()
            collectVisibleTexts(rootNode, texts, maxNodes = 80)
            val combined = texts.joinToString(" | ")

            val mentionsDesplug = combined.contains("Desplug", ignoreCase = true) ||
                combined.contains("com.desplug.app", ignoreCase = true)

            if (!mentionsDesplug) return false

            // Se o Administrador do Dispositivo ainda não está ativo, permite que o usuário ative na tela do sistema
            if (!PermissionHelper.isDeviceAdminActive(context) &&
                (combined.contains("Desplug — Proteção contra Desinstalação", ignoreCase = true) ||
                    combined.contains("administrador", ignoreCase = true))
            ) {
                return false
            }

            // Verifica se está dentro da página de Informações do App, Acessibilidade do Desplug ou Device Admin do Desplug
            // (Ignora a lista geral de configurações onde "Desplug" aparece apenas como um item da lista ao lado de outros apps,
            // a menos que contenha botões de ação direta contra o Desplug)
            val dangerousKeywords = listOf(
                "Forçar parada",
                "Forçar interrupção",
                "Force stop",
                "Desinstalar",
                "Uninstall",
                "Desativar aplicativo",
                "Desativar",
                "Limpar dados",
                "Armazenamento e cache",
                "Desplug — Monitor de Foco",
                "Desplug — Proteção contra Desinstalação",
                "Permitir que o app Desplug"
            )

            dangerousKeywords.any { keyword ->
                combined.contains(keyword, ignoreCase = true)
            }
        } catch (_: Exception) {
            false
        }
    }

    private fun collectVisibleTexts(
        node: AccessibilityNodeInfo?,
        outList: MutableList<String>,
        maxNodes: Int
    ) {
        if (node == null || outList.size >= maxNodes) return
        node.text?.toString()?.takeIf { it.isNotBlank() }?.let { outList.add(it) }
        node.contentDescription?.toString()?.takeIf { it.isNotBlank() }?.let { outList.add(it) }

        val childCount = node.childCount
        for (i in 0 until childCount) {
            if (outList.size >= maxNodes) break
            collectVisibleTexts(node.getChild(i), outList, maxNodes)
        }
    }

    override fun onInterrupt() {
        // Nenhuma ação necessária
    }
}
