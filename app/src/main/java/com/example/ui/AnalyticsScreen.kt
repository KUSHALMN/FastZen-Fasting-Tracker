package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.FastSession
import com.example.model.MetabolicStage
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AnalyticsScreen(
    sessions: List<FastSession>,
    streakDays: Int
) {
    val totalSeconds = sessions.sumOf { it.durationSeconds }
    val totalHours = totalSeconds / 3600f

    // Accurate hours spent in each metabolic stage across all recorded sessions
    val ketosisHours = sessions.sumOf { session ->
        val durH = session.durationSeconds / 3600f
        (durH - 12f).coerceIn(0f, 6f).toDouble()
    }.toFloat()
    val deepKetosisHours = sessions.sumOf { session ->
        val durH = session.durationSeconds / 3600f
        (durH - 18f).coerceIn(0f, 6f).toDouble()
    }.toFloat()
    val autophagyHours = sessions.sumOf { session ->
        val durH = session.durationSeconds / 3600f
        (durH - 24f).coerceAtLeast(0f).toDouble()
    }.toFloat()

    val past7DaysData = remember(sessions) {
        val cal = Calendar.getInstance()
        (6 downTo 0).map { daysAgo ->
            val dayCal = Calendar.getInstance().apply {
                timeInMillis = cal.timeInMillis
                add(Calendar.DAY_OF_YEAR, -daysAgo)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val startOfDay = dayCal.timeInMillis
            val endOfDay = startOfDay + 86400000L - 1
            val dayLabel = SimpleDateFormat("EEE", Locale.getDefault()).format(dayCal.time)
            val dayHours = sessions
                .filter { it.endTime in startOfDay..endOfDay }
                .sumOf { it.durationSeconds }.toFloat() / 3600f
            dayLabel to dayHours
        }
    }
    val daysOfWeek = past7DaysData.map { it.first }
    val pastWeekHours = past7DaysData.map { it.second }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 96.dp)
    ) {
        // Streak Banner Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF97316).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = "Streak flame icon",
                            tint = Color(0xFFF97316),
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "$streakDays Day Fasting Streak!",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "You are in the top 10% of consistent fasters this month.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // 7-Day Fasting Duration Bar Visualizer
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        text = "Weekly Consistency",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Hours fasted per day (Target 16h)",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        daysOfWeek.forEachIndexed { index, day ->
                            val hours = pastWeekHours.getOrElse(index) { 0f }
                            val heightFraction = if (hours > 0f) (hours / 24f).coerceIn(0.12f, 1f) else 0.04f

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Bottom,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = if (hours > 0f) "${hours.toInt()}h" else "-",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (hours >= 16f) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .width(20.dp)
                                        .fillMaxHeight(heightFraction)
                                        .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                        .background(
                                            if (hours >= 16f) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.surfaceVariant
                                        )
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = day,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // Metabolic Zone Distribution Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        text = "Metabolic Zone Breakdown",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Estimated cumulative hours in biological stages",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    MetabolicZoneRow(
                        stage = MetabolicStage.KETOSIS,
                        hours = ketosisHours,
                        note = "Active Fat Oxidation"
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    MetabolicZoneRow(
                        stage = MetabolicStage.DEEP_KETOSIS,
                        hours = deepKetosisHours,
                        note = "Mitochondrial Energy Peak"
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    MetabolicZoneRow(
                        stage = MetabolicStage.AUTOPHAGY,
                        hours = autophagyHours,
                        note = "Cellular Cleanup & Renewal"
                    )
                }
            }
        }
    }
}

@Composable
fun MetabolicZoneRow(
    stage: MetabolicStage,
    hours: Float,
    note: String
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(stage.color)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = stage.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = note,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Text(
                text = String.format(java.util.Locale.US, "%.1f hrs", hours),
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = stage.color
            )
        }
    }
}
