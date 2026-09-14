package com.example.ui

import androidx.compose.animation.*
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
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.FastingFaqRepository
import com.example.model.FastingFaqItem

@Composable
fun FaqScreen(
    modifier: Modifier = Modifier
) {
    val clipboardManager = LocalClipboardManager.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    val expandedIds = remember { mutableStateListOf<String>() }
    val bookmarkedIds = remember { mutableStateListOf<String>() }
    var showOnlyBookmarked by remember { mutableStateOf(false) }
    var copyToastMessage by remember { mutableStateOf<String?>(null) }

    val allFaqs = remember { FastingFaqRepository.allFaqs }
    val categories = remember { FastingFaqRepository.categories }
    val categoryCounts = remember(allFaqs) {
        allFaqs.groupingBy { it.category }.eachCount()
    }

    // Filter FAQs based on category, search query, and bookmarks
    val filteredFaqs = remember(selectedCategory, searchQuery, showOnlyBookmarked, bookmarkedIds.size) {
        allFaqs.filter { faq ->
            val matchesBookmark = !showOnlyBookmarked || bookmarkedIds.contains(faq.id)
            val matchesCategory = selectedCategory == "All" || faq.category == selectedCategory
            val matchesSearch = searchQuery.isBlank() ||
                    faq.question.contains(searchQuery, ignoreCase = true) ||
                    faq.answer.contains(searchQuery, ignoreCase = true) ||
                    faq.keyTakeaway.contains(searchQuery, ignoreCase = true)
            matchesBookmark && matchesCategory && matchesSearch
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 96.dp)
    ) {
        // Hero Header Banner with Total Stats
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MenuBook,
                            contentDescription = "Fasting Encyclopedia Icon",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Fasting Encyclopedia",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${allFaqs.size} evidence-based answers to master your fasts",
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
                placeholder = { Text("Search coffee, autophagy, hunger, muscle, etc.") },
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

        // Action Toolbar: Saved Bookmarks toggle & Expand/Collapse All
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Bookmarked filter button
                FilterChip(
                    selected = showOnlyBookmarked,
                    onClick = { showOnlyBookmarked = !showOnlyBookmarked },
                    leadingIcon = {
                        Icon(
                            imageVector = if (showOnlyBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = "Bookmarked FAQs",
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    label = {
                        Text(
                            text = if (bookmarkedIds.isEmpty()) "Saved" else "Saved (${bookmarkedIds.size})",
                            fontSize = 12.sp
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${filteredFaqs.size} answers",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))

                    TextButton(
                        onClick = {
                            if (expandedIds.size == filteredFaqs.size) {
                                expandedIds.clear()
                            } else {
                                expandedIds.clear()
                                expandedIds.addAll(filteredFaqs.map { it.id })
                            }
                        },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (expandedIds.size == filteredFaqs.size && filteredFaqs.isNotEmpty()) "Collapse All" else "Expand All",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // Categories Horizontal Scrolling Bar with Counts
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories, key = { it }, contentType = { "category_chip" }) { category ->
                    val isSelected = category == selectedCategory && !showOnlyBookmarked
                    val count = if (category == "All") allFaqs.size else (categoryCounts[category] ?: 0)

                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            selectedCategory = category
                            showOnlyBookmarked = false
                        },
                        label = {
                            Text(
                                text = "$category ($count)",
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }

        // Empty Search or Filter State
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
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = if (showOnlyBookmarked) Icons.Outlined.BookmarkBorder else Icons.Default.SearchOff,
                            contentDescription = "No questions found",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (showOnlyBookmarked) "No Saved Questions Yet" else "No questions match '$searchQuery'",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (showOnlyBookmarked)
                                "Tap the bookmark icon on any question to save it for offline review!"
                            else
                                "Try searching for coffee, tea, hunger, electrolytes, or lifting.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            // FAQs List with Animated Expansion, Copy, and Bookmark Actions
            items(filteredFaqs, key = { it.id }, contentType = { "faq_item" }) { faq ->
                val isExpanded = expandedIds.contains(faq.id)
                val isBookmarked = bookmarkedIds.contains(faq.id)
                val categoryColor = getFaqCategoryColor(faq.category)

                FaqCard(
                    faq = faq,
                    isExpanded = isExpanded,
                    isBookmarked = isBookmarked,
                    categoryColor = categoryColor,
                    onToggleExpand = {
                        if (isExpanded) expandedIds.remove(faq.id)
                        else expandedIds.add(faq.id)
                    },
                    onToggleBookmark = {
                        if (isBookmarked) bookmarkedIds.remove(faq.id)
                        else bookmarkedIds.add(faq.id)
                    },
                    onCopy = {
                        val textToCopy = "${faq.question}\n\n${faq.answer}\n\nTakeaway: ${faq.keyTakeaway}"
                        clipboardManager.setText(AnnotatedString(textToCopy))
                    }
                )
            }
        }
    }
}

// Pure top-level category color mapping (zero allocations)
fun getFaqCategoryColor(category: String): Color {
    return when (category) {
        "Drinks & Fasting" -> Color(0xFF0284C7)      // Ocean Blue
        "Metabolism & Science" -> Color(0xFF8B5CF6)  // Violet
        "Hunger & Symptoms" -> Color(0xFFEA580C)     // Warm Amber
        "Breaking a Fast" -> Color(0xFF10B981)       // Emerald
        "Fitness & Muscle" -> Color(0xFFEF4444)      // Crimson
        "Women's Health" -> Color(0xFFEC4899)        // Rose Pink
        "Protocols & Habits" -> Color(0xFF14B8A6)    // Teal
        "Myths & Mistakes" -> Color(0xFFF59E0B)      // Gold
        "Mindset & Psychology" -> Color(0xFF6366F1)  // Indigo
        "Longevity & Anti-Aging" -> Color(0xFF3B82F6)// Deep Azure
        else -> Color(0xFF00796B)
    }
}

@Composable
private fun FaqCard(
    faq: FastingFaqItem,
    isExpanded: Boolean,
    isBookmarked: Boolean,
    categoryColor: Color,
    onToggleExpand: () -> Unit,
    onToggleBookmark: () -> Unit,
    onCopy: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onToggleExpand)
            .testTag("faq_item_${faq.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    // Colored Category Tag
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = categoryColor.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = faq.category.uppercase(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.8.sp,
                            color = categoryColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = faq.question,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 21.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Bookmark IconButton
                    IconButton(
                        onClick = onToggleBookmark,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = if (isBookmarked) "Remove bookmark" else "Bookmark question",
                            tint = if (isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Expand/Collapse Icon
                    IconButton(
                        onClick = onToggleExpand,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (isExpanded) "Collapse" else "Expand",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Expandable Content
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    Text(
                        text = faq.answer,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 21.sp
                    )

                    if (faq.keyTakeaway.isNotBlank()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = categoryColor.copy(alpha = 0.10f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lightbulb,
                                    contentDescription = "Key takeaway",
                                    tint = categoryColor,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = faq.keyTakeaway,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = categoryColor,
                                    lineHeight = 17.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Quick Copy Answer Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = onCopy,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy answer",
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Copy", fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}
