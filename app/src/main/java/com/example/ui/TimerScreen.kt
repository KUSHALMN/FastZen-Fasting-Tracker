package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.FastingPlan
import com.example.model.MetabolicStage
import com.example.ui.theme.ThemeManager
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalAnimationApi::class)
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

    // Smooth progress sweep
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 750, easing = FastOutSlowInEasing),
        label = "fast_progress"
    )

    val currentStage = when {
        elapsedHours >= 24f -> MetabolicStage.AUTOPHAGY
        elapsedHours >= 18f -> MetabolicStage.DEEP_KETOSIS
        elapsedHours >= 12f -> MetabolicStage.KETOSIS
        elapsedHours >= 4f -> MetabolicStage.EARLY_FAST
        else -> MetabolicStage.FED
    }

    // Smooth stage color morphing
    val animatedStageColor by animateColorAsState(
        targetValue = currentStage.color,
        animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing),
        label = "stage_color_transition"
    )

    val animatedStageBgColor by animateColorAsState(
        targetValue = currentStage.color.copy(alpha = 0.16f),
        animationSpec = tween(durationMillis = 700),
        label = "stage_bg_transition"
    )

    // Breathing pulse animation for active fast dial
    val infiniteTransition = rememberInfiniteTransition(label = "ambient_fasting_pulse")
    val breathScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2800, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breath_scale"
    )
    val breathAlpha by infiniteTransition.animateFloat(
        initialValue = 0.08f,
        targetValue = 0.20f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2800, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breath_alpha"
    )

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

                val plans = remember { FastingPlan.values() }
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(plans, key = { it.name }, contentType = { "plan_chip" }) { plan ->
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
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                containerColor = MaterialTheme.colorScheme.surface,
                                labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = MaterialTheme.colorScheme.outlineVariant,
                                selectedBorderColor = MaterialTheme.colorScheme.primary,
                                borderWidth = 1.dp
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
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
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

        // Circular Timer Card with Subtle Breathing & Stage Transitions
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (ThemeManager.isDarkMode) 0.5f else 0.8f)),
                elevation = CardDefaults.cardElevation(defaultElevation = if (ThemeManager.isDarkMode) 0.dp else 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Circular Dial Container
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(240.dp)
                    ) {
                        // Subtle Ambient Breathing Halo behind the dial (graphicsLayer draws on render thread with zero recomposition)
                        if (isFasting) {
                            Box(
                                modifier = Modifier
                                    .size(230.dp)
                                    .graphicsLayer {
                                        scaleX = breathScale
                                        scaleY = breathScale
                                        alpha = breathAlpha
                                    }
                                    .clip(CircleShape)
                                    .background(animatedStageColor)
                            )
                        }

                        val trackColor = MaterialTheme.colorScheme.surfaceVariant
                        val primaryColor = MaterialTheme.colorScheme.primary
                        val sweepGradientBrush = remember(primaryColor, animatedStageColor) {
                            Brush.sweepGradient(listOf(primaryColor, animatedStageColor, primaryColor))
                        }

                        Canvas(modifier = Modifier.fillMaxSize().padding(14.dp)) {
                            val strokeWidthPx = 16.dp.toPx()

                            // Background static track
                            drawArc(
                                color = trackColor,
                                startAngle = -90f,
                                sweepAngle = 360f,
                                useCenter = false,
                                style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
                            )

                            // Animated Progress arc with dynamic stage color gradient
                            if (isFasting && animatedProgress > 0f) {
                                val sweep = 360f * animatedProgress
                                drawArc(
                                    brush = sweepGradientBrush,
                                    startAngle = -90f,
                                    sweepAngle = sweep,
                                    useCenter = false,
                                    style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
                                )

                                // Glowing indicator bead at the tip of the sweep
                                val angleRad = Math.toRadians((-90f + sweep).toDouble())
                                val arcRadius = (size.minDimension - strokeWidthPx) / 2f
                                val centerX = size.width / 2f
                                val centerY = size.height / 2f
                                val beadX = centerX + (arcRadius * cos(angleRad)).toFloat()
                                val beadY = centerY + (arcRadius * sin(angleRad)).toFloat()

                                drawCircle(
                                    color = animatedStageColor.copy(alpha = 0.4f),
                                    radius = 12.dp.toPx(),
                                    center = Offset(beadX, beadY)
                                )
                                drawCircle(
                                    color = if (ThemeManager.isDarkMode) Color.White else primaryColor,
                                    radius = 6.dp.toPx(),
                                    center = Offset(beadX, beadY)
                                )
                            }
                        }

                        // Center Info with animated stage badge and countdown
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = if (isFasting) "FASTING" else "READY",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = animatedStageColor,
                                letterSpacing = 2.sp
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            // Large Digital Timer Display
                            Text(
                                text = formatDurationClock(if (isFasting) elapsedSeconds else 0L),
                                fontSize = 32.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            // Animated Remaining / Beyond Target text
                            AnimatedContent(
                                targetState = isTargetReached,
                                transitionSpec = {
                                    fadeIn(tween(400)) togetherWith fadeOut(tween(300))
                                },
                                label = "target_text_transition"
                            ) { reached ->
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
                                    color = if (reached) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = if (reached) FontWeight.SemiBold else FontWeight.Normal
                                )
                            }

                            if (isFasting) {
                                Spacer(modifier = Modifier.height(10.dp))

                                // Subtle Animated Stage Badge Transition
                                AnimatedContent(
                                    targetState = currentStage,
                                    transitionSpec = {
                                        (slideInVertically { it / 2 } + fadeIn(tween(400)))
                                            .togetherWith(slideOutVertically { -it / 2 } + fadeOut(tween(300)))
                                    },
                                    label = "stage_badge_transition"
                                ) { stage ->
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = animatedStageBgColor
                                    ) {
                                        Text(
                                            text = stage.title,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = animatedStageColor,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                        )
                                    }
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
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (ThemeManager.isDarkMode) 0.5f else 0.8f)),
                elevation = CardDefaults.cardElevation(defaultElevation = if (ThemeManager.isDarkMode) 0.dp else 1.dp)
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
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (ThemeManager.isDarkMode) 0.5f else 0.8f)),
                elevation = CardDefaults.cardElevation(defaultElevation = if (ThemeManager.isDarkMode) 0.dp else 1.dp)
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
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.HelpOutline,
                            contentDescription = "Fasting FAQ Help",
                            tint = MaterialTheme.colorScheme.primary
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

        // Metabolic Biological Explanation Card with smooth animated transition
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (ThemeManager.isDarkMode) 0.5f else 0.8f)),
                elevation = CardDefaults.cardElevation(defaultElevation = if (ThemeManager.isDarkMode) 0.dp else 1.dp)
            ) {
                AnimatedContent(
                    targetState = currentStage,
                    transitionSpec = {
                        (fadeIn(tween(450)) + slideInVertically { it / 6 })
                            .togetherWith(fadeOut(tween(250)) + slideOutVertically { -it / 6 })
                    },
                    label = "stage_card_explanation_transition"
                ) { stage ->
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
                                .background(stage.color.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Science,
                                contentDescription = "Metabolic Stage Science Icon",
                                tint = stage.color
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = stage.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "(${stage.rangeText})",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = stage.description,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 18.sp
                            )
                        }
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
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (ThemeManager.isDarkMode) 0.5f else 0.8f)),
                elevation = CardDefaults.cardElevation(defaultElevation = if (ThemeManager.isDarkMode) 0.dp else 1.dp)
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
