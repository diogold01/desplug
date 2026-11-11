package com.desplug.app.util

import android.app.usage.UsageStatsManager
import android.content.Context
import java.util.Calendar

/**
 * Utilitário responsável pelo cálculo do tempo de uso em primeiro plano (Foreground)
 * utilizando o UsageStatsManager oficial do Android.
 *
 * Respeita rigorosamente o fuso horário configurado no aparelho (Calendar.getInstance()).
 */
object UsageStatsCalculator {

    /**
     * Retorna o momento (timestamp em ms) em que o "dia" começou segundo o fuso do aparelho.
     *
     * @param resetHour Hora do dia configurada para o reset (ex: 0 = meia-noite, 4 = 4h da manhã).
     */
    fun getStartOfDayMillis(resetHour: Int = 0): Long {
        val calendar = Calendar.getInstance().apply {
            val currentHour = get(Calendar.HOUR_OF_DAY)
            // Se ainda não passou da hora de reset, o "dia de uso" começou no dia anterior
            if (currentHour < resetHour) {
                add(Calendar.DAY_OF_YEAR, -1)
            }
            set(Calendar.HOUR_OF_DAY, resetHour)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis
    }

    /**
     * Calcula o tempo de tela em primeiro plano (em minutos) de um aplicativo desde o início do dia.
     */
    fun getUsageTodayInMinutes(context: Context, packageName: String, resetHour: Int = 0): Int {
        val usageStatsManager =
            context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager ?: return 0

        val startTime = getStartOfDayMillis(resetHour)
        val endTime = System.currentTimeMillis()

        if (startTime >= endTime) return 0

        val statsList = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            startTime,
            endTime
        )

        var totalTimeForegroundMs = 0L
        if (!statsList.isNullOrEmpty()) {
            for (stat in statsList) {
                if (stat.packageName == packageName) {
                    totalTimeForegroundMs += stat.totalTimeInForeground
                }
            }
        }

        return (totalTimeForegroundMs / 1000L / 60L).toInt()
    }

    /**
     * Retorna um mapa [packageName -> minutosUsadosHoje] de todos os apps com uso no dia.
     */
    fun getAllUsageTodayInMinutes(context: Context, resetHour: Int = 0): Map<String, Int> {
        val usageStatsManager =
            context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager ?: return emptyMap()

        val startTime = getStartOfDayMillis(resetHour)
        val endTime = System.currentTimeMillis()

        if (startTime >= endTime) return emptyMap()

        val statsList = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            startTime,
            endTime
        ) ?: return emptyMap()

        val resultMap = mutableMapOf<String, Long>()
        for (stat in statsList) {
            val current = resultMap.getOrDefault(stat.packageName, 0L)
            resultMap[stat.packageName] = current + stat.totalTimeInForeground
        }

        return resultMap.mapValues { (_, ms) -> (ms / 1000L / 60L).toInt() }
    }
}
