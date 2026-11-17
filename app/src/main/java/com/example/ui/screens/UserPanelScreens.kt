package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.local.*
import com.example.ui.components.*
import com.example.ui.theme.*
import java.io.File

enum class UserHubTab(val label: String) {
    PROJECTS("Projects"),
    FAVORITES("Favorites"),
    DOWNLOADS("Downloads"),
    HISTORY("History"),
    CREDITS("Credits & Plans"),
    REFERRAL("Referrals"),
    SUPPORT("Support"),
    PROFILE("Profile")
}

@Composable
fun UserPanelHubScreen(
    initialTab: UserHubTab = UserHubTab.PROJECTS,
    user: UserEntity,
    projects: List<ProjectEntity>,
    favorites: List<ProjectEntity>,
    history: List<HistoryEntity>,
    transactions: List<CreditTransactionEntity>,
    plans: List<SubscriptionPlanEntity>,
    payments: List<PaymentRecordEntity>,
    tickets: List<SupportTicketEntity>,
    dailyBonusAmount: Int,
    onOpenProject: (ProjectEntity) -> Unit,
    onRenameProject: (Long, String) -> Unit,
    onToggleFavorite: (Long) -> Unit,
    onDeleteProject: (Long) -> Unit,
    onExportProject: (ProjectEntity) -> Unit,
    onShareProject: (ProjectEntity) -> Unit,
    onClaimDailyBonus: () -> Unit,
    onSubscribePlan: (SubscriptionPlanEntity) -> Unit,
    onRedeemReferral: (String) -> Unit,
    onSubmitTicket: (String, String, () -> Unit) -> Unit,
    onUpdateProfileName: (String) -> Unit,
    onOpenAdminPanel: () -> Unit,
    onLogout: () -> Unit,
    onNotify: (String) -> Unit,
    onBack: (() -> Unit)? = null
) {
    var activeTab by remember(initialTab) { mutableStateOf(initialTab) }
    var renameTarget by remember { mutableStateOf<ProjectEntity?>(null) }
    var deleteTarget by remember { mutableStateOf<ProjectEntity?>(null) }
    val clipboard = LocalClipboardManager.current

    if (onBack != null) {
        BackHandler(onBack = onBack)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBlack)
    ) {
        // Header + Tab Strip
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = PrimaryGold)
                        }
                    }
                    Text("Studio Vault & Account", style = MaterialTheme.typography.headlineMedium, color = TextPrimary)
                }
                CreditBalancePill(credits = user.credits, onClick = { activeTab = UserHubTab.CREDITS })
            }
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                UserHubTab.entries.forEach { tab ->
                    FilterChip(
                        selected = activeTab == tab,
                        onClick = { activeTab = tab },
                        label = { Text(tab.label) },
                        modifier = Modifier.testTag("user_tab_${tab.name.lowercase()}")
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            when (activeTab) {
                UserHubTab.PROJECTS -> {
                    if (projects.isEmpty()) {
                        item {
                            EmptyStateCard(
                                icon = Icons.Filled.FolderOpen,
                                title = "No Saved Projects",
                                subtitle = "Save edits from any tool to manage, rename, favorite, and export them here."
                            )
                        }
                    } else {
                        items(projects, key = { it.id }) { proj ->
                            ProjectItemCard(
                                project = proj,
                                onOpen = { onOpenProject(proj) },
                                onRename = { renameTarget = proj },
                                onToggleFavorite = { onToggleFavorite(proj.id) },
                                onExport = { onExportProject(proj) },
                                onShare = { onShareProject(proj) },
                                onDelete = { deleteTarget = proj }
                            )
                        }
                    }
                }

                UserHubTab.FAVORITES -> {
                    if (favorites.isEmpty()) {
                        item {
                            EmptyStateCard(
                                icon = Icons.Filled.FavoriteBorder,
                                title = "No Favorites Yet",
                                subtitle = "Tap the heart icon on any saved project to bookmark it here."
                            )
                        }
                    } else {
                        items(favorites, key = { it.id }) { proj ->
                            ProjectItemCard(
                                project = proj,
                                onOpen = { onOpenProject(proj) },
                                onRename = { renameTarget = proj },
                                onToggleFavorite = { onToggleFavorite(proj.id) },
                                onExport = { onExportProject(proj) },
                                onShare = { onShareProject(proj) },
                                onDelete = { deleteTarget = proj }
                            )
                        }
                    }
                }

                UserHubTab.DOWNLOADS -> {
                    val downloads = projects.filter { it.isExportedToGallery || it.fileSizeBytes > 0 }
                    if (downloads.isEmpty()) {
                        item {
                            EmptyStateCard(
                                icon = Icons.Filled.DownloadDone,
                                title = "Download Gallery Empty",
                                subtitle = "Exported or saved PNG, JPG, and WEBP studio renders appear here."
                            )
                        }
                    } else {
                        items(downloads, key = { it.id }) { proj ->
                            LuxuryCard {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    AsyncImage(
                                        model = File(proj.imagePath),
                                        contentDescription = proj.title,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(64.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                    )
                                    Spacer(Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(proj.title, style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                                        Text(
                                            "${proj.format} • ${proj.widthPx}×${proj.heightPx} • ${proj.fileSizeBytes / 1024}KB",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextSecondary
                                        )
                                        Text(
                                            if (proj.isExportedToGallery) "Exported to Gallery" else "Saved Locally",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (proj.isExportedToGallery) StatusSuccess else SecondaryGold
                                        )
                                    }
                                    IconButton(onClick = { onExportProject(proj) }) {
                                        Icon(Icons.Filled.Download, contentDescription = "Save to Gallery", tint = PrimaryGold)
                                    }
                                    IconButton(onClick = { onShareProject(proj) }) {
                                        Icon(Icons.Filled.Share, contentDescription = "Share", tint = TextPrimary)
                                    }
                                }
                            }
                        }
                    }
                }

                UserHubTab.HISTORY -> {
                    if (history.isEmpty()) {
                        item {
                            EmptyStateCard(
                                icon = Icons.Filled.History,
                                title = "No Editing History",
                                subtitle = "Every tool run and generation attempt is recorded here."
                            )
                        }
                    } else {
                        items(history, key = { it.id }) { entry ->
                            LuxuryCard {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(entry.toolName, style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                                        Text(entry.inputSummary, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                        Text(formatTimestamp(entry.createdAt), style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            entry.status,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (entry.status == "SUCCESS") StatusSuccess else SecondaryGold
                                        )
                                        Text("${entry.creditsUsed} cr", style = MaterialTheme.typography.labelMedium, color = PrimaryGold)
                                    }
                                }
                            }
                        }
                    }
                }

                UserHubTab.CREDITS -> {
                    item {
                        LuxuryCard(highlighted = true) {
                            Text("Available Credits", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                            Text("${user.credits} Credits", style = MaterialTheme.typography.displayMedium, color = SecondaryGold)
                            Spacer(Modifier.height(6.dp))
                            Text("Active Plan: ${user.planName}", style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                            Spacer(Modifier.height(12.dp))
                            GoldGradientButton(
                                text = "Claim +$dailyBonusAmount Daily Free Credits",
                                icon = Icons.Filled.CardGiftcard,
                                onClick = onClaimDailyBonus,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("claim_daily_credits_button")
                            )
                        }
                    }
                    item {
                        SectionHeader(
                            title = "Subscription Plans",
                            subtitle = "Payment orders require server webhook verification"
                        )
                    }
                    items(plans, key = { it.id }) { plan ->
                        LuxuryCard(highlighted = plan.isPopular) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(plan.name, style = MaterialTheme.typography.titleMedium, color = SecondaryGold)
                                    Text("${plan.monthlyCredits} monthly credits", style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                                }
                                Text(
                                    if (plan.priceUsd <= 0.0) "Free" else "$${"%.2f".format(plan.priceUsd)}/mo",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = TextPrimary
                                )
                            }
                            Spacer(Modifier.height(6.dp))
                            Text(plan.featuresSummary, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            Spacer(Modifier.height(10.dp))
                            GoldOutlinedButton(
                                text = if (user.planName == plan.name) "Current Plan" else "Create Order (${plan.name})",
                                enabled = user.planName != plan.name && plan.isActive,
                                onClick = { onSubscribePlan(plan) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                    if (payments.isNotEmpty()) {
                        item { SectionHeader(title = "Payment & Order History") }
                        items(payments, key = { it.id }) { pay ->
                            LuxuryCard {
                                Text("${pay.planName} • ${pay.transactionRef}", style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                                Text("Status: ${pay.status}", style = MaterialTheme.typography.labelSmall, color = StatusWarning)
                                Text(pay.note, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            }
                        }
                    }
                    item { SectionHeader(title = "Credit Ledger") }
                    items(transactions, key = { it.id }) { tx ->
                        LuxuryCard {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(tx.description, style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                                    Text("${tx.type} • ${formatTimestamp(tx.createdAt)}", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                                }
                                Text(
                                    text = if (tx.amount >= 0) "+${tx.amount}" else "${tx.amount}",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = if (tx.amount >= 0) StatusSuccess else StatusError
                                )
                            }
                        }
                    }
                }

                UserHubTab.REFERRAL -> {
                    item {
                        var refInput by remember { mutableStateOf("") }
                        LuxuryCard(highlighted = true) {
                            Text("Your Referral Code", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                            Spacer(Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(user.referralCode, style = MaterialTheme.typography.headlineMedium, color = SecondaryGold)
                                GoldOutlinedButton(
                                    text = "Copy",
                                    icon = Icons.Filled.ContentCopy,
                                    onClick = {
                                        clipboard.setText(AnnotatedString(user.referralCode))
                                        onNotify("Copied referral code ${user.referralCode}")
                                    }
                                )
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Share your code: friends earn +10 credits on signup and you earn +15 credits.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                            Spacer(Modifier.height(16.dp))
                            if (user.referredByCode.isNullOrBlank()) {
                                OutlinedTextField(
                                    value = refInput,
                                    onValueChange = { refInput = it },
                                    label = { Text("Enter Friend's Referral Code") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(Modifier.height(10.dp))
                                GoldGradientButton(
                                    text = "Redeem Referral (+10 Credits)",
                                    onClick = { onRedeemReferral(refInput) },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            } else {
                                Text("Redeemed Referral Code: ${user.referredByCode}", color = StatusSuccess, style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                }

                UserHubTab.SUPPORT -> {
                    item {
                        var subject by remember { mutableStateOf("") }
                        var message by remember { mutableStateOf("") }
                        LuxuryCard(highlighted = true) {
                            Text("Customer Support Ticket", style = MaterialTheme.typography.titleMedium, color = SecondaryGold)
                            Spacer(Modifier.height(10.dp))
                            OutlinedTextField(
                                value = subject,
                                onValueChange = { subject = it },
                                label = { Text("Subject") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(Modifier.height(10.dp))
                            OutlinedTextField(
                                value = message,
                                onValueChange = { message = it },
                                label = { Text("How can we help?") },
                                minLines = 3,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(Modifier.height(12.dp))
                            GoldGradientButton(
                                text = "Submit Ticket",
                                icon = Icons.Filled.SupportAgent,
                                onClick = {
                                    onSubmitTicket(subject, message) {
                                        subject = ""
                                        message = ""
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                    items(tickets, key = { it.id }) { ticket ->
                        LuxuryCard {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(ticket.subject, style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                                Text(
                                    ticket.status,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (ticket.status == "RESOLVED") StatusSuccess else SecondaryGold
                                )
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(ticket.message, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            if (!ticket.adminReply.isNullOrBlank()) {
                                Spacer(Modifier.height(8.dp))
                                Surface(
                                    color = Color(0xFF102819),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        "Admin Reply: ${ticket.adminReply}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = StatusSuccess,
                                        modifier = Modifier.padding(10.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                UserHubTab.PROFILE -> {
                    item {
                        var editName by remember(user.name) { mutableStateOf(user.name) }
                        LuxuryCard(highlighted = true) {
                            Text("Account Profile", style = MaterialTheme.typography.titleLarge, color = SecondaryGold)
                            Spacer(Modifier.height(6.dp))
                            Text("Email: ${user.email}", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                            Text("Role: ${user.role} • Status: ${user.status}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            Spacer(Modifier.height(12.dp))
                            OutlinedTextField(
                                value = editName,
                                onValueChange = { editName = it },
                                label = { Text("Display Name") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(Modifier.height(12.dp))
                            GoldGradientButton(
                                text = "Update Profile",
                                onClick = { onUpdateProfileName(editName) },
                                modifier = Modifier.fillMaxWidth()
                            )
                            if (user.role == "ADMIN") {
                                Spacer(Modifier.height(10.dp))
                                GoldOutlinedButton(
                                    text = "Open Admin Panel",
                                    icon = Icons.Filled.AdminPanelSettings,
                                    onClick = onOpenAdminPanel,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            Spacer(Modifier.height(10.dp))
                            OutlinedButton(
                                onClick = onLogout,
                                border = BorderStroke(1.dp, StatusError),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusError),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Filled.Logout, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Sign Out")
                            }
                        }
                    }
                }
            }
        }
    }

    renameTarget?.let { target ->
        var newName by remember(target.id) { mutableStateOf(target.title) }
        AlertDialog(
            onDismissRequest = { renameTarget = null },
            containerColor = CardObsidian,
            title = { Text("Rename Project", color = TextPrimary) },
            text = {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("New Title") },
                    singleLine = true
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onRenameProject(target.id, newName)
                        renameTarget = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGold, contentColor = ObsidianBlack)
                ) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { renameTarget = null }) { Text("Cancel", color = TextSecondary) }
            }
        )
    }

    deleteTarget?.let { target ->
        StudioConfirmDialog(
            title = "Delete Project?",
            message = "This permanently removes '${target.title}' and its saved image file.",
            confirmLabel = "Delete",
            isDestructive = true,
            onConfirm = {
                onDeleteProject(target.id)
                deleteTarget = null
            },
            onDismiss = { deleteTarget = null }
        )
    }
}

@Composable
private fun ProjectItemCard(
    project: ProjectEntity,
    onOpen: () -> Unit,
    onRename: () -> Unit,
    onToggleFavorite: () -> Unit,
    onExport: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit
) {
    LuxuryCard(onClick = onOpen) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = File(project.imagePath),
                contentDescription = project.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(74.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(ObsidianBlack)
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(project.title, style = MaterialTheme.typography.titleMedium, color = TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(project.toolName, style = MaterialTheme.typography.bodySmall, color = SecondaryGold, maxLines = 1)
                Text("${project.format} • ${project.fileSizeBytes / 1024}KB", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            }
            IconButton(onClick = onToggleFavorite) {
                Icon(
                    if (project.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (project.isFavorite) PrimaryGold else TextSecondary
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            TextButton(onClick = onRename) { Text("Rename", color = SecondaryGold) }
            TextButton(onClick = onExport) { Text("Gallery", color = TextPrimary) }
            TextButton(onClick = onShare) { Text("Share", color = TextPrimary) }
            TextButton(onClick = onDelete) { Text("Delete", color = StatusError) }
        }
    }
}
