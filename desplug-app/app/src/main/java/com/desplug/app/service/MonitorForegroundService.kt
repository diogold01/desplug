package com.desplug.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.desplug.app.MainActivity
import com.desplug.app.data.BlockPreferences
import com.desplug.app.util.PermissionHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Serviço em Primeiro Plano (Foreground Service) do Desplug.
 *
 * Mantém o app imune a fechamentos pelo sistema Android e executa o loop de verificação contínua
 * de limites de tempo mesmo se o usuário navegar dentro do app bloqueado.
 */
class MonitorForegroundService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var monitorJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = buildNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        startMonitoringLoop()
        return START_STICKY
    }

    private fun startMonitoringLoop() {
        if (monitorJob?.isActive == true) return
        monitorJob = serviceScope.launch {
            while (isActive) {
                if (BlockPreferences.isBlockingEnabled(applicationContext) &&
                    PermissionHelper.hasUsageStatsPermission(applicationContext)
                ) {
                    val currentApp = getForegroundPackageName()
                    if (currentApp != null) {
                        val (shouldBlock, reason) =
                            BlockPreferences.evaluateBlockStatus(applicationContext, currentApp)
                        if (shouldBlock) {
                            BlockPreferences.triggerLockScreen(applicationContext, currentApp, reason)
                        }
                    }
                }
                delay(1000L)
            }
        }
    }

    private fun getForegroundPackageName(): String? {
        val usageStatsManager =
            getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager ?: return null
        val endTime = System.currentTimeMillis()
        val beginTime = endTime - 10_000L

        val usageEvents = usageStatsManager.queryEvents(beginTime, endTime)
        val event = UsageEvents.Event()
        var lastForegroundPackage: String? = null

        while (usageEvents.hasNextEvent()) {
            usageEvents.getNextEvent(event)
            if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED) {
                lastForegroundPackage = event.packageName
            }
        }
        return lastForegroundPackage
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Monitoramento de Foco Desplug",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Mantém a proteção de foco ativa em segundo plano."
            setShowBadge(false)
        }
        val manager = getSystemService(NotificationManager::class.java)
        manager?.createNotificationChannel(channel)
    }

    private fun buildNotification(): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_lock)
            .setContentTitle("Desplug Ativo")
            .setContentText("Monitorando tempo de tela e limites diários")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .build()
    }

    override fun onDestroy() {
        monitorJob?.cancel()
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val CHANNEL_ID = "desplug_monitor_channel"
        private const val NOTIFICATION_ID = 1001

        fun startServiceIfNeeded(context: Context) {
            val appContext = context.applicationContext
            val intent = Intent(appContext, MonitorForegroundService::class.java)
            try {
                ContextCompat.startForegroundService(appContext, intent)
            } catch (_: Exception) {
                // Fallback silencioso
            }
        }
    }
}
