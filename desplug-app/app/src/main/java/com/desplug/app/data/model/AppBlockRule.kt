package com.desplug.app.data.model

import org.json.JSONObject

/**
 * Representa uma regra de bloqueio para um aplicativo específico.
 *
 * @param packageName Identificador único do app (ex: com.zhiliaoapp.musically)
 * @param appName Nome amigável do app (ex: TikTok)
 * @param dailyLimitMinutes Limite diário de uso em minutos (0 = bloqueio total instantâneo)
 * @param isAlwaysBlocked Se true, o app fica sempre bloqueado independentemente do tempo
 */
data class AppBlockRule(
    val packageName: String,
    val appName: String,
    val dailyLimitMinutes: Int = 0,
    val isAlwaysBlocked: Boolean = true
) {
    fun toJson(): JSONObject {
        return JSONObject().apply {
            put("packageName", packageName)
            put("appName", appName)
            put("dailyLimitMinutes", dailyLimitMinutes)
            put("isAlwaysBlocked", isAlwaysBlocked)
        }
    }

    companion object {
        fun fromJson(json: JSONObject): AppBlockRule {
            return AppBlockRule(
                packageName = json.getString("packageName"),
                appName = json.optString("appName", json.getString("packageName")),
                dailyLimitMinutes = json.optInt("dailyLimitMinutes", 0),
                isAlwaysBlocked = json.optBoolean("isAlwaysBlocked", true)
            )
        }
    }
}
