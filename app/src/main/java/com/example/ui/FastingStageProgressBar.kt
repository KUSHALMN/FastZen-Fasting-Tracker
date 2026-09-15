package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.MetabolicStage
import com.example.ui.theme.ThemeManager

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun FastingStageProgressBar(
    elapsedHours: Float,
    currentStage: MetabolicStage,
    modifier: Modifier = Modifier
) {
    val animatedStageColor by animateColorAsState(
        targetValue = currentStage.color,
        animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing),
        label = "progress_stage_color"
    )

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Metabolic Stages",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(9.dp)
                        .clip(CircleShape)
                        .background(animatedStageColor)
                )
                Spacer(modifier = Modifier.width(6.dp))

                AnimatedContent(
                    targetState = currentStage,
                    transitionSpec = {
                        (slideInVertically { it / 2 } + fadeIn(tween(400)))
                            .togetherWith(slideOutVertically { -it / 2 } + fadeOut(tween(300)))
                    },
                    label = "progress_stage_name_anim"
                ) { stage ->
                    Text(
                        text = stage.title,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = animatedStageColor
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Multi-segment stage track
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp)),
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            val stages = remember { MetabolicStage.values() }
            for (stage in stages) {
                val isActive = elapsedHours >= stage.startHour
                val weight = when (stage) {
                    MetabolicStage.FED -> 4f
                    MetabolicStage.EARLY_FAST -> 8f
                    MetabolicStage.KETOSIS -> 6f
                    MetabolicStage.DEEP_KETOSIS -> 6f
                    MetabolicStage.AUTOPHAGY -> 12f
                }
                StageTrackSegment(
                    stage = stage,
                    isActive = isActive,
                    weight = weight,
                    modifier = Modifier.weight(weight)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Stage labels below
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "0h", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = "4h", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = "12h", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = "18h", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = "24h+", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun StageTrackSegment(
    stage: MetabolicStage,
    isActive: Boolean,
    weight: Float,
    modifier: Modifier = Modifier
) {
    val inactiveColor = if (ThemeManager.isDarkMode) stage.color.copy(alpha = 0.20f) else MaterialTheme.colorScheme.surfaceVariant
    val segmentColor by animateColorAsState(
        targetValue = if (isActive) stage.color else inactiveColor,
        animationSpec = tween(durationMillis = 500),
        label = "segment_color_${stage.name}"
    )

    Box(
        modifier = modifier
            .fillMaxHeight()
            .background(segmentColor)
    )
}
