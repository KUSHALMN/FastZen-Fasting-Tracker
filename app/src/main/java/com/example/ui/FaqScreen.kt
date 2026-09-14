package com.example.ui

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.FastingFaqItem

val FastingFaqRepository = listOf(
    FastingFaqItem(
        id = "1",
        category = "Drinks & Fasting",
        question = "Does black coffee or tea break my fast?",
        answer = "No, pure black coffee and unsweetened plain green, black, or herbal teas do not break a fast. They contain virtually zero calories and contain polyphenols that can actually stimulate autophagy and enhance fat oxidation. Avoid adding milk, creamer, sugar, honey, or artificial syrups.",
        keyTakeaway = "Black coffee and plain teas are completely fast-friendly."
    ),
    FastingFaqItem(
        id = "2",
        category = "Drinks & Fasting",
        question = "What strictly breaks a fast?",
        answer = "Any food, drink, or supplement with caloric value (protein, carbohydrates, or fats) causes the digestive system to activate and raises circulating insulin. Even 30-50 calories of milk, sugary gum, or protein powder pauses the fasted state and halts deep autophagy.",
        keyTakeaway = "Anything that triggers digestive calories and insulin disrupts your fast."
    ),
    FastingFaqItem(
        id = "3",
        category = "Drinks & Fasting",
        question = "Can I drink lemon water or apple cider vinegar?",
        answer = "Yes! A few squeezes of fresh lemon juice or 1-2 tablespoons of raw unfiltered apple cider vinegar diluted in a large glass of water have negligible calories and will not break ketosis. In fact, ACV can improve insulin sensitivity and blunt hunger.",
        keyTakeaway = "Lemon water and diluted ACV are safe and beneficial."
    ),
    FastingFaqItem(
        id = "4",
        category = "Metabolism & Science",
        question = "What is Autophagy and when does it begin?",
        answer = "Autophagy (literally 'self-eating') is your body's cellular recycling process. It cleanses damaged cellular debris, misfolded proteins, and worn-out mitochondria. Research suggests autophagy initiates in subtle amounts around 16 hours and accelerates significantly between 18 to 24+ hours of continuous fasting.",
        keyTakeaway = "Autophagy peaks between 18-24 hours of fasting."
    ),
    FastingFaqItem(
        id = "5",
        category = "Metabolism & Science",
        question = "When do I enter Ketosis during fasting?",
        answer = "As liver glycogen stores empty (usually within 12 to 16 hours depending on your prior carbohydrate intake and activity level), your liver starts converting stored fatty acids into ketones (acetoacetate and beta-hydroxybutyrate) for clean, sustained brain fuel.",
        keyTakeaway = "Ketosis typically kicks in between 12 and 16 hours."
    ),
    FastingFaqItem(
        id = "6",
        category = "Hunger & Symptoms",
        question = "How do I handle sudden hunger waves?",
        answer = "Hunger is dictated by the hormone ghrelin, which releases in waves according to your usual meal times. It does not grow indefinitely. When a wave hits, drink a tall glass of cold water with a pinch of pink Himalayan sea salt, sip sparkling water, or take a short brisk walk. The wave typically passes within 15-20 minutes.",
        keyTakeaway = "Hunger comes in short waves that fade within 20 minutes."
    ),
    FastingFaqItem(
        id = "7",
        category = "Hunger & Symptoms",
        question = "Why do I get headaches or fatigue ('Fasting Flu')?",
        answer = "When insulin drops during fasting, your kidneys naturally excrete sodium and water rapidly. Headaches, lightheadedness, and lethargy are almost always caused by electrolyte depletion (sodium, potassium, magnesium), not starvation. Taking electrolytes in water will quickly relieve these symptoms.",
        keyTakeaway = "Electrolyte depletion is the primary cause of headaches."
    ),
    FastingFaqItem(
        id = "8",
        category = "Breaking a Fast",
        question = "How should I break my fast without digestive upset?",
        answer = "Break your fast gently, especially on fasts exceeding 18 hours. Begin with easily digested whole foods: a cup of warm bone broth, half an avocado, scrambled eggs, or steamed greens. Wait 30 minutes before eating a larger meal. Avoid sudden large loads of refined sugar, deep-fried food, or high-glycemic carbohydrates.",
        keyTakeaway = "Start small with healthy fats and protein; avoid sugary feasts."
    ),
    FastingFaqItem(
        id = "9",
        category = "Fitness & Safety",
        question = "Can I exercise or lift weights while fasting?",
        answer = "Yes! Exercising while fasted can boost fat oxidation and trigger spikes in Human Growth Hormone (HGH), which helps preserve lean muscle mass. Stay well-hydrated with electrolytes. If doing intense lifting, schedule your workout close to the end of your fast so you can replenish with protein soon after.",
        keyTakeaway = "Fasted workouts are safe and effective with proper electrolytes."
    ),
    FastingFaqItem(
        id = "10",
        category = "Fitness & Safety",
        question = "Who should NOT practice intermittent fasting?",
        answer = "Fasting is not recommended for pregnant or nursing mothers, individuals with an active or history of eating disorders, children or teenagers who are still developing, or individuals with type 1 diabetes without close physician supervision. Always listen to your body.",
        keyTakeaway = "Pregnant women, children, and those with eating disorders should not fast."
    )
)

@Composable
fun FaqScreen(
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    val expandedIds = remember { mutableStateListOf<String>() }

    val categories = listOf("All", "Drinks & Fasting", "Metabolism & Science", "Hunger & Symptoms", "Breaking a Fast", "Fitness & Safety")

    val filteredFaqs = FastingFaqRepository.filter { faq ->
        val matchesCategory = selectedCategory == "All" || faq.category == selectedCategory
        val matchesSearch = searchQuery.isBlank() ||
                faq.question.contains(searchQuery, ignoreCase = true) ||
                faq.answer.contains(searchQuery, ignoreCase = true)
        matchesCategory && matchesSearch
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 96.dp)
    ) {
        // Hero Header Banner
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
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MenuBook,
                            contentDescription = "Fasting Knowledge Icon",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "Fasting Knowledge & FAQs",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Evidence-based answers to master your fasts",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Search Field
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("faq_search_input"),
                placeholder = { Text("Search coffee, autophagy, hunger, etc.") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search FAQs",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear search")
                        }
                    }
                },
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )
        }

        // Categories Horizontal Row
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { category ->
                    val isSelected = category == selectedCategory
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategory = category },
                        label = { Text(category) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }

        if (filteredFaqs.isEmpty()) {
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
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.SearchOff,
                            contentDescription = "No questions found",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(44.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "No questions match '$searchQuery'",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Try searching for coffee, tea, hunger, or electrolytes.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(filteredFaqs) { faq ->
                val isExpanded = expandedIds.contains(faq.id)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .clickable {
                            if (isExpanded) expandedIds.remove(faq.id)
                            else expandedIds.add(faq.id)
                        }
                        .testTag("faq_item_${faq.id}"),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = faq.category.uppercase(),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 1.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = faq.question,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            IconButton(
                                onClick = {
                                    if (isExpanded) expandedIds.remove(faq.id)
                                    else expandedIds.add(faq.id)
                                }
                            ) {
                                Icon(
                                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = if (isExpanded) "Collapse question" else "Expand question",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        AnimatedVisibility(visible = isExpanded) {
                            Column(modifier = Modifier.padding(top = 12.dp)) {
                                Text(
                                    text = faq.answer,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 20.sp
                                )

                                if (faq.keyTakeaway.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Lightbulb,
                                                contentDescription = "Key takeaway",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = faq.keyTakeaway,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
