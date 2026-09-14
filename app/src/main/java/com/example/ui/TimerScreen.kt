package com.example.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.FastingPlan
import com.example.model.MetabolicStage
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

@Composable
fun TimerScreen(
    selectedPlan: FastingPlan,
    onSelectPlan: (FastingPlan) -> Unit,
    customHours: Int,
    onUpdateCustomHours: (Int) -> Unit,
    activeTargetHours: Int,
    onUpdateActiveTargetHours: (Int) -> Unit,
    isFasting: Boolean,
    fastStartTime: Long,
    onAdjustStartTime: (Long) -> Unit,
    elapsedSeconds: Long,
    onStartFast: () -> Unit,
    onEndFast: () -> Unit,
    currentWaterMl: Int,
    onAddWater: (Int) -> Unit,
    onNavigateToFaq: () -> Unit
) {
    var showCustomDurationDialog by remember { mutableStateOf(false) }
    var showAdjustStartTimeDialog by remember { mutableStateOf(false) }

    val elapsedHours = elapsedSeconds.toFloat() / 3600f
    val targetSeconds = activeTargetHours * 3600L
    val isTargetReached = isFasting && elapsedSeconds >= targetSeconds

    val progress = if (isFasting && targetSeconds > 0) {
        (elapsedSeconds.toFloat() / targetSeconds.toFloat()).coerceIn(0f, 1f)
    } else 0f

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(600),
        label = "fast_progress"
    )

    val currentStage = when {
        elapsedHours >= 24f -> MetabolicStage.AUTOPHAGY
        elapsedHours >= 18f -> MetabolicStage.DEEP_KETOSIS
        elapsedHours >= 12f -> MetabolicStage.KETOSIS
        elapsedHours >= 4f -> MetabolicStage.EARLY_FAST
        else -> MetabolicStage.FED
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 96.dp)
    ) {
        // Fasting Plan Protocols Selector Row
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Fasting Protocol",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    TextButton(
                        onClick = { showCustomDurationDialog = true },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Custom Target", modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Custom Timer", fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(FastingPlan.values()) { plan ->
                        val isSelected = plan == selectedPlan
                        val chipText = if (plan == FastingPlan.CUSTOM) "Custom (${customHours}h)" else plan.title

                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                if (plan == FastingPlan.CUSTOM) {
                                    onSelectPlan(plan)
                                    onUpdateActiveTargetHours(customHours)
                                    showCustomDurationDialog = true
                                } else {
                                    onSelectPlan(plan)
                                    onUpdateActiveTargetHours(plan.targetHours)
                                }
                            },
                            label = {
                                Text(
                                    text = chipText,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.testTag("plan_chip_${plan.name}")
                        )
                    }
                }
            }
        }

        // Goal Reached Celebration Banner
        if (isTargetReached) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = "Target reached trophy",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Target Goal Achieved! 🎉",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "You passed ${activeTargetHours}h! You can end your fast or keep burning fat.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }
        }

        // Circular Timer Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Circular Dial
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(240.dp)
                    ) {
                        val trackColor = MaterialTheme.colorScheme.surfaceVariant
                        val primaryColor = MaterialTheme.colorScheme.primary
                        val stageColor = currentStage.color

                        Canvas(modifier = Modifier.fillMaxSize().padding(14.dp)) {
                            // Background track
                            drawArc(
                                color = trackColor,
                                startAngle = -90f,
                                sweepAngle = 360f,
                                useCenter = false,
                                style = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round)
                            )
                            // Progress arc with stage gradient
                            if (isFasting && animatedProgress > 0f) {
                                drawArc(
                                    brush = Brush.sweepGradient(
                                        listOf(primaryColor, stageColor, primaryColor)
                                    ),
                                    startAngle = -90f,
                                    sweepAngle = 360f * animatedProgress,
                                    useCenter = false,
                                    style = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round)
                                )
                            }
                        }

                        // Center Info
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = if (isFasting) "FASTING" else "READY",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 2.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = formatDurationClock(if (isFasting) elapsedSeconds else 0L),
                                fontSize = 32.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (isFasting) {
                                    val remaining = (targetSeconds - elapsedSeconds)
                                    if (remaining > 0) {
                                        "${formatDurationClock(remaining)} remaining"
                                    } else {
                                        "+${formatDurationClock(-remaining)} beyond target"
                                    }
                                } else {
                                    "Target: $activeTargetHours hours"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            if (isFasting) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = currentStage.color.copy(alpha = 0.18f)
                                ) {
                                    Text(
                                        text = currentStage.title,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = currentStage.color,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Start Time & Target Controls
                    if (isFasting) {
                        val timeFormat = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(
                                onClick = { showAdjustStartTimeDialog = true },
                                modifier = Modifier.testTag("adjust_start_time_btn")
                            ) {
                                Icon(Icons.Default.AccessTime, contentDescription = "Edit Start Time", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Started: ${timeFormat.format(Date(fastStartTime))}", fontSize = 12.sp)
                            }

                            TextButton(
                                onClick = { showCustomDurationDialog = true },
                                modifier = Modifier.testTag("edit_target_hours_btn")
                            ) {
                                Icon(Icons.Default.Flag, contentDescription = "Edit Target", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Target: ${activeTargetHours}h", fontSize = 12.sp)
                            }
                        }

                        // Quick +/- Target Hours Buttons during fasting
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = {
                                    if (activeTargetHours > 1) {
                                        onUpdateActiveTargetHours(activeTargetHours - 1)
                                    }
                                },
                                modifier = Modifier.height(36.dp),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Text("-1 Hour", fontSize = 12.sp)
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            OutlinedButton(
                                onClick = {
                                    onUpdateActiveTargetHours(activeTargetHours + 1)
                                },
                                modifier = Modifier.height(36.dp),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Text("+1 Hour", fontSize = 12.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    // Primary Action Button (Start / End)
                    if (!isFasting) {
                        Button(
                            onClick = onStartFast,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("start_fast_button"),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Start Fast")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Start ${activeTargetHours}h Fast",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        Button(
                            onClick = onEndFast,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("end_fast_button"),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isTargetReached) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                            )
                        ) {
                            Icon(if (isTargetReached) Icons.Default.CheckCircle else Icons.Default.Stop, contentDescription = "End Fast")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isTargetReached) "Complete & Save Fast" else "End Fast Early",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Fasting Stage Progress Bar Segment
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    FastingStageProgressBar(
                        elapsedHours = elapsedHours,
                        currentStage = currentStage
                    )
                }
            }
        }

        // Fasting Knowledge & FAQ Spotlight Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToFaq() },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF8B5CF6).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.HelpOutline,
                            contentDescription = "Fasting FAQ Help",
                            tint = Color(0xFF8B5CF6)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Fasting Tips & FAQs",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Can I drink black coffee? How to tame hunger? Explore answers →",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Navigate to FAQs",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Metabolic Biological Explanation Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(currentStage.color.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Science,
                            contentDescription = "Metabolic Stage Science Icon",
                            tint = currentStage.color
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = currentStage.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "(${currentStage.rangeText})",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = currentStage.description,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }

        // Quick Water Hydration Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.WaterDrop,
                            contentDescription = "Hydration Icon",
                            tint = Color(0xFF0284C7),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Hydration Today",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "$currentWaterMl / 2500 ml",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Button(
                        onClick = { onAddWater(250) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("log_water_quick_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Water", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("+250 ml")
                    }
                }
            }
        }
    }

    // Dialog: Custom Fast Duration Picker
    if (showCustomDurationDialog) {
        var tempDuration by remember { mutableStateOf(activeTargetHours) }
        AlertDialog(
            onDismissRequest = { showCustomDurationDialog = false },
            title = {
                Text(
                    text = "Custom Fast Target",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "$tempDuration Hours",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Customize your target fasting duration",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Slider(
                        value = tempDuration.toFloat(),
                        onValueChange = { tempDuration = it.toInt() },
                        valueRange = 1f..72f,
                        steps = 70,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Preset Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(12, 14, 16, 18, 20, 24, 36).forEach { h ->
                            FilterChip(
                                selected = tempDuration == h,
                                onClick = { tempDuration = h },
                                label = { Text("${h}h", fontSize = 11.sp) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdateCustomHours(tempDuration)
                        onUpdateActiveTargetHours(tempDuration)
                        showCustomDurationDialog = false
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Apply Target")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomDurationDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Dialog: Adjust Start Time
    if (showAdjustStartTimeDialog) {
        var hoursAgo by remember { mutableStateOf(1) }
        AlertDialog(
            onDismissRequest = { showAdjustStartTimeDialog = false },
            title = {
                Text(
                    text = "Adjust Fast Start Time",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Forgot to start the timer? Adjust when you finished your last meal.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Started $hoursAgo hour(s) ago",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Slider(
                        value = hoursAgo.toFloat(),
                        onValueChange = { hoursAgo = it.toInt() },
                        valueRange = 1f..24f,
                        steps = 22,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Quick buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(1, 2, 4, 8, 12).forEach { offsetH ->
                            OutlinedButton(
                                onClick = { hoursAgo = offsetH },
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(2.dp)
                            ) {
                                Text("${offsetH}h ago", fontSize = 11.sp)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val newStartTime = System.currentTimeMillis() - (hoursAgo * 3600000L)
                        onAdjustStartTime(newStartTime)
                        showAdjustStartTimeDialog = false
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Update Start Time")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAdjustStartTimeDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

fun formatDurationClock(totalSeconds: Long): String {
    val hours = TimeUnit.SECONDS.toHours(totalSeconds)
    val minutes = TimeUnit.SECONDS.toMinutes(totalSeconds) % 60
    val seconds = totalSeconds % 60
    return String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)
}
