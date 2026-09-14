package com.example.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.*

enum class FastingPlan(
    val title: String,
    val targetHours: Int,
    val fastingHours: Int,
    val eatingHours: Int,
    val description: String
) {
    PLAN_16_8("16:8 LeanGains", 16, 16, 8, "Most popular daily rhythm. Perfect for fat burning & mental clarity."),
    PLAN_18_6("18:6 Accelerated", 18, 18, 6, "Enhanced autophagy & accelerated ketosis for experienced fasters."),
    PLAN_20_4("20:4 The Warrior", 20, 20, 4, "4-hour eating window with profound cellular renewal."),
    PLAN_24("24h OMAD", 24, 24, 0, "One Meal A Day. Deep metabolic reset & maximum autophagy."),
    PLAN_36("36h Monk Fast", 36, 36, 0, "Extended reset for deep cellular repair and immune regeneration."),
    CUSTOM("Custom", 14, 14, 10, "Customizable duration tailored to your personal goals.")
}

enum class MetabolicStage(
    val title: String,
    val rangeText: String,
    val startHour: Float,
    val endHour: Float,
    val color: Color,
    val description: String
) {
    FED("Fed State", "0 - 4h", 0f, 4f, StageFedColor, "Insulin levels drop, blood sugar begins to normalize."),
    EARLY_FAST("Glycogen Burn", "4 - 12h", 4f, 12f, StageEarlyColor, "Stomach is empty; body burns liver glycogen stores."),
    KETOSIS("Ketosis", "12 - 18h", 12f, 18f, StageKetosisColor, "Fat breakdown accelerates, ketones produced for clean brain fuel."),
    DEEP_KETOSIS("Deep Ketosis", "18 - 24h", 18f, 24f, StageDeepKetosisColor, "High ketone concentration, reduced inflammation, accelerated fat loss."),
    AUTOPHAGY("Autophagy", "24h+", 24f, 72f, StageAutophagyColor, "Cellular cleaning, damaged protein recycling, mitochondrial renewal.")
}

data class FastSession(
    val id: String,
    val plan: FastingPlan,
    val targetHours: Int = plan.targetHours,
    val startTime: Long,
    val endTime: Long,
    val durationSeconds: Long,
    val completedTarget: Boolean
)

data class WaterEntry(
    val id: String,
    val amountMl: Int,
    val timestamp: Long
)

data class WeightEntry(
    val id: String,
    val weightKg: Float,
    val timestamp: Long,
    val note: String = ""
)

data class SymptomLog(
    val id: String,
    val feeling: String,
    val energyLevel: Int, // 1 to 5
    val timestamp: Long
)

data class FastingFaqItem(
    val id: String,
    val category: String,
    val question: String,
    val answer: String,
    val keyTakeaway: String = ""
)

data class NotificationPreferences(
    val notificationsEnabled: Boolean = true,
    val notifyGoalReached: Boolean = true,
    val notifyStageMilestones: Boolean = true,
    val notifyHydration: Boolean = true,
    val hydrationIntervalHours: Int = 2
)
