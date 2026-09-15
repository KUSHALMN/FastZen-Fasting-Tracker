package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.*
import org.json.JSONArray
import org.json.JSONObject
import java.util.Calendar

/**
 * Lightweight, 100% offline, local-only persistence for FastZen.
 * Preserves active fasting timer state across app restarts, process death,
 * and system reboots.
 */
object FastZenStorage {
    private const val PREFS_NAME = "fastzen_preferences"

    // Active Timer State
    private const val KEY_IS_FASTING = "is_fasting"
    private const val KEY_FAST_START_TIME = "fast_start_time"
    private const val KEY_ACTIVE_TARGET_HOURS = "active_target_hours"
    private const val KEY_SELECTED_PLAN = "selected_plan"
    private const val KEY_CUSTOM_HOURS = "custom_hours"
    private const val KEY_STREAK_DAYS = "streak_days"

    // Alert Sent Flags
    private const val KEY_GOAL_ALERT_SENT = "goal_alert_sent"
    private const val KEY_KETOSIS_ALERT_SENT = "ketosis_alert_sent"
    private const val KEY_AUTOPHAGY_ALERT_SENT = "autophagy_alert_sent"

    // Hydration
    private const val KEY_WATER_ML = "water_ml"
    private const val KEY_WATER_DAY = "water_day"

    // JSON Lists
    private const val KEY_SESSIONS_JSON = "sessions_json"
    private const val KEY_WEIGHT_JSON = "weight_json"
    private const val KEY_SYMPTOMS_JSON = "symptoms_json"

    // Notification Preferences
    private const val KEY_NOTIF_ENABLED = "notif_enabled"
    private const val KEY_NOTIF_GOAL = "notif_goal"
    private const val KEY_NOTIF_STAGE = "notif_stage"
    private const val KEY_NOTIF_HYDRATION = "notif_hydration"
    private const val KEY_NOTIF_INTERVAL = "notif_interval"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    // --- Fasting State ---

    fun saveFastingState(
        context: Context,
        isFasting: Boolean,
        startTime: Long,
        targetHours: Int,
        plan: FastingPlan,
        customHours: Int
    ) {
        getPrefs(context).edit().apply {
            putBoolean(KEY_IS_FASTING, isFasting)
            putLong(KEY_FAST_START_TIME, startTime)
            putInt(KEY_ACTIVE_TARGET_HOURS, targetHours)
            putString(KEY_SELECTED_PLAN, plan.name)
            putInt(KEY_CUSTOM_HOURS, customHours)
            apply()
        }
    }

    fun isFasting(context: Context): Boolean = getPrefs(context).getBoolean(KEY_IS_FASTING, false)
    fun getFastStartTime(context: Context): Long = getPrefs(context).getLong(KEY_FAST_START_TIME, 0L)
    fun getActiveTargetHours(context: Context): Int = getPrefs(context).getInt(KEY_ACTIVE_TARGET_HOURS, 16)
    fun getCustomHours(context: Context): Int = getPrefs(context).getInt(KEY_CUSTOM_HOURS, 14)

    fun getSelectedPlan(context: Context): FastingPlan {
        val name = getPrefs(context).getString(KEY_SELECTED_PLAN, FastingPlan.PLAN_16_8.name)
        return try {
            FastingPlan.valueOf(name ?: FastingPlan.PLAN_16_8.name)
        } catch (_: Exception) {
            FastingPlan.PLAN_16_8
        }
    }

    fun saveAlertFlags(context: Context, goalSent: Boolean, ketosisSent: Boolean, autophagySent: Boolean) {
        getPrefs(context).edit().apply {
            putBoolean(KEY_GOAL_ALERT_SENT, goalSent)
            putBoolean(KEY_KETOSIS_ALERT_SENT, ketosisSent)
            putBoolean(KEY_AUTOPHAGY_ALERT_SENT, autophagySent)
            apply()
        }
    }

    fun isGoalAlertSent(context: Context): Boolean = getPrefs(context).getBoolean(KEY_GOAL_ALERT_SENT, false)
    fun isKetosisAlertSent(context: Context): Boolean = getPrefs(context).getBoolean(KEY_KETOSIS_ALERT_SENT, false)
    fun isAutophagyAlertSent(context: Context): Boolean = getPrefs(context).getBoolean(KEY_AUTOPHAGY_ALERT_SENT, false)

