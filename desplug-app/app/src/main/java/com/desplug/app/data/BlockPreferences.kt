package com.desplug.app.data

import android.content.Context
import android.content.Intent
import android.os.SystemClock
import com.desplug.app.data.model.AppBlockRule
import com.desplug.app.data.model.AttemptRecord
import com.desplug.app.data.model.QuickBlockState
import com.desplug.app.data.model.ScheduleRule
import com.desplug.app.ui.LockActivity
import com.desplug.app.util.UsageStatsCalculator
import org.json.JSONArray
import org.json.JSONObject
import java.security.MessageDigest
import java.util.Calendar

/**
 * Gerenciador centralizado de preferências, regras, estado do Quick Block e contador de tentativas.
 */
object BlockPreferences {
    private const val PREFS_NAME = "desplug_block_prefs"
    private const val KEY_BLOCKING_ENABLED = "blocking_enabled"
    private const val KEY_BLOCK_RULES = "block_rules_json"
    private const val KEY_SCHEDULE_RULES = "schedule_rules_json"
    private const val KEY_QUICK_BLOCK_STATE = "quick_block_state_json"
    private const val KEY_PIN_HASH = "security_pin_hash"
    private const val KEY_DAY_RESET_HOUR = "day_reset_hour"
    private const val KEY_ATTEMPTS_JSON = "attempts_records_json"
    private const val KEY_ATTEMPTS_DAY_TIMESTAMP = "attempts_day_timestamp"

    @Volatile
    private var lastLockTriggerTimeMs: Long = 0L

    @Volatile
    private var lastLockedPackage: String = ""

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun isBlockingEnabled(context: Context): Boolean {
        return prefs(context).getBoolean(KEY_BLOCKING_ENABLED, true)
    }

