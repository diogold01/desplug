package com.desplug.app.data.model

import org.json.JSONArray
import org.json.JSONObject
import java.util.Calendar
import java.util.UUID

/**
 * Representa uma regra recorrente de bloqueio por horário.
 *
 * @param id Identificador único da regra (UUID)
 * @param name Nome amigável (ex: "Foco no Trabalho", "Estudo Noturno")
 * @param isEnabled Se a regra está ligada/desligada
 * @param startHour Hora de início (0..23)
 * @param startMinute Minuto de início (0..59)
 * @param endHour Hora de término (0..23)
 * @param endMinute Minuto de término (0..59)
 * @param daysOfWeek Dias ativos da semana usando Calendar.SUNDAY (1) até Calendar.SATURDAY (7)
 * @param targetPackages Pacotes de apps bloqueados durante esta janela de horário
 */
data class ScheduleRule(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val isEnabled: Boolean = true,
    val startHour: Int,
    val startMinute: Int,
    val endHour: Int,
    val endMinute: Int,
    val daysOfWeek: Set<Int> = setOf(
        Calendar.MONDAY,
        Calendar.TUESDAY,
        Calendar.WEDNESDAY,
        Calendar.THURSDAY,
        Calendar.FRIDAY
    ),
    val targetPackages: Set<String> = emptySet()
) {
    /**
     * Verifica se esta regra está ativa exatamente no momento atual segundo o relógio e fuso do celular.
     */
    fun isCurrentlyActive(calendar: Calendar = Calendar.getInstance()): Boolean {
        if (!isEnabled) return false

        val currentDay = calendar.get(Calendar.DAY_OF_WEEK)
        if (!daysOfWeek.contains(currentDay)) return false

        val currentMinutes = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE)
        val startTotalMinutes = startHour * 60 + startMinute
        val endTotalMinutes = endHour * 60 + endMinute

        return if (startTotalMinutes <= endTotalMinutes) {
            // Janela normal no mesmo dia (ex: 08:00 às 17:00)
            currentMinutes in startTotalMinutes until endTotalMinutes
        } else {
            // Janela que cruza a meia-noite (ex: 22:00 às 06:00 do dia seguinte)
            currentMinutes >= startTotalMinutes || currentMinutes < endTotalMinutes
        }
    }

    fun formattedTimeWindow(): String {
        return "%02d:%02d às %02d:%02d".format(startHour, startMinute, endHour, endMinute)
    }

    fun formattedDaysOfWeek(): String {
        val daysMap = mapOf(
            Calendar.MONDAY to "Seg",
            Calendar.TUESDAY to "Ter",
            Calendar.WEDNESDAY to "Qua",
            Calendar.THURSDAY to "Qui",
            Calendar.FRIDAY to "Sex",
            Calendar.SATURDAY to "Sáb",
            Calendar.SUNDAY to "Dom"
        )
        if (daysOfWeek.size == 7) return "Todos os dias"
        if (daysOfWeek == setOf(Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY, Calendar.THURSDAY, Calendar.FRIDAY)) {
            return "Seg a Sex"
        }
        if (daysOfWeek == setOf(Calendar.SATURDAY, Calendar.SUNDAY)) {
            return "Fim de semana"
        }
        val orderedDays = listOf(
            Calendar.SUNDAY, Calendar.MONDAY, Calendar.TUESDAY,
            Calendar.WEDNESDAY, Calendar.THURSDAY, Calendar.FRIDAY, Calendar.SATURDAY
        )
        return orderedDays.filter { daysOfWeek.contains(it) }.joinToString(", ") { daysMap[it] ?: "" }
    }

    fun toJson(): JSONObject {
        return JSONObject().apply {
            put("id", id)
            put("name", name)
            put("isEnabled", isEnabled)
            put("startHour", startHour)
            put("startMinute", startMinute)
            put("endHour", endHour)
            put("endMinute", endMinute)

            val daysArray = JSONArray()
            daysOfWeek.forEach { daysArray.put(it) }
            put("daysOfWeek", daysArray)

            val pkgsArray = JSONArray()
            targetPackages.forEach { pkgsArray.put(it) }
            put("targetPackages", pkgsArray)
        }
    }

    companion object {
        fun fromJson(json: JSONObject): ScheduleRule {
            val daysSet = mutableSetOf<Int>()
            val daysArray = json.optJSONArray("daysOfWeek")
            if (daysArray != null) {
                for (i in 0 until daysArray.length()) {
                    daysSet.add(daysArray.getInt(i))
                }
            }

            val pkgsSet = mutableSetOf<String>()
            val pkgsArray = json.optJSONArray("targetPackages")
            if (pkgsArray != null) {
                for (i in 0 until pkgsArray.length()) {
                    pkgsSet.add(pkgsArray.getString(i))
                }
            }

            return ScheduleRule(
                id = json.optString("id", UUID.randomUUID().toString()),
                name = json.optString("name", "Regra de Horário"),
                isEnabled = json.optBoolean("isEnabled", true),
                startHour = json.getInt("startHour"),
                startMinute = json.getInt("startMinute"),
                endHour = json.getInt("endHour"),
                endMinute = json.getInt("endMinute"),
                daysOfWeek = daysSet,
                targetPackages = pkgsSet
            )
        }
    }
}