    // --- Streak Days ---

    fun getStreakDays(context: Context): Int = getPrefs(context).getInt(KEY_STREAK_DAYS, 6)
    fun saveStreakDays(context: Context, streak: Int) {
        getPrefs(context).edit().putInt(KEY_STREAK_DAYS, streak).apply()
    }

    // --- Water Hydration (with automatic daily reset) ---

    fun getWaterMl(context: Context): Int {
        val prefs = getPrefs(context)
        val savedDay = prefs.getInt(KEY_WATER_DAY, -1)
        val currentDay = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)

        return if (savedDay != currentDay) {
            // New day: reset to 0
            prefs.edit().putInt(KEY_WATER_DAY, currentDay).putInt(KEY_WATER_ML, 0).apply()
            0
        } else {
            prefs.getInt(KEY_WATER_ML, 1500)
        }
    }

    fun saveWaterMl(context: Context, amountMl: Int) {
        val currentDay = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        getPrefs(context).edit().apply {
            putInt(KEY_WATER_DAY, currentDay)
            putInt(KEY_WATER_ML, amountMl)
            apply()
        }
    }

    // --- Completed Sessions History ---

    fun loadSessions(context: Context): List<FastSession> {
        val jsonString = getPrefs(context).getString(KEY_SESSIONS_JSON, null) ?: return defaultSessions()
        return try {
            val jsonArray = JSONArray(jsonString)
            val list = mutableListOf<FastSession>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val planName = obj.optString("plan", FastingPlan.PLAN_16_8.name)
                val plan = try { FastingPlan.valueOf(planName) } catch (_: Exception) { FastingPlan.PLAN_16_8 }
                list.add(
                    FastSession(
                        id = obj.getString("id"),
                        plan = plan,
                        targetHours = obj.optInt("targetHours", 16),
                        startTime = obj.getLong("startTime"),
                        endTime = obj.getLong("endTime"),
                        durationSeconds = obj.getLong("durationSeconds"),
                        completedTarget = obj.getBoolean("completedTarget")
                    )
                )
            }
            if (list.isEmpty()) defaultSessions() else list
        } catch (_: Exception) {
            defaultSessions()
        }
    }

    fun saveSessions(context: Context, sessions: List<FastSession>) {
        try {
            val jsonArray = JSONArray()
            sessions.take(100).forEach { session ->
                val obj = JSONObject().apply {
                    put("id", session.id)
                    put("plan", session.plan.name)
                    put("targetHours", session.targetHours)
                    put("startTime", session.startTime)
                    put("endTime", session.endTime)
                    put("durationSeconds", session.durationSeconds)
                    put("completedTarget", session.completedTarget)
                }
                jsonArray.put(obj)
            }
            getPrefs(context).edit().putString(KEY_SESSIONS_JSON, jsonArray.toString()).apply()
        } catch (_: Exception) {}
    }

    private fun defaultSessions(): List<FastSession> {
        val now = System.currentTimeMillis()
        return listOf(
            FastSession(
                id = "1",
                plan = FastingPlan.PLAN_16_8,
                targetHours = 16,
                startTime = now - 86400000L - (16 * 3600000L + 1800000L),
                endTime = now - 86400000L,
                durationSeconds = 16 * 3600L + 1800L,
                completedTarget = true
            ),
            FastSession(
                id = "2",
                plan = FastingPlan.PLAN_18_6,
                targetHours = 18,
                startTime = now - (86400000L * 2) - (18 * 3600000L),
                endTime = now - (86400000L * 2),
                durationSeconds = 18 * 3600L + 600L,
                completedTarget = true
            ),
            FastSession(
                id = "3",
                plan = FastingPlan.PLAN_16_8,
                targetHours = 16,
                startTime = now - (86400000L * 3) - (15 * 3600000L),
                endTime = now - (86400000L * 3),
                durationSeconds = 15 * 3600L,
                completedTarget = false
            )
        )
    }

    // --- Weight Entries ---

    fun loadWeightEntries(context: Context): List<WeightEntry> {
        val jsonString = getPrefs(context).getString(KEY_WEIGHT_JSON, null) ?: return defaultWeightEntries()
        return try {
            val jsonArray = JSONArray(jsonString)
            val list = mutableListOf<WeightEntry>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(
                    WeightEntry(
                        id = obj.getString("id"),
                        weightKg = obj.getDouble("weightKg").toFloat(),
                        timestamp = obj.getLong("timestamp"),
                        note = obj.optString("note", "")
                    )
                )
            }
            if (list.isEmpty()) defaultWeightEntries() else list
        } catch (_: Exception) {
            defaultWeightEntries()
        }
    }

    fun saveWeightEntries(context: Context, entries: List<WeightEntry>) {
        try {
            val jsonArray = JSONArray()
            entries.take(100).forEach { entry ->
                val obj = JSONObject().apply {
                    put("id", entry.id)
                    put("weightKg", entry.weightKg.toDouble())
                    put("timestamp", entry.timestamp)
                    put("note", entry.note)
                }
                jsonArray.put(obj)
            }
            getPrefs(context).edit().putString(KEY_WEIGHT_JSON, jsonArray.toString()).apply()
        } catch (_: Exception) {}
    }

    private fun defaultWeightEntries(): List<WeightEntry> {
        val now = System.currentTimeMillis()
        return listOf(
            WeightEntry(id = "1", weightKg = 74.2f, timestamp = now - 86400000L * 3),
            WeightEntry(id = "2", weightKg = 73.8f, timestamp = now - 86400000L * 1),
            WeightEntry(id = "3", weightKg = 73.4f, timestamp = now)
        )
    }

    // --- Symptom Logs ---

    fun loadSymptomLogs(context: Context): List<SymptomLog> {
        val jsonString = getPrefs(context).getString(KEY_SYMPTOMS_JSON, null) ?: return defaultSymptomLogs()
        return try {
            val jsonArray = JSONArray(jsonString)
            val list = mutableListOf<SymptomLog>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(
                    SymptomLog(
                        id = obj.getString("id"),
                        feeling = obj.getString("feeling"),
                        energyLevel = obj.getInt("energyLevel"),
                        timestamp = obj.getLong("timestamp")
                    )
                )
            }
            if (list.isEmpty()) defaultSymptomLogs() else list
        } catch (_: Exception) {
            defaultSymptomLogs()
        }
    }

    fun saveSymptomLogs(context: Context, logs: List<SymptomLog>) {
        try {
            val jsonArray = JSONArray()
            logs.take(100).forEach { log ->
                val obj = JSONObject().apply {
                    put("id", log.id)
                    put("feeling", log.feeling)
                    put("energyLevel", log.energyLevel)
                    put("timestamp", log.timestamp)
                }
                jsonArray.put(obj)
            }
            getPrefs(context).edit().putString(KEY_SYMPTOMS_JSON, jsonArray.toString()).apply()
        } catch (_: Exception) {}
    }

    private fun defaultSymptomLogs(): List<SymptomLog> {
        val now = System.currentTimeMillis()
        return listOf(
            SymptomLog(id = "1", feeling = "Energized", energyLevel = 5, timestamp = now - 7200000L),
            SymptomLog(id = "2", feeling = "Focused", energyLevel = 4, timestamp = now - 3600000L)
        )
    }

    // --- Notification Preferences ---

    fun loadNotificationPreferences(context: Context): NotificationPreferences {
        val prefs = getPrefs(context)
        return NotificationPreferences(
            notificationsEnabled = prefs.getBoolean(KEY_NOTIF_ENABLED, true),
            notifyGoalReached = prefs.getBoolean(KEY_NOTIF_GOAL, true),
            notifyStageMilestones = prefs.getBoolean(KEY_NOTIF_STAGE, true),
            notifyHydration = prefs.getBoolean(KEY_NOTIF_HYDRATION, true),
            hydrationIntervalHours = prefs.getInt(KEY_NOTIF_INTERVAL, 2)
        )
    }

    fun saveNotificationPreferences(context: Context, prefs: NotificationPreferences) {
        getPrefs(context).edit().apply {
            putBoolean(KEY_NOTIF_ENABLED, prefs.notificationsEnabled)
            putBoolean(KEY_NOTIF_GOAL, prefs.notifyGoalReached)
            putBoolean(KEY_NOTIF_STAGE, prefs.notifyStageMilestones)
            putBoolean(KEY_NOTIF_HYDRATION, prefs.notifyHydration)
            putInt(KEY_NOTIF_INTERVAL, prefs.hydrationIntervalHours)
            apply()
        }
    }
}
