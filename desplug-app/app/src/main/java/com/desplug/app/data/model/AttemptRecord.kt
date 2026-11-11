package com.desplug.app.data.model

import org.json.JSONObject

/**
 * Registro de tentativas diárias de abertura de apps bloqueados.
 */
data class AttemptRecord(
    val packageName: String,
    val count: Int = 0,
    val lastAttemptTimestamp: Long = 0L
) {
    fun toJson(): JSONObject {
        return JSONObject().apply {
            put("packageName", packageName)
            put("count", count)
            put("lastAttemptTimestamp", lastAttemptTimestamp)
        }
    }

    companion object {
        fun fromJson(json: JSONObject): AttemptRecord {
            return AttemptRecord(
                packageName = json.getString("packageName"),
                count = json.optInt("count", 0),
                lastAttemptTimestamp = json.optLong("lastAttemptTimestamp", 0L)
            )
        }
    }
}
