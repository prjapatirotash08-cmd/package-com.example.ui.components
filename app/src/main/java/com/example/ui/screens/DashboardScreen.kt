package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.local.*
import com.example.domain.ToolCategory
import com.example.ui.components.*
import com.example.ui.theme.*
import java.io.File

@Composable
fun DashboardScreen(
    user: UserEntity,
    projects: List<ProjectEntity>,
    favorites: List<ProjectEntity>,
    history: List<HistoryEntity>,
    banners: List<BannerEntity>,
    notifications: List<NotificationEntity>,
    aiNotice: String,
    maintenanceMode: Boolean,
    onOpenCategory: (ToolCategory) -> Unit,
    onOpenProjects: () -> Unit,
    onOpenFavorites: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenSubscriptions: () -> Unit,
    onOpenProfile: () -> Unit,
    onOpenAdmin: () -> Unit,
    onOpenToolFromProject: (ProjectEntity) -> Unit
) {
    var showNotificationsDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBlack),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Maintenance alert if toggled by Admin
        if (maintenanceMode) {
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF2B1313),
                    border = BorderStroke(1.dp, StatusError),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Warning, contentDescription = null, tint = StatusError)
                        Spacer(Modifier.width(10.dp))
                        Text(
                            "Maintenance Mode is currently enabled by Admin. Some actions may be restricted.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextPrimary
                        )
                    }
                }
            }
        }

        // Welcome & Credits Hero Card
        item {
            LuxuryCard(highlighted = true) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Welcome back, ${user.name}",
                            style = MaterialTheme.typography.headlineMedium,
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Surface(
                                color = Color(0xFF231C0E),
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(1.dp, PrimaryGold)
                            ) {
                                Text(
                                    "${user.planName} PLAN",
                                    color = SecondaryGold,
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                            if (user.role == "ADMIN") {
                                Surface(
                                    color = Color(0xFF1A291D),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.clickable(onClick = onOpenAdmin)
                                ) {
                                    Text(
                                        "ADMIN",
                                        color = StatusSuccess,
                                        style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(onClick = { showNotificationsDialog = true }) {
                            BadgedBox(
                                badge = {
                                    if (notifications.isNotEmpty()) {
                                        Badge(containerColor = PrimaryGold, contentColor = ObsidianBlack) {
                                            Text("${notifications.size}")
                                        }
                                    }
                                }
                            ) {
                                Icon(Icons.Filled.Notifications, contentDescription = "Notifications", tint = PrimaryGold)
                            }
                        }
                        CreditBalancePill(credits = user.credits, onClick = onOpenSubscriptions)
                    }
                }

                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    DashboardStatTile(
                        label = "Credits",
                        value = "${user.credits}",
                        icon = Icons.Filled.AutoAwesome,
                        modifier = Modifier.weight(1f),
                        onClick = onOpenSubscriptions
                    )
                    DashboardStatTile(
                        label = "Projects",
                        value = "${projects.size}",
                        icon = Icons.Filled.Folder,
                        modifier = Modifier.weight(1f),
                        onClick = onOpenProjects
                    )
                    DashboardStatTile(
                        label = "Favorites",
                        value = "${favorites.size}",
                        icon = Icons.Filled.Favorite,
                        modifier = Modifier.weight(1f),
                        onClick = onOpenFavorites
                    )
                }
            }
        }

        // Admin Portal shortcut when signed in as Admin
        if (user.role == "ADMIN") {
            item {
                GoldGradientButton(
                    text = "Open Admin Control Panel (10 Admin Tools)",
                    icon = Icons.Filled.AdminPanelSettings,
                    onClick = onOpenAdmin,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dashboard_open_admin_button")
                )
            }
        }

        // Published Admin Banners
        if (banners.isNotEmpty()) {
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(banners, key = { it.id }) { banner ->
                        LuxuryCard(
                            highlighted = true,
                            modifier = Modifier.width(290.dp),
                            onClick = {
                                when (banner.targetRoute) {
                                    "creative_hub" -> onOpenCategory(ToolCategory.CREATIVE_STUDIO)
                                    "ai_edit_hub" -> onOpenCategory(ToolCategory.AI_EDITING)
                                    "ai_gen_hub" -> onOpenCategory(ToolCategory.AI_GENERATOR)
                                    else -> onOpenCategory(ToolCategory.ADVANCED_EDITING)
                                }
                            }
                        ) {
                            Text(banner.title, style = MaterialTheme.typography.titleMedium, color = SecondaryGold)
                            Spacer(Modifier.height(4.dp))
                            Text(banner.subtitle, style = MaterialTheme.typography.bodySmall, color = TextSecondary, maxLines = 2)
                            Spacer(Modifier.height(10.dp))
                            Text("${banner.ctaLabel} →", style = MaterialTheme.typography.labelMedium, color = PrimaryGold)
                        }
                    }
                }
            }
        }

        // Firebase AI Notice
        item {
            FirebaseNoticeBanner(notice = aiNotice)
        }

        // 4 Studio Category Launchers (40 tools)
        item {
            SectionHeader(title = "Studio Suites", subtitle = "Choose a specialized creative engine")
            Spacer(Modifier.height(10.dp))
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                ToolCategory.entries.forEach { cat ->
                    LuxuryCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dashboard_category_${cat.id}"),
                        onClick = { onOpenCategory(cat) }
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
                                    imageVector = when (cat) {
                                        ToolCategory.AI_EDITING -> Icons.Filled.AutoFixHigh
                                        ToolCategory.AI_GENERATOR -> Icons.Filled.AutoAwesome
                                        ToolCategory.CREATIVE_STUDIO -> Icons.Filled.Dashboard
                                        ToolCategory.ADVANCED_EDITING -> Icons.Filled.Tune
                                    },
                                    contentDescription = cat.title,
                                    tint = PrimaryGold
                                )
                            }
                            Spacer(Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(cat.title, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                                Text(cat.subtitle, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            }
                            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = PrimaryGold)
                        }
                    }
                }
            }
        }

        // Recent Projects
        item {
            SectionHeader(
                title = "Recent Projects",
                actionLabel = "View All",
                onActionClick = onOpenProjects
            )
            Spacer(Modifier.height(8.dp))
            if (projects.isEmpty()) {
                EmptyStateCard(
                    icon = Icons.Filled.PhotoLibrary,
                    title = "No Saved Projects Yet",
                    subtitle = "Edit a photo in Creative Studio or Advanced Tools and tap Save to build your portfolio.",
                    actionLabel = "Open Creative Studio",
                    onAction = { onOpenCategory(ToolCategory.CREATIVE_STUDIO) }
                )
            } else {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(projects.take(8), key = { it.id }) { project ->
                        LuxuryCard(
                            modifier = Modifier.width(165.dp),
                            onClick = { onOpenToolFromProject(project) }
                        ) {
                            AsyncImage(
                                model = File(project.imagePath),
                                contentDescription = project.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(110.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(ObsidianBlack)
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                project.title,
                                style = MaterialTheme.typography.titleSmall,
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                "${project.toolName} • ${project.format}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        // Recent Activity
        item {
            SectionHeader(
                title = "Recent Activity",
                actionLabel = "History",
                onActionClick = onOpenHistory
            )
            Spacer(Modifier.height(8.dp))
            if (history.isEmpty()) {
                LuxuryCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "No tool runs yet. Launch any tool above to begin.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    history.take(5).forEach { entry ->
                        LuxuryCard(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(entry.toolName, style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                                    Text(entry.inputSummary, style = MaterialTheme.typography.bodySmall, color = TextSecondary, maxLines = 1)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        entry.status,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (entry.status == "SUCCESS") StatusSuccess else SecondaryGold
                                    )
                                    Text(
                                        formatTimestamp(entry.createdAt),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            GoldOutlinedButton(
                text = "Account, Profile & Support",
                icon = Icons.Filled.Person,
                onClick = onOpenProfile,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))
        }
    }

    if (showNotificationsDialog) {
        AlertDialog(
            onDismissRequest = { showNotificationsDialog = false },
            containerColor = CardObsidian,
            title = { Text("Studio Notifications", color = TextPrimary) },
            text = {
                if (notifications.isEmpty()) {
                    Text("No active notifications.", color = TextSecondary)
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        notifications.forEach { n ->
                            Column {
                                Text(n.title, style = MaterialTheme.typography.titleSmall, color = SecondaryGold)
                                Text(n.message, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            }
                            HorizontalDivider(color = BorderGoldMuted)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showNotificationsDialog = false }) {
                    Text("Close", color = PrimaryGold)
                }
            }
        )
    }
}

@Composable
private fun DashboardStatTile(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = CharcoalSurface,
        border = BorderStroke(1.dp, BorderGoldMuted)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = label, tint = PrimaryGold, modifier = Modifier.size(18.dp))
            Spacer(Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
            Text(label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
        }
    }
}
