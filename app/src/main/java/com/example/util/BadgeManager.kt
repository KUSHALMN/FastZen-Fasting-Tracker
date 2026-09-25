package com.example.util

import com.example.model.ElectrolyteProtocol
import com.example.model.FastSession
import com.example.model.ZenBadge
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object BadgeManager {

    fun calculateBadges(
        sessions: List<FastSession>,
        streakDays: Int,
        currentWaterMl: Int,
        protocol: ElectrolyteProtocol
    ): List<ZenBadge> {
        val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())

        // 1. First Steps (Completed first 16h fast)
        val first16hSession = sessions.filter { it.durationSeconds >= 16 * 3600L }
            .minByOrNull { it.endTime }
        val firstStepsUnlocked = first16hSession != null
        val maxFastHours = (sessions.maxOfOrNull { it.durationSeconds } ?: 0L) / 3600

        val badgeFirstSteps = ZenBadge(
            id = "badge_first_steps",
            title = "First Steps",
            emoji = "🌿",
            description = "Completed your first 16-hour fast. Welcome to intermittent fasting!",
            requirementText = "Complete at least one 16h fast",
            isUnlocked = firstStepsUnlocked,
            progress = if (firstStepsUnlocked) 1f else (maxFastHours.toFloat() / 16f).coerceIn(0f, 1f),
            progressText = if (firstStepsUnlocked) "Completed" else "${maxFastHours}/16 hrs",
            unlockedDate = first16hSession?.let { dateFormat.format(Date(it.endTime)) }
        )

        // 2. Fat Burner (5 fasts reaching ketosis >= 12h)
        val ketosisFastsCount = sessions.count { it.durationSeconds >= 12 * 3600L }
        val fatBurnerUnlocked = ketosisFastsCount >= 5
        val fifthKetosisFast = sessions.filter { it.durationSeconds >= 12 * 3600L }
            .sortedBy { it.endTime }
            .getOrNull(4)

        val badgeFatBurner = ZenBadge(
            id = "badge_fat_burner",
            title = "Fat Burner",
            emoji = "🔥",
            description = "Completed 5 fasts reaching the active Ketosis stage (12h+).",
            requirementText = "Reach ketosis (12h+) in 5 sessions",
            isUnlocked = fatBurnerUnlocked,
            progress = (ketosisFastsCount.toFloat() / 5f).coerceIn(0f, 1f),
            progressText = "$ketosisFastsCount / 5 fasts",
            unlockedDate = fifthKetosisFast?.let { dateFormat.format(Date(it.endTime)) }
        )

        // 3. Cellular Reset (First 24h autophagy fast)
        val first24hSession = sessions.filter { it.durationSeconds >= 24 * 3600L }
            .minByOrNull { it.endTime }
        val cellularResetUnlocked = first24hSession != null

        val badgeCellularReset = ZenBadge(
            id = "badge_cellular_reset",
            title = "Cellular Reset",
            emoji = "🧬",
            description = "Reached the deep Autophagy stage with a 24-hour cellular renewal fast.",
            requirementText = "Complete a 24h+ autophagy fast",
            isUnlocked = cellularResetUnlocked,
            progress = if (cellularResetUnlocked) 1f else (maxFastHours.toFloat() / 24f).coerceIn(0f, 1f),
            progressText = if (cellularResetUnlocked) "Achieved" else "${maxFastHours}/24 hrs",
            unlockedDate = first24hSession?.let { dateFormat.format(Date(it.endTime)) }
        )

        // 4. 7-Day Zen Flow (1-week continuous streak)
        val zenFlowUnlocked = streakDays >= 7

        val badgeZenFlow = ZenBadge(
            id = "badge_zen_flow",
            title = "7-Day Zen Flow",
            emoji = "⚡",
            description = "Maintained a continuous 7-day fasting streak. Habits turned into lifestyle!",
            requirementText = "Reach a 7-day continuous streak",
            isUnlocked = zenFlowUnlocked,
            progress = (streakDays.toFloat() / 7f).coerceIn(0f, 1f),
            progressText = "$streakDays / 7 days",
            unlockedDate = if (zenFlowUnlocked) "Active Streak" else null
        )

        // 5. Hydration Hero (2,500ml water goal)
        val waterUnlocked = currentWaterMl >= 2500
        val badgeHydration = ZenBadge(
            id = "badge_hydration",
            title = "Hydration Master",
            emoji = "💧",
            description = "Reached the optimal daily 2,500 ml water target during fasting.",
            requirementText = "Drink 2,500 ml water in a single day",
            isUnlocked = waterUnlocked,
            progress = (currentWaterMl.toFloat() / 2500f).coerceIn(0f, 1f),
            progressText = "$currentWaterMl / 2500 ml",
            unlockedDate = if (waterUnlocked) "Today" else null
        )

        // 6. Electrolyte Protocol Master
        val protocolCount = (if (protocol.pinkSalt) 1 else 0) +
                (if (protocol.potassiumMagnesium) 1 else 0) +
                (if (protocol.blackCoffeeOrTea) 1 else 0) +
                (if (protocol.boneBrothOrMineral) 1 else 0)
        val protocolUnlocked = protocolCount == 4
        val badgeProtocol = ZenBadge(
            id = "badge_electrolyte",
            title = "Electrolyte Pro",
            emoji = "🧂",
            description = "Logged all 4 essential fasting minerals and appetite modulators.",
            requirementText = "Complete all 4 items on the during-fast checklist",
            isUnlocked = protocolUnlocked,
            progress = (protocolCount.toFloat() / 4f).coerceIn(0f, 1f),
            progressText = "$protocolCount / 4 protocols",
            unlockedDate = if (protocolUnlocked) "Today" else null
        )

        // 7. Zen Master (10 completed fasts)
        val completedFasts = sessions.count { it.completedTarget }
        val zenMasterUnlocked = completedFasts >= 10
        val badgeZenMaster = ZenBadge(
            id = "badge_zen_master",
            title = "Zen Master",
            emoji = "🧘",
            description = "Successfully reached your fasting goal in 10 separate sessions.",
            requirementText = "Complete 10 scheduled fasts",
            isUnlocked = zenMasterUnlocked,
            progress = (completedFasts.toFloat() / 10f).coerceIn(0f, 1f),
            progressText = "$completedFasts / 10 fasts",
            unlockedDate = if (zenMasterUnlocked) "Mastery Unlocked" else null
        )

        return listOf(
            badgeFirstSteps,
            badgeFatBurner,
            badgeCellularReset,
            badgeZenFlow,
            badgeHydration,
            badgeProtocol,
            badgeZenMaster
        )
    }
}