    fun setBlockingEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_BLOCKING_ENABLED, enabled).apply()
    }

    fun getDayResetHour(context: Context): Int {
        return prefs(context).getInt(KEY_DAY_RESET_HOUR, 0)
    }

    fun setDayResetHour(context: Context, hour: Int) {
        val clampedHour = hour.coerceIn(0, 23)
        prefs(context).edit().putInt(KEY_DAY_RESET_HOUR, clampedHour).apply()
    }

    // --- CONTADOR DE TENTATIVAS DIÁRIAS ---

    /**
     * Incrementa o contador de tentativas para o app bloqueado e retorna a contagem atualizada de hoje.
     */
    @Synchronized
    fun recordAndGetAttemptCount(context: Context, packageName: String): Int {
        val resetHour = getDayResetHour(context)
        val todayStart = UsageStatsCalculator.getStartOfDayMillis(resetHour)
        val savedDay = prefs(context).getLong(KEY_ATTEMPTS_DAY_TIMESTAMP, 0L)

        val attemptsMap = mutableMapOf<String, AttemptRecord>()

        // Se virou o dia pelo horário de reset, zera as tentativas do dia anterior
        if (savedDay == todayStart) {
            val jsonString = prefs(context).getString(KEY_ATTEMPTS_JSON, null)
            if (!jsonString.isNullOrBlank()) {
                try {
                    val array = JSONArray(jsonString)
                    for (i in 0 until array.length()) {
                        val record = AttemptRecord.fromJson(array.getJSONObject(i))
                        attemptsMap[record.packageName] = record
                    }
                } catch (_: Exception) {}
            }
        }

        val currentRecord = attemptsMap[packageName]
        val newCount = (currentRecord?.count ?: 0) + 1
        attemptsMap[packageName] = AttemptRecord(
            packageName = packageName,
            count = newCount,
            lastAttemptTimestamp = System.currentTimeMillis()
        )

        val outputArray = JSONArray()
        attemptsMap.values.forEach { outputArray.put(it.toJson()) }

        prefs(context).edit()
            .putLong(KEY_ATTEMPTS_DAY_TIMESTAMP, todayStart)
            .putString(KEY_ATTEMPTS_JSON, outputArray.toString())
            .apply()

        return newCount
    }

    fun getAttemptCountToday(context: Context, packageName: String): Int {
        val resetHour = getDayResetHour(context)
        val todayStart = UsageStatsCalculator.getStartOfDayMillis(resetHour)
        val savedDay = prefs(context).getLong(KEY_ATTEMPTS_DAY_TIMESTAMP, 0L)
        if (savedDay != todayStart) return 0

        val jsonString = prefs(context).getString(KEY_ATTEMPTS_JSON, null) ?: return 0
        return try {
            val array = JSONArray(jsonString)
            for (i in 0 until array.length()) {
                val record = AttemptRecord.fromJson(array.getJSONObject(i))
                if (record.packageName == packageName) return record.count
            }
            0
        } catch (_: Exception) {
            0
        }
    }

    fun getTotalAttemptsToday(context: Context): Int {
        val resetHour = getDayResetHour(context)
        val todayStart = UsageStatsCalculator.getStartOfDayMillis(resetHour)
        val savedDay = prefs(context).getLong(KEY_ATTEMPTS_DAY_TIMESTAMP, 0L)
        if (savedDay != todayStart) return 0

        val jsonString = prefs(context).getString(KEY_ATTEMPTS_JSON, null) ?: return 0
        return try {
            val array = JSONArray(jsonString)
            var total = 0
            for (i in 0 until array.length()) {
                val record = AttemptRecord.fromJson(array.getJSONObject(i))
                total += record.count
            }
            total
        } catch (_: Exception) {
            0
        }
    }

    // --- GERENCIAMENTO DE PIN ---

    fun hasPinConfigured(context: Context): Boolean {
        return !prefs(context).getString(KEY_PIN_HASH, null).isNullOrBlank()
    }

    fun setSecurityPin(context: Context, pin: String) {
        val hash = hashPin(pin)
        prefs(context).edit().putString(KEY_PIN_HASH, hash).apply()
    }

    fun verifySecurityPin(context: Context, pin: String): Boolean {
        val savedHash = prefs(context).getString(KEY_PIN_HASH, null) ?: return true
        return savedHash == hashPin(pin)
    }

    private fun hashPin(pin: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(pin.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    // --- QUICK BLOCK ---

    fun getQuickBlockState(context: Context): QuickBlockState {
        val jsonString = prefs(context).getString(KEY_QUICK_BLOCK_STATE, null) ?: return QuickBlockState()
        return try {
            val state = QuickBlockState.fromJson(JSONObject(jsonString))
            val now = System.currentTimeMillis()
            if (state.isActive && state.hasExpired(now)) {
                stopQuickBlock(context)
                QuickBlockState()
            } else if (state.isActive && state.isCooldownFinished(now)) {
                stopQuickBlock(context)
                QuickBlockState()
            } else {
                state
            }
        } catch (_: Exception) {
            QuickBlockState()
        }
    }

    fun startQuickBlock(
        context: Context,
        durationMinutes: Int,
        targetPackages: Set<String>,
        cooldownSeconds: Int = 300
    ) {
        val state = QuickBlockState(
            isActive = true,
            startedAtMillis = System.currentTimeMillis(),
            durationMinutes = durationMinutes,
            targetPackages = targetPackages,
            unlockRequestedAtMillis = 0L,
            unlockCooldownSeconds = cooldownSeconds
        )
        prefs(context).edit().putString(KEY_QUICK_BLOCK_STATE, state.toJson().toString()).apply()
    }

    fun requestQuickBlockUnlock(context: Context) {
        val current = getQuickBlockState(context)
        if (!current.isActive) return
        val updated = current.copy(unlockRequestedAtMillis = System.currentTimeMillis())
        prefs(context).edit().putString(KEY_QUICK_BLOCK_STATE, updated.toJson().toString()).apply()
    }

    fun cancelQuickBlockUnlock(context: Context) {
        val current = getQuickBlockState(context)
        if (!current.isActive) return
        val updated = current.copy(unlockRequestedAtMillis = 0L)
        prefs(context).edit().putString(KEY_QUICK_BLOCK_STATE, updated.toJson().toString()).apply()
    }

    fun stopQuickBlock(context: Context) {
        prefs(context).edit().remove(KEY_QUICK_BLOCK_STATE).apply()
    }

    // --- REGRAS POR APLICATIVO ---

    fun getAllRules(context: Context): List<AppBlockRule> {
        val jsonString = prefs(context).getString(KEY_BLOCK_RULES, null) ?: return emptyList()
        return try {
            val jsonArray = JSONArray(jsonString)
            val list = mutableListOf<AppBlockRule>()
            for (i in 0 until jsonArray.length()) {
                list.add(AppBlockRule.fromJson(jsonArray.getJSONObject(i)))
            }
            list
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun getRuleForPackage(context: Context, packageName: String): AppBlockRule? {
        return getAllRules(context).firstOrNull { it.packageName == packageName }
    }

    fun saveRule(context: Context, rule: AppBlockRule) {
        val currentRules = getAllRules(context).toMutableList()
        val existingIndex = currentRules.indexOfFirst { it.packageName == rule.packageName }
        if (existingIndex >= 0) {
            currentRules[existingIndex] = rule
        } else {
            currentRules.add(rule)
        }
        persistRules(context, currentRules)
    }

    fun removeRule(context: Context, packageName: String) {
        val currentRules = getAllRules(context).filterNot { it.packageName == packageName }
        persistRules(context, currentRules)
    }

    private fun persistRules(context: Context, rules: List<AppBlockRule>) {
        val jsonArray = JSONArray()
        rules.forEach { jsonArray.put(it.toJson()) }
        prefs(context).edit().putString(KEY_BLOCK_RULES, jsonArray.toString()).apply()
    }

    // --- REGRAS POR HORÁRIO ---

    fun getAllScheduleRules(context: Context): List<ScheduleRule> {
        val jsonString = prefs(context).getString(KEY_SCHEDULE_RULES, null) ?: return emptyList()
        return try {
            val jsonArray = JSONArray(jsonString)
            val list = mutableListOf<ScheduleRule>()
            for (i in 0 until jsonArray.length()) {
                list.add(ScheduleRule.fromJson(jsonArray.getJSONObject(i)))
            }
            list
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun saveScheduleRule(context: Context, rule: ScheduleRule) {
        val current = getAllScheduleRules(context).toMutableList()
        val index = current.indexOfFirst { it.id == rule.id }
        if (index >= 0) {
            current[index] = rule
        } else {
            current.add(rule)
        }
        persistScheduleRules(context, current)
    }

    fun removeScheduleRule(context: Context, ruleId: String) {
        val current = getAllScheduleRules(context).filterNot { it.id == ruleId }
        persistScheduleRules(context, current)
    }

    private fun persistScheduleRules(context: Context, rules: List<ScheduleRule>) {
        val jsonArray = JSONArray()
        rules.forEach { jsonArray.put(it.toJson()) }
        prefs(context).edit().putString(KEY_SCHEDULE_RULES, jsonArray.toString()).apply()
    }

    // --- AVALIAÇÃO DE BLOQUEIO ---

    fun evaluateBlockStatus(context: Context, packageName: String): Pair<Boolean, String?> {
        if (packageName.isBlank() || packageName == context.packageName) return Pair(false, null)
        if (!isBlockingEnabled(context)) return Pair(false, null)

        val nowMillis = System.currentTimeMillis()

        // 1. Quick Block
        val quickBlock = getQuickBlockState(context)
        if (quickBlock.isActive && !quickBlock.hasExpired(nowMillis)) {
            val isTarget = if (quickBlock.targetPackages.isNotEmpty()) {
                quickBlock.targetPackages.contains(packageName)
            } else {
                getAllRules(context).any { it.packageName == packageName }
            }

            if (isTarget) {
                return if (quickBlock.isCoolingDown(nowMillis)) {
                    val rem = quickBlock.remainingCooldownSeconds(nowMillis)
                    Pair(
                        true,
                        "Quick Block em espera para desativar: restam %d min %02d seg.".format(rem / 60, rem % 60)
                    )
                } else {
                    Pair(true, "Bloqueado pelo Quick Block (Foco Imediato Ativo).")
                }
            }
        }

        val nowCalendar = Calendar.getInstance()

        // 2. Regras de Horário
        val activeSchedules = getAllScheduleRules(context).filter {
            it.isEnabled && it.isCurrentlyActive(nowCalendar) && it.targetPackages.contains(packageName)
        }

        if (activeSchedules.isNotEmpty()) {
            val schedule = activeSchedules.first()
            return Pair(
                true,
                "Bloqueado pela regra de horário \"${schedule.name}\" (${schedule.formattedTimeWindow()})."
            )
        }

        // 3. Regra por aplicativo
        val appRule = getRuleForPackage(context, packageName)
        if (appRule != null) {
            if (appRule.isAlwaysBlocked || appRule.dailyLimitMinutes <= 0) {
                return Pair(true, "Este aplicativo está configurado como sempre bloqueado.")
            }

            val resetHour = getDayResetHour(context)
            val usedMinutes = UsageStatsCalculator.getUsageTodayInMinutes(context, packageName, resetHour)
            if (usedMinutes >= appRule.dailyLimitMinutes) {
                return Pair(
                    true,
                    "Você atingiu o seu limite diário de ${appRule.dailyLimitMinutes} min (usou $usedMinutes min hoje)."
                )
            }
        }

        return Pair(false, null)
    }

    fun triggerLockScreen(context: Context, blockedPackage: String, reason: String? = null) {
        val now = SystemClock.elapsedRealtime()
        if (blockedPackage == lastLockedPackage && (now - lastLockTriggerTimeMs) < 800L) {
            return
        }
        lastLockedPackage = blockedPackage
        lastLockTriggerTimeMs = now

        // Registra e incrementa o contador de tentativas diárias
        val attemptCount = recordAndGetAttemptCount(context, blockedPackage)

        val lockIntent = Intent(context.applicationContext, LockActivity::class.java).apply {
            addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP or
                    Intent.FLAG_ACTIVITY_NO_ANIMATION
            )
            putExtra(LockActivity.EXTRA_BLOCKED_PACKAGE, blockedPackage)
            putExtra(LockActivity.EXTRA_BLOCK_REASON, reason)
            putExtra(LockActivity.EXTRA_ATTEMPT_COUNT, attemptCount)
        }
        context.applicationContext.startActivity(lockIntent)
    }
}
