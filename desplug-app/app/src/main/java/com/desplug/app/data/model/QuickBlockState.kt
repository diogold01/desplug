package com.desplug.app.data.model

import org.json.JSONArray
import org.json.JSONObject

/**
 * Representa o estado atual do Bloqueio Rápido (Quick Block).
 *
 * @param isActive Se o Quick Block está ligado
 * @param startedAtMillis Timestamp de início
 * @param durationMinutes Duração total em minutos (0 = sem prazo / até ser desativado)
 * @param targetPackages Apps bloqueados durante este Quick Block
 * @param unlockRequestedAtMillis Timestamp em que o usuário digitou o PIN correto para desativar
 * @param unlockCooldownSeconds Segundos de espera obrigatória após o PIN (padrão 300s = 5 minutos)
 */
data class QuickBlockState(
    val isActive: Boolean = false,
    val startedAtMillis: Long = 0L,
    val durationMinutes: Int = 0,
    val targetPackages: Set<String> = emptySet(),
    val unlockRequestedAtMillis: Long = 0L,
    val unlockCooldownSeconds: Int = 300
) {
    /**
     * Retorna se o Quick Block expirou pelo tempo de duração configurado.
     */
    fun hasExpired(nowMillis: Long = System.currentTimeMillis()): Boolean {
        if (!isActive) return true
        if (durationMinutes <= 0) return false // Infinito até desligar
        val expiresAt = startedAtMillis + (durationMinutes * 60L * 1000L)
        return nowMillis >= expiresAt
    }

    /**
     * Retorna se o desbloqueio está em contagem regressiva de espera (cooldown).
     */
    fun isCoolingDown(nowMillis: Long = System.currentTimeMillis()): Boolean {
        if (!isActive || unlockRequestedAtMillis <= 0L) return false
        val cooldownEnd = unlockRequestedAtMillis + (unlockCooldownSeconds * 1000L)
        return nowMillis < cooldownEnd
    }

    /**
     * Retorna quantos segundos restam na espera de 5 minutos.
     */
    fun remainingCooldownSeconds(nowMillis: Long = System.currentTimeMillis()): Int {
        if (!isCoolingDown(nowMillis)) return 0
        val cooldownEnd = unlockRequestedAtMillis + (unlockCooldownSeconds * 1000L)
        return ((cooldownEnd - nowMillis) / 1000L).toInt().coerceAtLeast(0)
    }

    /**
     * Retorna se a espera já terminou e o bloqueio pode ser oficialmente encerrado.
     */
    fun isCooldownFinished(nowMillis: Long = System.currentTimeMillis()): Boolean {
        if (!isActive || unlockRequestedAtMillis <= 0L) return false
        val cooldownEnd = unlockRequestedAtMillis + (unlockCooldownSeconds * 1000L)
        return nowMillis >= cooldownEnd
    }

    fun toJson(): JSONObject {
        return JSONObject().apply {
            put("isActive", isActive)
            put("startedAtMillis", startedAtMillis)
            put("durationMinutes", durationMinutes)
            put("unlockRequestedAtMillis", unlockRequestedAtMillis)
            put("unlockCooldownSeconds", unlockCooldownSeconds)

            val array = JSONArray()
            targetPackages.forEach { array.put(it) }
            put("targetPackages", array)
        }
    }

    companion object {
        fun fromJson(json: JSONObject): QuickBlockState {
            val pkgsSet = mutableSetOf<String>()
            val array = json.optJSONArray("targetPackages")
            if (array != null) {
                for (i in 0 until array.length()) {
                    pkgsSet.add(array.getString(i))
                }
            }

            return QuickBlockState(
                isActive = json.optBoolean("isActive", false),
                startedAtMillis = json.optLong("startedAtMillis", 0L),
                durationMinutes = json.optInt("durationMinutes", 0),
                unlockRequestedAtMillis = json.optLong("unlockRequestedAtMillis", 0L),
                unlockCooldownSeconds = json.optInt("unlockCooldownSeconds", 300),
                targetPackages = pkgsSet
            )
        }
    }
}
