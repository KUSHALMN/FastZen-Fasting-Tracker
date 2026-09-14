package com.example.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
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
    FAQS("Guides", Icons.Filled.MenuBook, Icons.Outlined.MenuBook, "nav_faqs"),
    HISTORY("History", Icons.Filled.DateRange, Icons.Outlined.DateRange, "nav_history"),
    SETTINGS("Settings", Icons.Filled.Settings, Icons.Outlined.Settings, "nav_settings")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainLayout() {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(FastZenTab.TIMER) }

    // Fasting Plan & Custom Target
    var selectedPlan by remember { mutableStateOf(FastingPlan.PLAN_16_8) }
    var customHours by remember { mutableIntStateOf(14) }
    var activeTargetHours by remember { mutableIntStateOf(16) }

    // Fasting state
    var isFasting by remember { mutableStateOf(false) }
    var fastStartTime by remember { mutableStateOf(0L) }
    var elapsedSeconds by remember { mutableStateOf(0L) }

    // Notification Preferences
    var notificationPrefs by remember { mutableStateOf(NotificationPreferences()) }

    // Notification Milestone Flags
    var goalAlertSent by remember { mutableStateOf(false) }
    var ketosisAlertSent by remember { mutableStateOf(false) }
    var autophagyAlertSent by remember { mutableStateOf(false) }

    // Hydration state
    var currentWaterMl by remember { mutableIntStateOf(1500) }

    // Weight entries
    val weightEntries = remember {
        mutableStateListOf(
            WeightEntry(id = "1", weightKg = 74.2f, timestamp = System.currentTimeMillis() - 86400000L * 3),
            WeightEntry(id = "2", weightKg = 73.8f, timestamp = System.currentTimeMillis() - 86400000L * 1),
            WeightEntry(id = "3", weightKg = 73.4f, timestamp = System.currentTimeMillis())
        )
    }

    // Symptoms
    val symptomLogs = remember {
        mutableStateListOf(
            SymptomLog(id = "1", feeling = "Energized", energyLevel = 5, timestamp = System.currentTimeMillis() - 7200000L),
            SymptomLog(id = "2", feeling = "Focused", energyLevel = 4, timestamp = System.currentTimeMillis() - 3600000L)
        )
    }

    // Fasting history
    val sessions = remember {
        mutableStateListOf(
            FastSession(
                id = "1",
                plan = FastingPlan.PLAN_16_8,
                targetHours = 16,
                startTime = System.currentTimeMillis() - 86400000L - (16 * 3600000L + 1800000L),
                endTime = System.currentTimeMillis() - 86400000L,
                durationSeconds = 16 * 3600L + 1800L,
                completedTarget = true
            ),
            FastSession(
                id = "2",
                plan = FastingPlan.PLAN_18_6,
                targetHours = 18,
                startTime = System.currentTimeMillis() - (86400000L * 2) - (18 * 3600000L),
                endTime = System.currentTimeMillis() - (86400000L * 2),
                durationSeconds = 18 * 3600L + 600L,
                completedTarget = true
            ),
            FastSession(
                id = "3",
                plan = FastingPlan.PLAN_16_8,
                targetHours = 16,
                startTime = System.currentTimeMillis() - (86400000L * 3) - (15 * 3600000L),
                endTime = System.currentTimeMillis() - (86400000L * 3),
                durationSeconds = 15 * 3600L,
                completedTarget = false
            )
        )
    }

    var streakDays by remember { mutableIntStateOf(6) }

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
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { selectedTab = FastZenTab.FAQS },
                        modifier = Modifier.testTag("appbar_faq_action")
                    ) {
                        Icon(
                            imageVector = Icons.Default.HelpOutline,
                            contentDescription = "Fasting FAQs & Guides",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    IconButton(
                        onClick = { ThemeManager.toggleDarkMode() },
                        modifier = Modifier.testTag("appbar_theme_toggle")
                    ) {
                        Icon(
                            imageVector = if (ThemeManager.isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Toggle Dark/Light Mode",
                            tint = MaterialTheme.colorScheme.onBackground
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
                        },
                        activeTargetHours = activeTargetHours,
                        onUpdateActiveTargetHours = { activeTargetHours = it },
                        isFasting = isFasting,
                        fastStartTime = fastStartTime,
                        onAdjustStartTime = { newStartTime ->
                            fastStartTime = newStartTime
                            elapsedSeconds = ((System.currentTimeMillis() - newStartTime) / 1000L).coerceAtLeast(0L)
                        },
                        elapsedSeconds = elapsedSeconds,
                        onStartFast = {
                            fastStartTime = System.currentTimeMillis()
                            elapsedSeconds = 0L
                            isFasting = true
                            goalAlertSent = false
                            ketosisAlertSent = false
                            autophagyAlertSent = false
                        },
                        onEndFast = {
                            if (elapsedSeconds > 10) {
                                val targetSecs = activeTargetHours * 3600L
                                sessions.add(
                                    0,
                                    FastSession(
                                        id = UUID.randomUUID().toString(),
                                        plan = selectedPlan,
                                        targetHours = activeTargetHours,
                                        startTime = fastStartTime,
                                        endTime = System.currentTimeMillis(),
                                        durationSeconds = elapsedSeconds,
                                        completedTarget = elapsedSeconds >= targetSecs
                                    )
                                )
                                streakDays += 1
                            }
                            isFasting = false
                            elapsedSeconds = 0L
                            goalAlertSent = false
                            ketosisAlertSent = false
                            autophagyAlertSent = false
                        },
                        currentWaterMl = currentWaterMl,
                        onAddWater = { currentWaterMl = (currentWaterMl + it).coerceAtMost(5000) },
                        onNavigateToFaq = { selectedTab = FastZenTab.FAQS }
                    )
                }
                FastZenTab.TRACKING -> {
                    TrackingScreen(
                        currentWaterMl = currentWaterMl,
                        onAddWater = { currentWaterMl = (currentWaterMl + it).coerceAtMost(5000) },
                        onResetWater = { currentWaterMl = 0 },
                        weightEntries = weightEntries,
                        onAddWeight = { kg ->
                            weightEntries.add(
                                0,
                                WeightEntry(
                                    id = UUID.randomUUID().toString(),
                                    weightKg = kg,
                                    timestamp = System.currentTimeMillis()
                                )
                            )
                        },
                        symptomLogs = symptomLogs,
                        onAddSymptom = { feeling, energy ->
                            symptomLogs.add(
                                0,
                                SymptomLog(
                                    id = UUID.randomUUID().toString(),
                                    feeling = feeling,
                                    energyLevel = energy,
                                    timestamp = System.currentTimeMillis()
                                )
                            )
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
                        },
                        customHours = customHours,
                        onUpdateCustomHours = { hours ->
                            customHours = hours
                            if (selectedPlan == FastingPlan.CUSTOM) {
                                activeTargetHours = hours
                            }
                        },
                        notificationPrefs = notificationPrefs,
                        onUpdateNotificationPrefs = { notificationPrefs = it }
                    )
                }
            }
        }
    }
}
