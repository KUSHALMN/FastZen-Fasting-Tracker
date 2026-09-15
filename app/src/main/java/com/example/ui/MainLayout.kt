package com.example.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.FastZenStorage
import com.example.model.FastSession
import com.example.model.FastingPlan
import com.example.model.NotificationPreferences
import com.example.model.SymptomLog
import com.example.model.WeightEntry
import com.example.ui.theme.ThemeManager
import com.example.util.NotificationHelper
import kotlinx.coroutines.delay
import java.util.*

enum class FastZenTab(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
) {
    TIMER("Timer", Icons.Filled.Timer, Icons.Outlined.Timer, "nav_timer"),
    TRACKING("Track", Icons.Filled.WaterDrop, Icons.Outlined.WaterDrop, "nav_tracking"),
    FAQS("Guides", Icons.AutoMirrored.Filled.MenuBook, Icons.AutoMirrored.Outlined.MenuBook, "nav_faqs"),
    HISTORY("History", Icons.Filled.DateRange, Icons.Outlined.DateRange, "nav_history"),
    SETTINGS("Settings", Icons.Filled.Settings, Icons.Outlined.Settings, "nav_settings")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainLayout() {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(FastZenTab.TIMER) }

    // Fasting Plan & Custom Target loaded from persistent local storage
    var selectedPlan by remember { mutableStateOf(FastZenStorage.getSelectedPlan(context)) }
    var customHours by remember { mutableIntStateOf(FastZenStorage.getCustomHours(context)) }
    var activeTargetHours by remember { mutableIntStateOf(FastZenStorage.getActiveTargetHours(context)) }

    // Fasting state loaded from persistent local storage
    var isFasting by remember { mutableStateOf(FastZenStorage.isFasting(context)) }
    var fastStartTime by remember { mutableStateOf(FastZenStorage.getFastStartTime(context)) }
    var elapsedSeconds by remember {
        mutableStateOf(
            if (isFasting && fastStartTime > 0)
                ((System.currentTimeMillis() - fastStartTime) / 1000L).coerceAtLeast(0L)
            else 0L
        )
    }

    // Notification Preferences
    var notificationPrefs by remember { mutableStateOf(FastZenStorage.loadNotificationPreferences(context)) }

    // Notification Milestone Flags
    var goalAlertSent by remember { mutableStateOf(FastZenStorage.isGoalAlertSent(context)) }
    var ketosisAlertSent by remember { mutableStateOf(FastZenStorage.isKetosisAlertSent(context)) }
    var autophagyAlertSent by remember { mutableStateOf(FastZenStorage.isAutophagyAlertSent(context)) }

    // Hydration state with automatic midnight reset
    var currentWaterMl by remember { mutableIntStateOf(FastZenStorage.getWaterMl(context)) }

    // Weight entries
    val weightEntries = remember {
        mutableStateListOf<WeightEntry>().apply {
            addAll(FastZenStorage.loadWeightEntries(context))
        }
    }

    // Symptoms
    val symptomLogs = remember {
        mutableStateListOf<SymptomLog>().apply {
            addAll(FastZenStorage.loadSymptomLogs(context))
        }
    }

    // Fasting history
    val sessions = remember {
        mutableStateListOf<FastSession>().apply {
            addAll(FastZenStorage.loadSessions(context))
        }
    }

    var streakDays by remember { mutableIntStateOf(FastZenStorage.getStreakDays(context)) }

    // Live timer ticking
    LaunchedEffect(isFasting, fastStartTime) {
        if (isFasting) {
            while (true) {
                elapsedSeconds = ((System.currentTimeMillis() - fastStartTime) / 1000L).coerceAtLeast(0L)

                // Trigger Goal reached alert if enabled
                if (notificationPrefs.notificationsEnabled && notificationPrefs.notifyGoalReached && !goalAlertSent) {
                    if (elapsedSeconds >= activeTargetHours * 3600L) {
                        NotificationHelper.sendGoalReachedNotification(context, activeTargetHours)
                        goalAlertSent = true
                        FastZenStorage.saveAlertFlags(context, goalAlertSent, ketosisAlertSent, autophagyAlertSent)
                    }
                }

                // Trigger Ketosis Milestone alert (12 hours)
                if (notificationPrefs.notificationsEnabled && notificationPrefs.notifyStageMilestones && !ketosisAlertSent) {
                    if (elapsedSeconds >= 12 * 3600L) {
                        NotificationHelper.sendStageReachedNotification(
                            context,
                            "Ketosis",
                            "Your body is actively utilizing stored fat for ketones & energy."
                        )
                        ketosisAlertSent = true
                        FastZenStorage.saveAlertFlags(context, goalAlertSent, ketosisAlertSent, autophagyAlertSent)
                    }
                }

                // Trigger Autophagy Milestone alert (24 hours)
                if (notificationPrefs.notificationsEnabled && notificationPrefs.notifyStageMilestones && !autophagyAlertSent) {
                    if (elapsedSeconds >= 24 * 3600L) {
                        NotificationHelper.sendStageReachedNotification(
                            context,
                            "Autophagy",
                            "Cellular recycling and mitochondrial renewal are now in high gear."
                        )
                        autophagyAlertSent = true
                        FastZenStorage.saveAlertFlags(context, goalAlertSent, ketosisAlertSent, autophagyAlertSent)
                    }
                }

                delay(1000L)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.SelfImprovement,
                            contentDescription = "FastZen Logo Icon",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (selectedTab == FastZenTab.TIMER) "FastZen" else selectedTab.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { selectedTab = FastZenTab.FAQS },
                        modifier = Modifier.testTag("appbar_faq_action")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                            contentDescription = "Fasting FAQs & Guides",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(
                        onClick = { ThemeManager.toggleDarkMode() },
                        modifier = Modifier.testTag("appbar_theme_toggle")
                    ) {
                        Icon(
                            imageVector = if (ThemeManager.isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Toggle Dark/Light Mode",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Persistent AdMob / Sponsored Ad Banner
                AdBanner()

                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    FastZenTab.values().forEach { tab ->
                        val isSelected = selectedTab == tab
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { selectedTab = tab },
                            icon = {
                                Icon(
                                    imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                    contentDescription = tab.title
                                )
                            },
                            label = { Text(tab.title) },
                            modifier = Modifier.testTag(tab.testTag),
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                FastZenTab.TIMER -> {
                    TimerScreen(
                        selectedPlan = selectedPlan,
                        onSelectPlan = { plan ->
                            selectedPlan = plan
                            if (plan != FastingPlan.CUSTOM) {
                                activeTargetHours = plan.targetHours
                            }
                        },
                        customHours = customHours,
                        onUpdateCustomHours = { hours ->
                            customHours = hours
                            if (selectedPlan == FastingPlan.CUSTOM) {
                                activeTargetHours = hours
                            }
                            FastZenStorage.saveFastingState(context, isFasting, fastStartTime, activeTargetHours, selectedPlan, hours)
                        },
                        activeTargetHours = activeTargetHours,
                        onUpdateActiveTargetHours = {
                            activeTargetHours = it
                            FastZenStorage.saveFastingState(context, isFasting, fastStartTime, it, selectedPlan, customHours)
                        },
                        isFasting = isFasting,
                        fastStartTime = fastStartTime,
                        onAdjustStartTime = { newStartTime ->
                            fastStartTime = newStartTime
                            elapsedSeconds = ((System.currentTimeMillis() - newStartTime) / 1000L).coerceAtLeast(0L)
                            FastZenStorage.saveFastingState(context, isFasting, newStartTime, activeTargetHours, selectedPlan, customHours)
                        },
                        elapsedSeconds = elapsedSeconds,
                        onStartFast = {
                            val now = System.currentTimeMillis()
                            fastStartTime = now
                            elapsedSeconds = 0L
                            isFasting = true
                            goalAlertSent = false
                            ketosisAlertSent = false
                            autophagyAlertSent = false
                            FastZenStorage.saveFastingState(context, true, now, activeTargetHours, selectedPlan, customHours)
                            FastZenStorage.saveAlertFlags(context, false, false, false)
                        },
                        onEndFast = {
                            if (elapsedSeconds > 10) {
                                val targetSecs = activeTargetHours * 3600L
                                val newSession = FastSession(
                                    id = UUID.randomUUID().toString(),
                                    plan = selectedPlan,
                                    targetHours = activeTargetHours,
                                    startTime = fastStartTime,
                                    endTime = System.currentTimeMillis(),
                                    durationSeconds = elapsedSeconds,
                                    completedTarget = elapsedSeconds >= targetSecs
                                )
                                sessions.add(0, newSession)
                                FastZenStorage.saveSessions(context, sessions)
                                streakDays += 1
                                FastZenStorage.saveStreakDays(context, streakDays)
                            }
                            isFasting = false
                            elapsedSeconds = 0L
                            fastStartTime = 0L
                            goalAlertSent = false
                            ketosisAlertSent = false
                            autophagyAlertSent = false
                            FastZenStorage.saveFastingState(context, false, 0L, activeTargetHours, selectedPlan, customHours)
                            FastZenStorage.saveAlertFlags(context, false, false, false)
                        },
                        currentWaterMl = currentWaterMl,
                        onAddWater = {
                            currentWaterMl = (currentWaterMl + it).coerceAtMost(5000)
                            FastZenStorage.saveWaterMl(context, currentWaterMl)
                        },
                        onNavigateToFaq = { selectedTab = FastZenTab.FAQS }
                    )
                }
                FastZenTab.TRACKING -> {
                    TrackingScreen(
                        currentWaterMl = currentWaterMl,
                        onAddWater = {
                            currentWaterMl = (currentWaterMl + it).coerceAtMost(5000)
                            FastZenStorage.saveWaterMl(context, currentWaterMl)
                        },
                        onResetWater = {
                            currentWaterMl = 0
                            FastZenStorage.saveWaterMl(context, 0)
                        },
                        weightEntries = weightEntries,
                        onAddWeight = { kg ->
                            val newEntry = WeightEntry(
                                id = UUID.randomUUID().toString(),
                                weightKg = kg,
                                timestamp = System.currentTimeMillis()
                            )
                            weightEntries.add(0, newEntry)
                            FastZenStorage.saveWeightEntries(context, weightEntries)
                        },
                        symptomLogs = symptomLogs,
                        onAddSymptom = { feeling, energy ->
                            val newLog = SymptomLog(
                                id = UUID.randomUUID().toString(),
                                feeling = feeling,
                                energyLevel = energy,
                                timestamp = System.currentTimeMillis()
                            )
                            symptomLogs.add(0, newLog)
                            FastZenStorage.saveSymptomLogs(context, symptomLogs)
                        }
                    )
                }
                FastZenTab.FAQS -> {
                    FaqScreen()
                }
                FastZenTab.HISTORY -> {
                    HistoryScreen(
                        sessions = sessions,
                        streakDays = streakDays
                    )
                }
                FastZenTab.SETTINGS -> {
                    SettingsScreen(
                        currentPlan = selectedPlan,
                        onSelectPlan = { plan ->
                            selectedPlan = plan
                            if (plan != FastingPlan.CUSTOM) {
                                activeTargetHours = plan.targetHours
                            } else {
                                activeTargetHours = customHours
                            }
                            FastZenStorage.saveFastingState(context, isFasting, fastStartTime, activeTargetHours, plan, customHours)
                        },
                        customHours = customHours,
                        onUpdateCustomHours = { hours ->
                            customHours = hours
                            if (selectedPlan == FastingPlan.CUSTOM) {
                                activeTargetHours = hours
                            }
                            FastZenStorage.saveFastingState(context, isFasting, fastStartTime, activeTargetHours, selectedPlan, hours)
                        },
                        notificationPrefs = notificationPrefs,
                        onUpdateNotificationPrefs = {
                            notificationPrefs = it
                            FastZenStorage.saveNotificationPreferences(context, it)
                        }
                    )
                }
            }
        }
    }
}
