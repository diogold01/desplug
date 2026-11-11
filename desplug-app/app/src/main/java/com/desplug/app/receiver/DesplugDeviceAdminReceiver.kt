package com.desplug.app.receiver

import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent
import com.desplug.app.data.BlockPreferences

/**
 * Administrador do Dispositivo (Device Admin).
 * Impede que o aplicativo Desplug seja desinstalado enquanto estiver ativo como administrador.
 */
class DesplugDeviceAdminReceiver : DeviceAdminReceiver() {

    override fun onDisableRequested(context: Context, intent: Intent): CharSequence {
        if (BlockPreferences.hasAnyActiveProtectionNow(context)) {
            BlockPreferences.triggerLockScreen(
                context,
                "com.android.settings",
                "Proteção Anti-Burla: Não é permitido remover o Administrador do Dispositivo enquanto houver um bloqueio ativo."
            )
        }
        return "Atenção: Desativar esta proteção permitirá que o Desplug seja desinstalado durante momentos de distração."
    }
}
