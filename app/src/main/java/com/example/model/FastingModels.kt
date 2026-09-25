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
    PLAN_36("36h Monk Fast", 36, 36, 0, "Extended reset for deep digestive rest and metabolic focus."),
    CUSTOM("Custom", 14, 14, 10, "Customizable duration tailored to your personal goals.")
}

enum class MetabolicStage(
    val title: String,
    val rangeText: String,
    val startHour: Float,
    val endHour: Float,
    val darkColor: Color,
    val lightColor: Color,
    val description: String
) {
    FED("Fed State", "0 - 4h", 0f, 4f, StageFedColorDark, StageFedColorLight, "Insulin levels stabilize, blood sugar begins to normalize."),
    EARLY_FAST("Glycogen Burn", "4 - 12h", 4f, 12f, StageEarlyColorDark, StageEarlyColorLight, "Stomach is empty; body draws upon liver glycogen stores."),
    KETOSIS("Ketosis", "12 - 18h", 12f, 18f, StageKetosisColorDark, StageKetosisColorLight, "Fat breakdown increases; ketones produced as an alternative fuel source."),
    DEEP_KETOSIS("Deep Ketosis", "18 - 24h", 18f, 24f, StageDeepKetosisColorDark, StageDeepKetosisColorLight, "Elevated ketone concentration and sustained fat utilization."),
    AUTOPHAGY("Autophagy", "24h+", 24f, 72f, StageAutophagyColorDark, StageAutophagyColorLight, "Intracellular recycling and cellular maintenance processes.");

    val color: Color
        get() = if (ThemeManager.isDarkMode) darkColor else lightColor
}

enum class FastingStyle(
    val title: String,
    val subtitle: String,
    val allowedSummary: String
) {
    CLEAN(
        title = "Clean Fast",
        subtitle = "Zero calories & zero glycemic impact",
        allowedSummary = "Water, black coffee, pure green/black tea, electrolytes & salts only"
    ),
    DIRTY(
        title = "Dirty / Keto Fast",
        subtitle = "Fat-assisted metabolic fast",
        allowedSummary = "MCT oil, keto coffee, splash of heavy cream, bone broth (<50 kcal)"
    )
}

data class FastSession(
    val id: String,
    val plan: FastingPlan,
    val targetHours: Int = plan.targetHours,
    val startTime: Long,
    val endTime: Long,
    val durationSeconds: Long,
    val completedTarget: Boolean,
    val mentalClarity: Int? = null, // 1 to 5
    val energyLevel: Int? = null,   // 1 to 5
    val hungerLevel: Int? = null,   // 1 to 5
    val reflectionNote: String = "",
    val fastingStyle: String = FastingStyle.CLEAN.title
)

data class ElectrolyteProtocol(
    val pinkSalt: Boolean = false,
    val potassiumMagnesium: Boolean = false,
    val blackCoffeeOrTea: Boolean = false,
    val boneBrothOrMineral: Boolean = false,
    // Mineral tracking in milligrams (mg)
    val sodiumMg: Int = 0,
    val potassiumMg: Int = 0,
    val magnesiumMg: Int = 0,
    val targetSodiumMg: Int = 2500,
    val targetPotassiumMg: Int = 1500,
    val targetMagnesiumMg: Int = 400,
    // Fasting hours checklist
    val morningSaltWater: Boolean = false,
    val middayHydration: Boolean = false,
    val middayPotassium: Boolean = false,
    val afternoonSaltBuster: Boolean = false,
    val eveningMagnesium: Boolean = false
) {
    val completedChecklistCount: Int
        get() = (if (morningSaltWater) 1 else 0) +
                (if (middayHydration) 1 else 0) +
                (if (middayPotassium) 1 else 0) +
                (if (afternoonSaltBuster) 1 else 0) +
                (if (eveningMagnesium) 1 else 0) +
                (if (pinkSalt) 1 else 0) +
                (if (potassiumMagnesium) 1 else 0) +
                (if (blackCoffeeOrTea) 1 else 0) +
                (if (boneBrothOrMineral) 1 else 0)
}

data class ZenBadge(
    val id: String,
    val title: String,
    val emoji: String,
    val description: String,
    val requirementText: String,
    val isUnlocked: Boolean,
    val progress: Float,
    val progressText: String,
    val unlockedDate: String? = null
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
    val hydrationIntervalHours: Int = 2,
    val playChimeOnGoal: Boolean = true,
    val zenChimeSound: String = "TIBETAN_BOWL"
)
