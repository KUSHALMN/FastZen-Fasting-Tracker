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
    private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"

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
    private const val KEY_NOTIF_CHIME = "notif_chime"
    private const val KEY_NOTIF_SOUND = "notif_sound"

    // Fasting Style & Electrolyte Protocol
    private const val KEY_FASTING_STYLE = "fasting_style"
    private const val KEY_ELECTROLYTE_DAY = "electrolyte_day"
    private const val KEY_ELECTROLYTE_SALT = "electrolyte_salt"
    private const val KEY_ELECTROLYTE_KM = "electrolyte_km"
    private const val KEY_ELECTROLYTE_COFFEE = "electrolyte_coffee"
    private const val KEY_ELECTROLYTE_BROTH = "electrolyte_broth"
    private const val KEY_ELECTROLYTE_SODIUM_MG = "electrolyte_sodium_mg"
    private const val KEY_ELECTROLYTE_POTASSIUM_MG = "electrolyte_potassium_mg"
    private const val KEY_ELECTROLYTE_MAGNESIUM_MG = "electrolyte_magnesium_mg"
    private const val KEY_ELECTROLYTE_CH_MORNING = "electrolyte_ch_morning"
    private const val KEY_ELECTROLYTE_CH_MID_HYDR = "electrolyte_ch_mid_hydr"
    private const val KEY_ELECTROLYTE_CH_MID_POT = "electrolyte_ch_mid_pot"
    private const val KEY_ELECTROLYTE_CH_AFT_SALT = "electrolyte_ch_aft_salt"
    private const val KEY_ELECTROLYTE_CH_EVE_MAG = "electrolyte_ch_eve_mag"

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

    fun hasCompletedOnboarding(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_ONBOARDING_COMPLETED, false)
    }

    fun setOnboardingCompleted(context: Context, completed: Boolean = true) {
        getPrefs(context).edit().putBoolean(KEY_ONBOARDING_COMPLETED, completed).apply()
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

    fun getStreakDays(context: Context): Int = getPrefs(context).getInt(KEY_STREAK_DAYS, 0)
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
            prefs.getInt(KEY_WATER_ML, 0)
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
        val jsonString = getPrefs(context).getString(KEY_SESSIONS_JSON, null) ?: return emptyList()
        return try {
            val jsonArray = JSONArray(jsonString)
            val list = mutableListOf<FastSession>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val planName = obj.optString("plan", FastingPlan.PLAN_16_8.name)
                val plan = try { FastingPlan.valueOf(planName) } catch (_: Exception) { FastingPlan.PLAN_16_8 }
                val clarity = if (obj.has("mentalClarity") && !obj.isNull("mentalClarity")) obj.getInt("mentalClarity") else null
                val energy = if (obj.has("energyLevel") && !obj.isNull("energyLevel")) obj.getInt("energyLevel") else null
                val hunger = if (obj.has("hungerLevel") && !obj.isNull("hungerLevel")) obj.getInt("hungerLevel") else null
                val note = obj.optString("reflectionNote", "")
                val style = obj.optString("fastingStyle", FastingStyle.CLEAN.title)

                list.add(
                    FastSession(
                        id = obj.getString("id"),
                        plan = plan,
                        targetHours = obj.optInt("targetHours", 16),
                        startTime = obj.getLong("startTime"),
                        endTime = obj.getLong("endTime"),
                        durationSeconds = obj.getLong("durationSeconds"),
                        completedTarget = obj.getBoolean("completedTarget"),
                        mentalClarity = clarity,
                        energyLevel = energy,
                        hungerLevel = hunger,
                        reflectionNote = note,
                        fastingStyle = style
                    )
                )
            }
            list
        } catch (_: Exception) {
            emptyList()
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
                    if (session.mentalClarity != null) put("mentalClarity", session.mentalClarity)
                    if (session.energyLevel != null) put("energyLevel", session.energyLevel)
                    if (session.hungerLevel != null) put("hungerLevel", session.hungerLevel)
                    put("reflectionNote", session.reflectionNote)
                    put("fastingStyle", session.fastingStyle)
                }
                jsonArray.put(obj)
            }
            getPrefs(context).edit().putString(KEY_SESSIONS_JSON, jsonArray.toString()).apply()
        } catch (_: Exception) {}
    }

    // --- Weight Entries ---

    fun loadWeightEntries(context: Context): List<WeightEntry> {
        val jsonString = getPrefs(context).getString(KEY_WEIGHT_JSON, null) ?: return emptyList()
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
            list
        } catch (_: Exception) {
            emptyList()
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

    // --- Symptom Logs ---

    fun loadSymptomLogs(context: Context): List<SymptomLog> {
        val jsonString = getPrefs(context).getString(KEY_SYMPTOMS_JSON, null) ?: return emptyList()
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
            list
        } catch (_: Exception) {
            emptyList()
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

    // --- Notification Preferences ---

    fun loadNotificationPreferences(context: Context): NotificationPreferences {
        val prefs = getPrefs(context)
        return NotificationPreferences(
            notificationsEnabled = prefs.getBoolean(KEY_NOTIF_ENABLED, true),
            notifyGoalReached = prefs.getBoolean(KEY_NOTIF_GOAL, true),
            notifyStageMilestones = prefs.getBoolean(KEY_NOTIF_STAGE, true),
            notifyHydration = prefs.getBoolean(KEY_NOTIF_HYDRATION, true),
            hydrationIntervalHours = prefs.getInt(KEY_NOTIF_INTERVAL, 2),
            playChimeOnGoal = prefs.getBoolean(KEY_NOTIF_CHIME, true),
            zenChimeSound = prefs.getString(KEY_NOTIF_SOUND, "TIBETAN_BOWL") ?: "TIBETAN_BOWL"
        )
    }

    fun saveNotificationPreferences(context: Context, prefs: NotificationPreferences) {
        getPrefs(context).edit().apply {
            putBoolean(KEY_NOTIF_ENABLED, prefs.notificationsEnabled)
            putBoolean(KEY_NOTIF_GOAL, prefs.notifyGoalReached)
            putBoolean(KEY_NOTIF_STAGE, prefs.notifyStageMilestones)
            putBoolean(KEY_NOTIF_HYDRATION, prefs.notifyHydration)
            putInt(KEY_NOTIF_INTERVAL, prefs.hydrationIntervalHours)
            putBoolean(KEY_NOTIF_CHIME, prefs.playChimeOnGoal)
            putString(KEY_NOTIF_SOUND, prefs.zenChimeSound)
            apply()
        }
    }

    // --- Fasting Style (Clean vs Dirty) ---

    fun loadFastingStyle(context: Context): FastingStyle {
        val name = getPrefs(context).getString(KEY_FASTING_STYLE, FastingStyle.CLEAN.name)
        return try {
            FastingStyle.valueOf(name ?: FastingStyle.CLEAN.name)
        } catch (_: Exception) {
            FastingStyle.CLEAN
        }
    }

    fun saveFastingStyle(context: Context, style: FastingStyle) {
        getPrefs(context).edit().putString(KEY_FASTING_STYLE, style.name).apply()
    }

    // --- Electrolyte & Hydration Protocol Checklist ---

    fun loadElectrolyteProtocol(context: Context): ElectrolyteProtocol {
        val prefs = getPrefs(context)
        val currentDay = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        val savedDay = prefs.getInt(KEY_ELECTROLYTE_DAY, -1)

        return if (savedDay != currentDay) {
            // Reset for new day
            ElectrolyteProtocol()
        } else {
            ElectrolyteProtocol(
                pinkSalt = prefs.getBoolean(KEY_ELECTROLYTE_SALT, false),
                potassiumMagnesium = prefs.getBoolean(KEY_ELECTROLYTE_KM, false),
                blackCoffeeOrTea = prefs.getBoolean(KEY_ELECTROLYTE_COFFEE, false),
                boneBrothOrMineral = prefs.getBoolean(KEY_ELECTROLYTE_BROTH, false),
                sodiumMg = prefs.getInt(KEY_ELECTROLYTE_SODIUM_MG, 0),
                potassiumMg = prefs.getInt(KEY_ELECTROLYTE_POTASSIUM_MG, 0),
                magnesiumMg = prefs.getInt(KEY_ELECTROLYTE_MAGNESIUM_MG, 0),
                morningSaltWater = prefs.getBoolean(KEY_ELECTROLYTE_CH_MORNING, false),
                middayHydration = prefs.getBoolean(KEY_ELECTROLYTE_CH_MID_HYDR, false),
                middayPotassium = prefs.getBoolean(KEY_ELECTROLYTE_CH_MID_POT, false),
                afternoonSaltBuster = prefs.getBoolean(KEY_ELECTROLYTE_CH_AFT_SALT, false),
                eveningMagnesium = prefs.getBoolean(KEY_ELECTROLYTE_CH_EVE_MAG, false)
            )
        }
    }

    fun saveElectrolyteProtocol(context: Context, protocol: ElectrolyteProtocol) {
        val currentDay = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        getPrefs(context).edit().apply {
            putInt(KEY_ELECTROLYTE_DAY, currentDay)
            putBoolean(KEY_ELECTROLYTE_SALT, protocol.pinkSalt)
            putBoolean(KEY_ELECTROLYTE_KM, protocol.potassiumMagnesium)
            putBoolean(KEY_ELECTROLYTE_COFFEE, protocol.blackCoffeeOrTea)
            putBoolean(KEY_ELECTROLYTE_BROTH, protocol.boneBrothOrMineral)
            putInt(KEY_ELECTROLYTE_SODIUM_MG, protocol.sodiumMg)
            putInt(KEY_ELECTROLYTE_POTASSIUM_MG, protocol.potassiumMg)
            putInt(KEY_ELECTROLYTE_MAGNESIUM_MG, protocol.magnesiumMg)
            putBoolean(KEY_ELECTROLYTE_CH_MORNING, protocol.morningSaltWater)
            putBoolean(KEY_ELECTROLYTE_CH_MID_HYDR, protocol.middayHydration)
            putBoolean(KEY_ELECTROLYTE_CH_MID_POT, protocol.middayPotassium)
            putBoolean(KEY_ELECTROLYTE_CH_AFT_SALT, protocol.afternoonSaltBuster)
            putBoolean(KEY_ELECTROLYTE_CH_EVE_MAG, protocol.eveningMagnesium)
            apply()
        }
    }
}
