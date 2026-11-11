package com.desplug.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.desplug.app.service.MonitorForegroundService

/**
 * Religamento automático do serviço de monitoramento quando o celular é reiniciado
 * ou quando o APK do aplicativo é atualizado.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        if (action == Intent.ACTION_BOOT_COMPLETED ||
            action == Intent.ACTION_MY_PACKAGE_REPLACED ||
            action == "android.intent.action.QUICKBOOT_POWERON"
        ) {
            MonitorForegroundService.startServiceIfNeeded(context)
        }
    }
}
