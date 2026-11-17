package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.local.ToolConfigEntity
import com.example.domain.StudioToolId
import com.example.domain.ToolCategory
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun ToolCategoryHubScreen(
    category: ToolCategory,
    allTools: List<ToolConfigEntity>,
    aiNotice: String,
    onSelectTool: (StudioToolId) -> Unit,
    onBack: (() -> Unit)? = null
) {
    var query by remember { mutableStateOf("") }
    val categoryTools = remember(allTools, category, query) {
        val dbMap = allTools.associateBy { it.id }
        StudioToolId.byCategory(category)
            .filter { tool ->
                query.isBlank() ||
                    tool.title.contains(query, ignoreCase = true) ||
                    tool.description.contains(query, ignoreCase = true)
            }
            .map { tool ->
                val cfg = dbMap[tool.id]
                tool to cfg
            }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBlack),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (onBack != null) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = PrimaryGold)
                    }
                }
                Column {
                    Text(category.title, style = MaterialTheme.typography.headlineMedium, color = TextPrimary)
                    Text(category.subtitle, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }
            }
        }

        if (category.requiresFirebaseAi) {
            item {
                FirebaseNoticeBanner(notice = aiNotice)
            }
        }

        item {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("Search ${category.title}...") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = PrimaryGold) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        items(categoryTools, key = { it.first.id }) { (tool, cfg) ->
            val enabled = cfg?.isEnabled ?: true
            val creditCost = cfg?.creditCost ?: tool.defaultCredits
            LuxuryCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("tool_card_${tool.id}"),
                onClick = if (enabled) {
                    { onSelectTool(tool) }
                } else null
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF231C0E)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (enabled) iconForTool(tool) else Icons.Filled.Lock,
                            contentDescription = tool.title,
                            tint = if (enabled) PrimaryGold else TextSecondary
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                "#${tool.number} ${tool.title}",
                                style = MaterialTheme.typography.titleMedium,
                                color = if (enabled) TextPrimary else TextSecondary
                            )
                        }
                        Spacer(Modifier.height(2.dp))
                        Text(tool.description, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                    Spacer(Modifier.width(8.dp))
                    Column(horizontalAlignment = Alignment.End) {
                        Surface(
                            color = if (!enabled) Color(0xFF2B1313) else if (creditCost == 0) Color(0xFF102819) else Color(0xFF231C0E),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = when {
                                    !enabled -> "Disabled"
                                    creditCost == 0 -> "Free"
                                    else -> "$creditCost cr"
                                },
                                color = when {
                                    !enabled -> StatusError
                                    creditCost == 0 -> StatusSuccess
                                    else -> SecondaryGold
                                },
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        if (category.requiresFirebaseAi && enabled) {
                            Spacer(Modifier.height(4.dp))
                            Text("Preview", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                        }
                    }
                }
            }
        }
    }
}
