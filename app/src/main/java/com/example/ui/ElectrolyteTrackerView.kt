package com.example.ui

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ElectrolyteProtocol
import com.example.ui.theme.ThemeManager

@Composable
fun ElectrolyteTrackerView(
    electrolyteProtocol: ElectrolyteProtocol,
    onUpdateElectrolyteProtocol: (ElectrolyteProtocol) -> Unit,
    currentWaterMl: Int,
    onAddWater: (Int) -> Unit,
    onResetWater: () -> Unit
) {
    val context = LocalContext.current
    var showCustomDoseDialog by remember { mutableStateOf<String?>(null) } // "SODIUM", "POTASSIUM", "MAGNESIUM"
    var showRecipeDetails by remember { mutableStateOf(false) }
    var showSymptomAdvisor by remember { mutableStateOf(false) }
    var showResetConfirmDialog by remember { mutableStateOf(false) }

    // Calculate balance scores
    val sodiumPercent = (electrolyteProtocol.sodiumMg.toFloat() / electrolyteProtocol.targetSodiumMg.toFloat()).coerceIn(0f, 1f)
    val potassiumPercent = (electrolyteProtocol.potassiumMg.toFloat() / electrolyteProtocol.targetPotassiumMg.toFloat()).coerceIn(0f, 1f)
    val magnesiumPercent = (electrolyteProtocol.magnesiumMg.toFloat() / electrolyteProtocol.targetMagnesiumMg.toFloat()).coerceIn(0f, 1f)
    val waterPercent = (currentWaterMl.toFloat() / 2500f).coerceIn(0f, 1f)
    val overallBalancePercent = ((sodiumPercent + potassiumPercent + magnesiumPercent + waterPercent) / 4f * 100f).toInt()

    // Determine status advisory
    val statusText = when {
        overallBalancePercent >= 80 -> "Optimal Mineral Equilibrium 🌿"
        electrolyteProtocol.sodiumMg < 600 -> "Low Sodium: Take Pink Himalayan Salt 🧂"
        electrolyteProtocol.potassiumMg < 400 -> "Low Potassium: Replenish with Minerals ⚡"
        electrolyteProtocol.magnesiumMg < 150 -> "Magnesium Needed: Evening Dose 🌙"
        currentWaterMl < 1000 -> "Dehydration Alert: Drink More Water 💧"
        else -> "Good Progress: Continue Sipping Mindfully 🧘"
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Overall Mineral & Hydration Hero Summary Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("electrolyte_summary_card"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (ThemeManager.isDarkMode) 0.5f else 0.8f)),
            elevation = CardDefaults.cardElevation(defaultElevation = if (ThemeManager.isDarkMode) 0.dp else 1.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE11D48).copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🧂", fontSize = 22.sp)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Electrolytes & Hydration",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Daily Fasting Equilibrium",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (overallBalancePercent >= 75) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = "$overallBalancePercent% Met",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (overallBalancePercent >= 75) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Status banner
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = statusText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 4 mini status metrics
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MiniMineralPill(
                        label = "Sodium (Na)",
                        value = "${electrolyteProtocol.sodiumMg}mg",
                        percent = sodiumPercent,
                        color = Color(0xFFE11D48),
                        modifier = Modifier.weight(1f)
                    )
                    MiniMineralPill(
                        label = "Potassium (K)",
                        value = "${electrolyteProtocol.potassiumMg}mg",
                        percent = potassiumPercent,
                        color = Color(0xFFD97706),
                        modifier = Modifier.weight(1f)
                    )
                    MiniMineralPill(
                        label = "Magnesium (Mg)",
                        value = "${electrolyteProtocol.magnesiumMg}mg",
                        percent = magnesiumPercent,
                        color = Color(0xFF7C3AED),
                        modifier = Modifier.weight(1f)
                    )
                    MiniMineralPill(
                        label = "Water (H2O)",
                        value = "${currentWaterMl}ml",
                        percent = waterPercent,
                        color = Color(0xFF0284C7),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 2. Mineral Cards: Salt / Sodium, Potassium, Magnesium, and Water
        Text(
            text = "Track Essential Fasting Minerals",
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 4.dp)
        )

        // CARD A: Salt / Sodium (Na+)
        MineralIntakeCard(
            title = "Salt / Sodium (Na+)",
            subtitle = "Target: ${electrolyteProtocol.targetSodiumMg} mg / day",
            emoji = "🧂",
            badgeColor = Color(0xFFE11D48),
            currentAmount = electrolyteProtocol.sodiumMg,
            targetAmount = electrolyteProtocol.targetSodiumMg,
            unit = "mg",
            scientificInsight = "When insulin falls during fasting, kidneys purge sodium rapidly. Salt maintains blood volume, preventing headaches, dizziness upon standing, and fatigue.",
            quickDoseOptions = listOf(
                250 to "+250mg Pinch",
                500 to "+500mg 1/4 tsp",
                1000 to "+1000mg 1/2 tsp"
            ),
            onAddDose = { dose ->
                val newSodium = (electrolyteProtocol.sodiumMg + dose).coerceAtLeast(0)
                onUpdateElectrolyteProtocol(
                    electrolyteProtocol.copy(
                        sodiumMg = newSodium,
                        pinkSalt = newSodium >= 500
                    )
                )
                Toast.makeText(context, "+$dose mg Sodium recorded", Toast.LENGTH_SHORT).show()
            },
            onOpenCustom = { showCustomDoseDialog = "SODIUM" },
            onUndo = {
                val newSodium = (electrolyteProtocol.sodiumMg - 250).coerceAtLeast(0)
                onUpdateElectrolyteProtocol(electrolyteProtocol.copy(sodiumMg = newSodium))
            },
            testTagPrefix = "sodium"
        )

        // CARD B: Potassium (K+)
        MineralIntakeCard(
            title = "Potassium (K+)",
            subtitle = "Target: ${electrolyteProtocol.targetPotassiumMg} mg / day",
            emoji = "⚡",
            badgeColor = Color(0xFFD97706),
            currentAmount = electrolyteProtocol.potassiumMg,
            targetAmount = electrolyteProtocol.targetPotassiumMg,
            unit = "mg",
            scientificInsight = "Intracellular mineral vital for steady heart rhythm, nerve conduction, and muscle power. Use potassium chloride (NoSalt/Lite-Salt) or electrolyte drops.",
            quickDoseOptions = listOf(
                150 to "+150mg Drops",
                300 to "+300mg 1/8 tsp",
                600 to "+600mg 1/4 tsp"
            ),
            onAddDose = { dose ->
                val newPotassium = (electrolyteProtocol.potassiumMg + dose).coerceAtLeast(0)
                onUpdateElectrolyteProtocol(
                    electrolyteProtocol.copy(
                        potassiumMg = newPotassium,
                        potassiumMagnesium = newPotassium >= 300 || electrolyteProtocol.magnesiumMg >= 100
                    )
                )
                Toast.makeText(context, "+$dose mg Potassium recorded", Toast.LENGTH_SHORT).show()
            },
            onOpenCustom = { showCustomDoseDialog = "POTASSIUM" },
            onUndo = {
                val newPotassium = (electrolyteProtocol.potassiumMg - 150).coerceAtLeast(0)
                onUpdateElectrolyteProtocol(electrolyteProtocol.copy(potassiumMg = newPotassium))
            },
            testTagPrefix = "potassium"
        )

        // CARD C: Magnesium (Mg2+)
        MineralIntakeCard(
            title = "Magnesium (Mg2+)",
            subtitle = "Target: ${electrolyteProtocol.targetMagnesiumMg} mg / day",
            emoji = "🌙",
            badgeColor = Color(0xFF7C3AED),
            currentAmount = electrolyteProtocol.magnesiumMg,
            targetAmount = electrolyteProtocol.targetMagnesiumMg,
            unit = "mg",
            scientificInsight = "Cofactor in 300+ enzymes and cellular ATP creation. Glycinate or Malate forms soothe the nervous system, prevent muscle cramps, and deepen delta-wave sleep.",
            quickDoseOptions = listOf(
                100 to "+100mg Malate",
                200 to "+200mg Glycinate",
                400 to "+400mg Night Dose"
            ),
            onAddDose = { dose ->
                val newMagnesium = (electrolyteProtocol.magnesiumMg + dose).coerceAtLeast(0)
                onUpdateElectrolyteProtocol(
                    electrolyteProtocol.copy(
                        magnesiumMg = newMagnesium,
                        potassiumMagnesium = newMagnesium >= 200 || electrolyteProtocol.potassiumMg >= 300
                    )
                )
                Toast.makeText(context, "+$dose mg Magnesium recorded", Toast.LENGTH_SHORT).show()
            },
            onOpenCustom = { showCustomDoseDialog = "MAGNESIUM" },
            onUndo = {
                val newMagnesium = (electrolyteProtocol.magnesiumMg - 100).coerceAtLeast(0)
                onUpdateElectrolyteProtocol(electrolyteProtocol.copy(magnesiumMg = newMagnesium))
            },
            testTagPrefix = "magnesium"
        )

        // CARD D: Pure Hydration (Water)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("water_card"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (ThemeManager.isDarkMode) 0.5f else 0.8f)),
            elevation = CardDefaults.cardElevation(defaultElevation = if (ThemeManager.isDarkMode) 0.dp else 1.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF0284C7).copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.WaterDrop, contentDescription = null, tint = Color(0xFF0284C7), modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Pure Water Hydration",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Target: 2,500 ml / day",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Text(
                        text = "$currentWaterMl / 2500 ml",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        color = Color(0xFF0284C7)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                LinearProgressIndicator(
                    progress = { waterPercent },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = Color(0xFF0284C7),
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { onAddWater(250) },
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .testTag("water_add_250"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("+250ml", fontSize = 12.sp)
                    }
                    Button(
                        onClick = { onAddWater(500) },
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .testTag("water_add_500"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("+500ml", fontSize = 12.sp)
                    }
                    Button(
                        onClick = { onAddWater(750) },
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .testTag("water_add_750"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("+750ml", fontSize = 12.sp)
                    }
                    OutlinedButton(
                        onClick = onResetWater,
                        modifier = Modifier
                            .height(40.dp)
                            .testTag("water_reset"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Reset Water", modifier = Modifier.size(16.dp))
                    }
                }
            }
        }

        // 3. Chronological Fasting Hours Checklist Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("fasting_hours_checklist_card"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (ThemeManager.isDarkMode) 0.5f else 0.8f)),
            elevation = CardDefaults.cardElevation(defaultElevation = if (ThemeManager.isDarkMode) 0.dp else 1.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                val checklistItemsTotal = 5
                val checklistItemsDone = (if (electrolyteProtocol.morningSaltWater) 1 else 0) +
                        (if (electrolyteProtocol.middayHydration) 1 else 0) +
                        (if (electrolyteProtocol.blackCoffeeOrTea) 1 else 0) +
                        (if (electrolyteProtocol.afternoonSaltBuster) 1 else 0) +
                        (if (electrolyteProtocol.eveningMagnesium) 1 else 0)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Checklist, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Fasting Hours Protocol",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Chronological mineral checklist",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (checklistItemsDone == checklistItemsTotal) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = "$checklistItemsDone/$checklistItemsTotal Done",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = if (checklistItemsDone == checklistItemsTotal) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                LinearProgressIndicator(
                    progress = { (checklistItemsDone.toFloat() / checklistItemsTotal.toFloat()).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Item 1: Morning Rise (Hour 0 - 4)
                TimedChecklistItem(
                    timeWindow = "Hour 0 - 4 • Morning Awakening",
                    title = "Wake-up Himalayan Salt Sole",
                    description = "500ml warm water + 1/4 tsp Pink Salt (~500mg Sodium). Prevents morning blood pressure drop & brain fog.",
                    isChecked = electrolyteProtocol.morningSaltWater,
                    onCheckedChange = { checked ->
                        onUpdateElectrolyteProtocol(
                            electrolyteProtocol.copy(
                                morningSaltWater = checked,
                                pinkSalt = checked || electrolyteProtocol.pinkSalt,
                                sodiumMg = if (checked && electrolyteProtocol.sodiumMg == 0) 500 else electrolyteProtocol.sodiumMg
                            )
                        )
                        if (checked && currentWaterMl < 500) onAddWater(500)
                    },
                    quickLogLabel = "+500mg Na & Water",
                    onQuickLog = {
                        onUpdateElectrolyteProtocol(
                            electrolyteProtocol.copy(
                                morningSaltWater = true,
                                pinkSalt = true,
                                sodiumMg = electrolyteProtocol.sodiumMg + 500
                            )
                        )
                        onAddWater(500)
                        Toast.makeText(context, "+500mg Sodium and +500ml Water logged!", Toast.LENGTH_SHORT).show()
                    },
                    testTag = "checklist_morning_salt"
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Item 2: Mid-day Ketosis (Hour 4 - 10)
                TimedChecklistItem(
                    timeWindow = "Hour 4 - 10 • Ketosis & Deep Focus",
                    title = "Midday Potassium Infusion",
                    description = "500ml water + 300mg Potassium chloride (NoSalt) or mineral drops. Maintains cellular hydration & stops limb fatigue.",
                    isChecked = electrolyteProtocol.middayHydration,
                    onCheckedChange = { checked ->
                        onUpdateElectrolyteProtocol(
                            electrolyteProtocol.copy(
                                middayHydration = checked,
                                middayPotassium = checked,
                                potassiumMagnesium = checked || electrolyteProtocol.potassiumMagnesium,
                                potassiumMg = if (checked && electrolyteProtocol.potassiumMg == 0) 300 else electrolyteProtocol.potassiumMg
                            )
                        )
                    },
                    quickLogLabel = "+300mg K & Water",
                    onQuickLog = {
                        onUpdateElectrolyteProtocol(
                            electrolyteProtocol.copy(
                                middayHydration = true,
                                middayPotassium = true,
                                potassiumMagnesium = true,
                                potassiumMg = electrolyteProtocol.potassiumMg + 300
                            )
                        )
                        onAddWater(250)
                        Toast.makeText(context, "+300mg Potassium and +250ml Water logged!", Toast.LENGTH_SHORT).show()
                    },
                    testTag = "checklist_midday_potassium"
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Item 3: Coffee / Tea AMPK Booster (Hour 6 - 12)
                TimedChecklistItem(
                    timeWindow = "Hour 6 - 12 • Autophagy Boost",
                    title = "Black Coffee or Pure Green Tea",
                    description = "Polyphenols upregulate autophagy and suppress ghrelin (hunger). Zero calories, 100% fasting approved.",
                    isChecked = electrolyteProtocol.blackCoffeeOrTea,
                    onCheckedChange = { checked ->
                        onUpdateElectrolyteProtocol(electrolyteProtocol.copy(blackCoffeeOrTea = checked))
                    },
                    quickLogLabel = null,
                    onQuickLog = null,
                    testTag = "checklist_coffee_tea"
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Item 4: Afternoon Hunger Buster (Hour 10 - 16)
                TimedChecklistItem(
                    timeWindow = "Hour 10 - 16 • Hunger Wave Tamer",
                    title = "Sublingual Salt or Warm Salt Water",
                    description = "Place a pinch of coarse sea salt crystals under tongue with 250ml water. Instantly shuts down fake hunger cravings.",
                    isChecked = electrolyteProtocol.afternoonSaltBuster,
                    onCheckedChange = { checked ->
                        onUpdateElectrolyteProtocol(
                            electrolyteProtocol.copy(
                                afternoonSaltBuster = checked,
                                sodiumMg = if (checked && electrolyteProtocol.sodiumMg < 250) 250 else electrolyteProtocol.sodiumMg
                            )
                        )
                    },
                    quickLogLabel = "+250mg Salt",
                    onQuickLog = {
                        onUpdateElectrolyteProtocol(
                            electrolyteProtocol.copy(
                                afternoonSaltBuster = true,
                                sodiumMg = electrolyteProtocol.sodiumMg + 250
                            )
                        )
                        Toast.makeText(context, "+250mg Sodium logged!", Toast.LENGTH_SHORT).show()
                    },
                    testTag = "checklist_afternoon_salt"
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Item 5: Evening Deep Repair (Hour 16+ / Pre-Bed)
                TimedChecklistItem(
                    timeWindow = "Hour 16+ • Cellular Rest & Sleep",
                    title = "Nighttime Magnesium Glycinate",
                    description = "300–400mg elemental Magnesium 1 hour before sleep. Stops nocturnal calf/foot cramps and induces deep restorative sleep.",
                    isChecked = electrolyteProtocol.eveningMagnesium,
                    onCheckedChange = { checked ->
                        onUpdateElectrolyteProtocol(
                            electrolyteProtocol.copy(
                                eveningMagnesium = checked,
                                potassiumMagnesium = checked || electrolyteProtocol.potassiumMagnesium,
                                magnesiumMg = if (checked && electrolyteProtocol.magnesiumMg == 0) 300 else electrolyteProtocol.magnesiumMg
                            )
                        )
                    },
                    quickLogLabel = "+300mg Mg",
                    onQuickLog = {
                        onUpdateElectrolyteProtocol(
                            electrolyteProtocol.copy(
                                eveningMagnesium = true,
                                potassiumMagnesium = true,
                                magnesiumMg = electrolyteProtocol.magnesiumMg + 300
                            )
                        )
                        Toast.makeText(context, "+300mg Magnesium logged!", Toast.LENGTH_SHORT).show()
                    },
                    testTag = "checklist_evening_magnesium"
                )
            }
        }

        // 4. Interactive "Listen to Your Body" Mineral Deficiency Advisor
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("symptom_advisor_card"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (ThemeManager.isDarkMode) 0.5f else 0.8f)),
            elevation = CardDefaults.cardElevation(defaultElevation = if (ThemeManager.isDarkMode) 0.dp else 1.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showSymptomAdvisor = !showSymptomAdvisor },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF59E0B).copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🩺", fontSize = 18.sp)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Listen to Your Body",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Mineral deficiency symptom guide",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Icon(
                        imageVector = if (showSymptomAdvisor) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Expand Advisor",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                AnimatedVisibility(visible = showSymptomAdvisor) {
                    Column(modifier = Modifier.padding(top = 16.dp)) {
                        Text(
                            text = "Experiencing any of these during your fast?",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        SymptomRemedyRow(
                            symptom = "Headache or Lightheadedness",
                            cause = "Low Sodium (Salt)",
                            remedy = "Drink 250ml warm water with 1/2 tsp Pink Salt.",
                            actionLabel = "+500mg Salt",
                            onAction = {
                                onUpdateElectrolyteProtocol(
                                    electrolyteProtocol.copy(
                                        sodiumMg = electrolyteProtocol.sodiumMg + 500,
                                        pinkSalt = true
                                    )
                                )
                                onAddWater(250)
                                Toast.makeText(context, "+500mg Sodium & water logged!", Toast.LENGTH_SHORT).show()
                            }
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        SymptomRemedyRow(
                            symptom = "Muscle Twitches or Leg Cramps",
                            cause = "Low Magnesium",
                            remedy = "Take 200-400mg Magnesium Glycinate.",
                            actionLabel = "+200mg Mg",
                            onAction = {
                                onUpdateElectrolyteProtocol(
                                    electrolyteProtocol.copy(
                                        magnesiumMg = electrolyteProtocol.magnesiumMg + 200,
                                        potassiumMagnesium = true
                                    )
                                )
                                Toast.makeText(context, "+200mg Magnesium logged!", Toast.LENGTH_SHORT).show()
                            }
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        SymptomRemedyRow(
                            symptom = "Heart Flutter or Heavy Legs",
                            cause = "Low Potassium",
                            remedy = "Sip 300mg Potassium chloride in water slowly.",
                            actionLabel = "+300mg K",
                            onAction = {
                                onUpdateElectrolyteProtocol(
                                    electrolyteProtocol.copy(
                                        potassiumMg = electrolyteProtocol.potassiumMg + 300,
                                        potassiumMagnesium = true
                                    )
                                )
                                Toast.makeText(context, "+300mg Potassium logged!", Toast.LENGTH_SHORT).show()
                            }
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        SymptomRemedyRow(
                            symptom = "Intense Sudden Hunger Wave",
                            cause = "Ghrelin Spike / Low Electrolytes",
                            remedy = "Place salt crystals on tongue + warm green tea.",
                            actionLabel = "+250mg Salt",
                            onAction = {
                                onUpdateElectrolyteProtocol(
                                    electrolyteProtocol.copy(
                                        sodiumMg = electrolyteProtocol.sodiumMg + 250,
                                        afternoonSaltBuster = true
                                    )
                                )
                                Toast.makeText(context, "+250mg Sodium logged!", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }
        }

        // 5. Zero-Calorie Homemade Electrolyte Recipe ("Snake Juice" / Fasting Drops)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("electrolyte_recipe_card"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (ThemeManager.isDarkMode) 0.5f else 0.8f)),
            elevation = CardDefaults.cardElevation(defaultElevation = if (ThemeManager.isDarkMode) 0.dp else 1.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showRecipeDetails = !showRecipeDetails },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🧪", fontSize = 18.sp)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Homemade Fasting Electrolyte Formula",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Zero-calorie 1-Liter recipe ratio",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Icon(
                        imageVector = if (showRecipeDetails) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Expand Recipe",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                AnimatedVisibility(visible = showRecipeDetails) {
                    Column(modifier = Modifier.padding(top = 16.dp)) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "1-Liter Pure Fasting Blend (Zero Calories):",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "• 1 Liter filtered or sparkling water\n" +
                                            "• 1/2 tsp Himalayan Pink Salt (~1,000mg Sodium)\n" +
                                            "• 1/2 tsp Potassium Chloride / NoSalt (~1,300mg Potassium)\n" +
                                            "• 1/4 tsp Food-grade Epsom Salt or 200mg Magnesium Glycinate powder\n" +
                                            "• Optional: 1/2 tsp Baking Soda (Sodium Bicarbonate for digestion)",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 18.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                onUpdateElectrolyteProtocol(
                                    electrolyteProtocol.copy(
                                        sodiumMg = electrolyteProtocol.sodiumMg + 1000,
                                        potassiumMg = electrolyteProtocol.potassiumMg + 1300,
                                        magnesiumMg = electrolyteProtocol.magnesiumMg + 200,
                                        pinkSalt = true,
                                        potassiumMagnesium = true
                                    )
                                )
                                onAddWater(1000)
                                Toast.makeText(context, "Logged 1L Electrolyte Batch (+1000 Na, +1300 K, +200 Mg, +1L H2O)!", Toast.LENGTH_LONG).show()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.AddCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Log 1-Liter Batch Prepared (+1L H2O & Minerals)", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Educational Mineral Notice
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (ThemeManager.isDarkMode) 0.45f else 0.85f)
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (ThemeManager.isDarkMode) 0.4f else 0.7f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Default.HealthAndSafety,
                    contentDescription = "Medical Notice",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(18.dp)
                        .padding(top = 2.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Electrolyte Disclaimer: Mineral targets and recipes are educational wellness benchmarks for healthy adults. They are not medical prescriptions. Individuals with kidney disease, cardiovascular conditions, or hypertension must consult their doctor before supplementing sodium, potassium, or magnesium.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )
            }
        }

        // 6. Reset Daily Minerals Button
        OutlinedButton(
            onClick = { showResetConfirmDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("reset_minerals_btn"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
        ) {
            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Reset Today's Mineral & Checklist Logs", fontSize = 12.sp)
        }
    }

    // Dialog: Custom Mineral Dosage Input
    if (showCustomDoseDialog != null) {
        val mineralKey = showCustomDoseDialog!!
        val mineralName = when (mineralKey) {
            "SODIUM" -> "Sodium / Salt"
            "POTASSIUM" -> "Potassium"
            else -> "Magnesium"
        }
        var customInput by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showCustomDoseDialog = null },
            title = { Text("Log $mineralName", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Enter amount in milligrams (mg):", fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = customInput,
                        onValueChange = { customInput = it.filter { char -> char.isDigit() } },
                        label = { Text("Dosage (mg)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("custom_dose_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = customInput.toIntOrNull() ?: 0
                        if (amount > 0) {
                            when (mineralKey) {
                                "SODIUM" -> {
                                    val newSodium = electrolyteProtocol.sodiumMg + amount
                                    onUpdateElectrolyteProtocol(electrolyteProtocol.copy(sodiumMg = newSodium, pinkSalt = true))
                                }
                                "POTASSIUM" -> {
                                    val newPotassium = electrolyteProtocol.potassiumMg + amount
                                    onUpdateElectrolyteProtocol(electrolyteProtocol.copy(potassiumMg = newPotassium, potassiumMagnesium = true))
                                }
                                "MAGNESIUM" -> {
                                    val newMagnesium = electrolyteProtocol.magnesiumMg + amount
                                    onUpdateElectrolyteProtocol(electrolyteProtocol.copy(magnesiumMg = newMagnesium, potassiumMagnesium = true))
                                }
                            }
                            Toast.makeText(context, "+$amount mg $mineralName recorded", Toast.LENGTH_SHORT).show()
                        }
                        showCustomDoseDialog = null
                    },
                    modifier = Modifier.testTag("confirm_custom_dose_btn")
                ) {
                    Text("Add Dose")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomDoseDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Dialog: Reset Confirmation
    if (showResetConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showResetConfirmDialog = false },
            title = { Text("Reset Daily Minerals?", fontWeight = FontWeight.Bold) },
            text = {
                Text("This will reset your logged Sodium, Potassium, Magnesium, and checklist items for today back to zero.", fontSize = 13.sp)
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdateElectrolyteProtocol(ElectrolyteProtocol())
                        showResetConfirmDialog = false
                        Toast.makeText(context, "Minerals reset for today", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Reset All")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun MiniMineralPill(
    label: String,
    value: String,
    percent: Float,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 12.sp,
                color = color
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                fontSize = 9.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { percent },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = color,
                trackColor = MaterialTheme.colorScheme.surface
            )
        }
    }
}

@Composable
fun MineralIntakeCard(
    title: String,
    subtitle: String,
    emoji: String,
    badgeColor: Color,
    currentAmount: Int,
    targetAmount: Int,
    unit: String,
    scientificInsight: String,
    quickDoseOptions: List<Pair<Int, String>>,
    onAddDose: (Int) -> Unit,
    onOpenCustom: () -> Unit,
    onUndo: () -> Unit,
    testTagPrefix: String
) {
    val progress = (currentAmount.toFloat() / targetAmount.toFloat()).coerceIn(0f, 1f)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("${testTagPrefix}_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (ThemeManager.isDarkMode) 0.5f else 0.8f)),
        elevation = CardDefaults.cardElevation(defaultElevation = if (ThemeManager.isDarkMode) 0.dp else 1.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(badgeColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(emoji, fontSize = 20.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = subtitle,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "$currentAmount / $targetAmount $unit",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp,
                        color = badgeColor
                    )
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = badgeColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = scientificInsight,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 15.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Quick dose buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                quickDoseOptions.forEach { (dose, label) ->
                    Button(
                        onClick = { onAddDose(dose) },
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                            .testTag("${testTagPrefix}_dose_${dose}"),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text(label, fontSize = 11.sp, maxLines = 1)
                    }
                }

                OutlinedButton(
                    onClick = onOpenCustom,
                    modifier = Modifier
                        .height(38.dp)
                        .testTag("${testTagPrefix}_custom_btn"),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text("Custom", fontSize = 11.sp)
                }

                if (currentAmount > 0) {
                    OutlinedButton(
                        onClick = onUndo,
                        modifier = Modifier
                            .height(38.dp)
                            .testTag("${testTagPrefix}_undo_btn"),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Undo Dose", modifier = Modifier.size(14.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun TimedChecklistItem(
    timeWindow: String,
    title: String,
    description: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    quickLogLabel: String?,
    onQuickLog: (() -> Unit)?,
    testTag: String
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (isChecked) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        border = BorderStroke(
            1.dp,
            if (isChecked) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onCheckedChange(!isChecked) }
            .testTag(testTag)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Checkbox(
                    checked = isChecked,
                    onCheckedChange = onCheckedChange,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = timeWindow,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = title,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = description,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 15.sp
                    )

                    if (!isChecked && quickLogLabel != null && onQuickLog != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = onQuickLog,
                            modifier = Modifier.height(32.dp),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(quickLogLabel, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SymptomRemedyRow(
    symptom: String,
    cause: String,
    remedy: String,
    actionLabel: String,
    onAction: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = symptom,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Likely: $cause",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = remedy,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = onAction,
                modifier = Modifier.height(32.dp),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(actionLabel, fontSize = 10.sp)
            }
        }
    }
}
