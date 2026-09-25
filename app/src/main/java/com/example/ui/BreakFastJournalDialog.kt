package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.FastingPlan
import com.example.model.FastingStyle
import com.example.ui.theme.ThemeManager
import com.example.util.ZenSoundHelper

@Composable
fun BreakFastJournalDialog(
    elapsedSeconds: Long,
    targetHours: Int,
    plan: FastingPlan,
    currentFastingStyle: FastingStyle,
    onDismiss: () -> Unit,
    onConfirmSave: (
        mentalClarity: Int,
        energyLevel: Int,
        hungerLevel: Int,
        notes: String,
        style: FastingStyle
    ) -> Unit
) {
    var mentalClarity by remember { mutableIntStateOf(4) }
    var energyLevel by remember { mutableIntStateOf(4) }
    var hungerLevel by remember { mutableIntStateOf(2) }
    var noteText by remember { mutableStateOf("") }
    var selectedStyle by remember { mutableStateOf(currentFastingStyle) }
    var isPlayingSound by remember { mutableStateOf(false) }

    val hours = elapsedSeconds / 3600
    val minutes = (elapsedSeconds % 3600) / 60
    val targetSecs = targetHours * 3600L
    val targetReached = elapsedSeconds >= targetSecs

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight()
                .padding(vertical = 24.dp)
                .testTag("break_fast_journal_dialog"),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (ThemeManager.isDarkMode) 0.5f else 0.8f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Icon & Title
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(
                            if (targetReached) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.secondaryContainer
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (targetReached) "🎉" else "🧘",
                        fontSize = 28.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = if (targetReached) "Fast Completed! 🎉" else "Break Fast & Reflect",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "${hours}h ${minutes}m elapsed • Goal: ${targetHours}h (${plan.title})",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Zen Chime Audio Celebration Bar
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "🔔", fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Zen Singing Bowl",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Centering 432Hz harmonic tone",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        IconButton(
                            onClick = {
                                isPlayingSound = true
                                ZenSoundHelper.playSound(ZenSoundHelper.SoundType.TIBETAN_BOWL)
                            },
                            modifier = Modifier.testTag("preview_zen_bowl_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = "Play Tibetan Bowl",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Fasting Style Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Fasting Style:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FastingStyle.values().forEach { style ->
                            val isSelected = selectedStyle == style
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedStyle = style },
                                label = { Text(style.title, fontSize = 11.sp) },
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
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(16.dp))

                // Rating 1: Mental Clarity
                JournalRatingRow(
                    label = "🧠 Mental Clarity",
                    currentValue = mentalClarity,
                    labels = listOf("Foggy", "Low", "Normal", "Sharp", "Crystal"),
                    onSelect = { mentalClarity = it }
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Rating 2: Energy Level
                JournalRatingRow(
                    label = "⚡ Energy Level",
                    currentValue = energyLevel,
                    labels = listOf("Drained", "Low", "Steady", "High", "Peak"),
                    onSelect = { energyLevel = it }
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Rating 3: Hunger Level
                JournalRatingRow(
                    label = "🍏 Hunger Level",
                    currentValue = hungerLevel,
                    labels = listOf("None", "Mild", "Moderate", "Strong", "Ravenous"),
                    onSelect = { hungerLevel = it }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Reflection Note Field
                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    label = { Text("Daily Fasting Reflection (Optional)") },
                    placeholder = { Text("e.g. Felt great on green tea, high focus all morning...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("journal_note_input"),
                    shape = RoundedCornerShape(14.dp),
                    minLines = 2,
                    maxLines = 4
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons
                Button(
                    onClick = {
                        onConfirmSave(mentalClarity, energyLevel, hungerLevel, noteText, selectedStyle)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("save_journal_and_end_btn"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = "Save")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Save Session & Reflection", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Keep Fasting", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    TextButton(onClick = {
                        onConfirmSave(3, 3, 3, "", selectedStyle)
                    }) {
                        Text("Skip Reflection", color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}

@Composable
private fun JournalRatingRow(
    label: String,
    currentValue: Int,
    labels: List<String>,
    onSelect: (Int) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = labels.getOrElse(currentValue - 1) { "$currentValue/5" },
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            (1..5).forEach { score ->
                val isSelected = score == currentValue
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    border = BorderStroke(
                        1.dp,
                        if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.outlineVariant
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onSelect(score) }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = score.toString(),
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 14.sp,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
