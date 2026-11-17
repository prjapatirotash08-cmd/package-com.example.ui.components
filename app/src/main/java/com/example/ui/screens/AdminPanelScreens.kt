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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.*
import com.example.ui.components.*
import com.example.ui.theme.*

enum class AdminTab(val label: String) {
    OVERVIEW("Dashboard"),
    USERS("Users"),
    TOOLS("AI Tools"),
    CATEGORIES("Categories"),
    CREDITS("Credits"),
    PLANS("Subscriptions"),
    PAYMENTS("Payments"),
    BANNERS("Banners"),
    NOTIFICATIONS("Notifications"),
    ANALYTICS("Analytics & Settings")
}

@Composable
fun AdminPanelScreen(
    currentUser: UserEntity,
    allUsers: List<UserEntity>,
    allProjects: List<ProjectEntity>,
    allHistory: List<HistoryEntity>,
    allTransactions: List<CreditTransactionEntity>,
    allTools: List<ToolConfigEntity>,
    allCategories: List<CategoryEntity>,
    allPlans: List<SubscriptionPlanEntity>,
    allPayments: List<PaymentRecordEntity>,
    allBanners: List<BannerEntity>,
    allNotifications: List<NotificationEntity>,
    allTickets: List<SupportTicketEntity>,
    auditLogs: List<AuditLogEntity>,
    signupFreeCredits: Int,
    dailyBonusCredits: Int,
    maintenanceMode: Boolean,
    onToggleUserStatus: (UserEntity) -> Unit,
    onAdjustUserCredits: (UserEntity, Int, String) -> Unit,
    onUpdateTool: (ToolConfigEntity) -> Unit,
    onSaveCategory: (CategoryEntity) -> Unit,
    onSavePlan: (SubscriptionPlanEntity) -> Unit,
    onSaveBanner: (BannerEntity) -> Unit,
    onDeleteBanner: (Long) -> Unit,
    onSaveNotification: (NotificationEntity) -> Unit,
    onDeleteNotification: (Long) -> Unit,
    onReplyTicket: (SupportTicketEntity, String) -> Unit,
    onUpdateSettings: (Int, Int, Boolean) -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)

    // Strict role guard
    if (currentUser.role != "ADMIN") {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(ObsidianBlack)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Filled.Lock, contentDescription = null, tint = StatusError, modifier = Modifier.size(48.dp))
            Spacer(Modifier.height(12.dp))
            Text("Administrator Role Required", style = MaterialTheme.typography.titleLarge, color = TextPrimary)
            Spacer(Modifier.height(8.dp))
            GoldOutlinedButton(text = "Return to Studio", onClick = onBack)
        }
        return
    }

    var activeTab by remember { mutableStateOf(AdminTab.OVERVIEW) }
    var userSearch by remember { mutableStateOf("") }
    var creditTargetUser by remember { mutableStateOf<UserEntity?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBlack)
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = PrimaryGold)
                    }
                    Column {
                        Text("Admin Control Panel", style = MaterialTheme.typography.headlineMedium, color = SecondaryGold)
                        Text("Signed in: ${currentUser.email}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AdminTab.entries.forEach { tab ->
                    FilterChip(
                        selected = activeTab == tab,
                        onClick = { activeTab = tab },
                        label = { Text(tab.label) },
                        modifier = Modifier.testTag("admin_tab_${tab.name.lowercase()}")
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
                AdminTab.OVERVIEW -> {
                    val activeUsersCount = allUsers.count { it.status == "ACTIVE" }
                    val creditsConsumed = allTransactions.filter { it.amount < 0 }.sumOf { -it.amount }
                    val openTickets = allTickets.count { it.status == "OPEN" }
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                            AdminMetricCard("Total Users", "${allUsers.size}", Modifier.weight(1f))
                            AdminMetricCard("Active Users", "$activeUsersCount", Modifier.weight(1f))
                        }
                    }
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                            AdminMetricCard("Tool Runs", "${allHistory.size}", Modifier.weight(1f))
                            AdminMetricCard("Saved Projects", "${allProjects.size}", Modifier.weight(1f))
                        }
                    }
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                            AdminMetricCard("Credits Spent", "$creditsConsumed", Modifier.weight(1f))
                            AdminMetricCard("Open Tickets", "$openTickets", Modifier.weight(1f))
                        }
                    }
                    item { SectionHeader(title = "Recent Audit Activity") }
                    items(auditLogs.take(12), key = { it.id }) { log ->
                        LuxuryCard {
                            Text("${log.action} • ${log.actorEmail}", style = MaterialTheme.typography.titleSmall, color = SecondaryGold)
                            Text(log.details, style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                            Text(formatTimestamp(log.createdAt), style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                        }
                    }
                }

                AdminTab.USERS -> {
                    item {
                        OutlinedTextField(
                            value = userSearch,
                            onValueChange = { userSearch = it },
                            label = { Text("Search users by name or email") },
                            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = PrimaryGold) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    val filtered = allUsers.filter {
                        userSearch.isBlank() ||
                            it.name.contains(userSearch, true) ||
                            it.email.contains(userSearch, true)
                    }
                    items(filtered, key = { it.id }) { u ->
                        LuxuryCard {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("${u.name} (${u.role})", style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                                    Text(u.email, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                    Text(
                                        "Credits: ${u.credits} • Plan: ${u.planName} • Status: ${u.status}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (u.status == "ACTIVE") StatusSuccess else StatusError
                                    )
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                GoldOutlinedButton(
                                    text = "Adjust Credits",
                                    onClick = { creditTargetUser = u },
                                    modifier = Modifier.weight(1f)
                                )
                                if (u.id != currentUser.id) {
                                    OutlinedButton(
                                        onClick = { onToggleUserStatus(u) },
                                        border = BorderStroke(1.dp, if (u.status == "ACTIVE") StatusError else StatusSuccess),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            contentColor = if (u.status == "ACTIVE") StatusError else StatusSuccess
                                        ),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(if (u.status == "ACTIVE") "Suspend" else "Reactivate")
                                    }
                                }
                            }
                        }
                    }
                }

                AdminTab.TOOLS -> {
                    items(allTools, key = { it.id }) { tool ->
                        var costInput by remember(tool.id, tool.creditCost) { mutableStateOf(tool.creditCost.toString()) }
                        LuxuryCard {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("#${tool.number} ${tool.title}", style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                                    Text("${tool.categoryId} • ${tool.apiStatus}", style = MaterialTheme.typography.labelSmall, color = SecondaryGold)
                                }
                                Switch(
                                    checked = tool.isEnabled,
                                    onCheckedChange = { enabled -> onUpdateTool(tool.copy(isEnabled = enabled)) }
                                )
                            }
                            Spacer(Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = costInput,
                                    onValueChange = { costInput = it.filter(Char::isDigit) },
                                    label = { Text("Credit Cost") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                                GoldOutlinedButton(
                                    text = "Save Cost",
                                    onClick = {
                                        val parsed = costInput.toIntOrNull() ?: tool.creditCost
                                        onUpdateTool(tool.copy(creditCost = parsed.coerceIn(0, 100)))
                                    }
                                )
                            }
                        }
                    }
                }

                AdminTab.CATEGORIES -> {
                    item {
                        var newId by remember { mutableStateOf("") }
                        var newTitle by remember { mutableStateOf("") }
                        var newSub by remember { mutableStateOf("") }
                        LuxuryCard(highlighted = true) {
                            Text("Create / Update Category", style = MaterialTheme.typography.titleMedium, color = SecondaryGold)
                            Spacer(Modifier.height(8.dp))
                            OutlinedTextField(value = newId, onValueChange = { newId = it }, label = { Text("Category ID (slug)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                            Spacer(Modifier.height(6.dp))
                            OutlinedTextField(value = newTitle, onValueChange = { newTitle = it }, label = { Text("Title") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                            Spacer(Modifier.height(6.dp))
                            OutlinedTextField(value = newSub, onValueChange = { newSub = it }, label = { Text("Subtitle") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                            Spacer(Modifier.height(10.dp))
                            GoldGradientButton(
                                text = "Save Category",
                                onClick = {
                                    if (newId.isNotBlank() && newTitle.isNotBlank()) {
                                        onSaveCategory(
                                            CategoryEntity(
                                                id = newId.trim().lowercase().replace(" ", "_"),
                                                title = newTitle.trim(),
                                                subtitle = newSub.trim(),
                                                sortOrder = allCategories.size,
                                                isActive = true
                                            )
                                        )
                                        newId = ""; newTitle = ""; newSub = ""
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                    items(allCategories, key = { it.id }) { cat ->
                        LuxuryCard {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(cat.title, style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                                    Text(cat.subtitle, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                }
                                Switch(
                                    checked = cat.isActive,
                                    onCheckedChange = { onSaveCategory(cat.copy(isActive = it)) }
                                )
                            }
                        }
                    }
                }

                AdminTab.CREDITS -> {
                    item {
                        var signupInput by remember(signupFreeCredits) { mutableStateOf(signupFreeCredits.toString()) }
                        var dailyInput by remember(dailyBonusCredits) { mutableStateOf(dailyBonusCredits.toString()) }
                        LuxuryCard(highlighted = true) {
                            Text("Global Free Credit Rules", style = MaterialTheme.typography.titleMedium, color = SecondaryGold)
                            Spacer(Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = signupInput,
                                    onValueChange = { signupInput = it.filter(Char::isDigit) },
                                    label = { Text("Signup Credits") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = dailyInput,
                                    onValueChange = { dailyInput = it.filter(Char::isDigit) },
                                    label = { Text("Daily Bonus") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            Spacer(Modifier.height(10.dp))
                            GoldGradientButton(
                                text = "Save Credit Configuration",
                                onClick = {
                                    onUpdateSettings(
                                        signupInput.toIntOrNull() ?: signupFreeCredits,
                                        dailyInput.toIntOrNull() ?: dailyBonusCredits,
                                        maintenanceMode
                                    )
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                    item { SectionHeader(title = "All Credit Transactions") }
                    items(allTransactions, key = { it.id }) { tx ->
                        LuxuryCard {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("User #${tx.userId} • ${tx.description}", style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                                    Text("${tx.type} • Bal: ${tx.balanceAfter}", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                                }
                                Text(
                                    if (tx.amount >= 0) "+${tx.amount}" else "${tx.amount}",
                                    color = if (tx.amount >= 0) StatusSuccess else StatusError,
                                    style = MaterialTheme.typography.titleSmall
                                )
                            }
                        }
                    }
                }

                AdminTab.PLANS -> {
                    item {
                        var planId by remember { mutableStateOf("") }
                        var planName by remember { mutableStateOf("") }
                        var planPrice by remember { mutableStateOf("19.99") }
                        var planCredits by remember { mutableStateOf("500") }
                        var planFeatures by remember { mutableStateOf("") }
                        LuxuryCard(highlighted = true) {
                            Text("Create / Edit Subscription Plan", style = MaterialTheme.typography.titleMedium, color = SecondaryGold)
                            Spacer(Modifier.height(8.dp))
                            OutlinedTextField(value = planId, onValueChange = { planId = it }, label = { Text("Plan ID (e.g., studio_plus)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                            Spacer(Modifier.height(6.dp))
                            OutlinedTextField(value = planName, onValueChange = { planName = it }, label = { Text("Plan Name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                            Spacer(Modifier.height(6.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(value = planPrice, onValueChange = { planPrice = it }, label = { Text("Price USD") }, singleLine = true, modifier = Modifier.weight(1f))
                                OutlinedTextField(value = planCredits, onValueChange = { planCredits = it.filter(Char::isDigit) }, label = { Text("Monthly Credits") }, singleLine = true, modifier = Modifier.weight(1f))
                            }
                            Spacer(Modifier.height(6.dp))
                            OutlinedTextField(value = planFeatures, onValueChange = { planFeatures = it }, label = { Text("Features Summary") }, modifier = Modifier.fillMaxWidth())
                            Spacer(Modifier.height(10.dp))
                            GoldGradientButton(
                                text = "Save Plan",
                                onClick = {
                                    if (planId.isNotBlank() && planName.isNotBlank()) {
                                        onSavePlan(
                                            SubscriptionPlanEntity(
                                                id = planId.trim().lowercase(),
                                                name = planName.trim().uppercase(),
                                                priceUsd = planPrice.toDoubleOrNull() ?: 0.0,
                                                monthlyCredits = planCredits.toIntOrNull() ?: 100,
                                                featuresSummary = planFeatures.ifBlank { "Custom studio plan" },
                                                isPopular = false,
                                                isActive = true
                                            )
                                        )
                                        planId = ""; planName = ""; planFeatures = ""
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                    items(allPlans, key = { it.id }) { p ->
                        LuxuryCard {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("${p.name} • $${"%.2f".format(p.priceUsd)}/mo", style = MaterialTheme.typography.titleSmall, color = SecondaryGold)
                                    Text("${p.monthlyCredits} credits • ${p.featuresSummary}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                }
                                Switch(
                                    checked = p.isActive,
                                    onCheckedChange = { onSavePlan(p.copy(isActive = it)) }
                                )
                            }
                        }
                    }
                }

                AdminTab.PAYMENTS -> {
                    item {
                        LuxuryCard(highlighted = true) {
                            Text("Payment Webhook Verification", style = MaterialTheme.typography.titleMedium, color = SecondaryGold)
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "Orders remain in PENDING_WEBHOOK_VERIFICATION until a server-side Stripe or Razorpay webhook confirms settlement. Never mark an order paid from client state alone.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                    if (allPayments.isEmpty()) {
                        item {
                            EmptyStateCard(
                                icon = Icons.Filled.ReceiptLong,
                                title = "No Payment Records",
                                subtitle = "Subscription order attempts appear here with transaction IDs and webhook status."
                            )
                        }
                    } else {
                        items(allPayments, key = { it.id }) { pay ->
                            LuxuryCard {
                                Text("${pay.transactionRef} • ${pay.planName} ($${"%.2f".format(pay.amountUsd)})", style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                                Text("User: ${pay.userEmail}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                Text("Status: ${pay.status}", style = MaterialTheme.typography.labelSmall, color = StatusWarning)
                            }
                        }
                    }
                }

                AdminTab.BANNERS -> {
                    item {
                        var bTitle by remember { mutableStateOf("") }
                        var bSub by remember { mutableStateOf("") }
                        var bCta by remember { mutableStateOf("Explore") }
                        LuxuryCard(highlighted = true) {
                            Text("Publish New Studio Banner", style = MaterialTheme.typography.titleMedium, color = SecondaryGold)
                            Spacer(Modifier.height(8.dp))
                            OutlinedTextField(value = bTitle, onValueChange = { bTitle = it }, label = { Text("Banner Title") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                            Spacer(Modifier.height(6.dp))
                            OutlinedTextField(value = bSub, onValueChange = { bSub = it }, label = { Text("Banner Subtitle") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                            Spacer(Modifier.height(6.dp))
                            OutlinedTextField(value = bCta, onValueChange = { bCta = it }, label = { Text("CTA Label") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                            Spacer(Modifier.height(10.dp))
                            GoldGradientButton(
                                text = "Publish Banner",
                                onClick = {
                                    if (bTitle.isNotBlank()) {
                                        onSaveBanner(
                                            BannerEntity(
                                                title = bTitle.trim(),
                                                subtitle = bSub.trim(),
                                                ctaLabel = bCta.ifBlank { "Explore" },
                                                targetRoute = "creative_hub",
                                                isPublished = true
                                            )
                                        )
                                        bTitle = ""; bSub = ""
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                    items(allBanners, key = { it.id }) { banner ->
                        LuxuryCard {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(banner.title, style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                                    Text(banner.subtitle, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Switch(
                                        checked = banner.isPublished,
                                        onCheckedChange = { onSaveBanner(banner.copy(isPublished = it)) }
                                    )
                                    IconButton(onClick = { onDeleteBanner(banner.id) }) {
                                        Icon(Icons.Filled.Delete, contentDescription = "Delete Banner", tint = StatusError)
                                    }
                                }
                            }
                        }
                    }
                }

                AdminTab.NOTIFICATIONS -> {
                    item {
                        var nTitle by remember { mutableStateOf("") }
                        var nMsg by remember { mutableStateOf("") }
                        LuxuryCard(highlighted = true) {
                            Text("Publish Notification", style = MaterialTheme.typography.titleMedium, color = SecondaryGold)
                            Spacer(Modifier.height(8.dp))
                            OutlinedTextField(value = nTitle, onValueChange = { nTitle = it }, label = { Text("Notification Title") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                            Spacer(Modifier.height(6.dp))
                            OutlinedTextField(value = nMsg, onValueChange = { nMsg = it }, label = { Text("Message") }, minLines = 2, modifier = Modifier.fillMaxWidth())
                            Spacer(Modifier.height(10.dp))
                            GoldGradientButton(
                                text = "Send Notification",
                                onClick = {
                                    if (nTitle.isNotBlank() && nMsg.isNotBlank()) {
                                        onSaveNotification(NotificationEntity(title = nTitle.trim(), message = nMsg.trim(), isPublished = true))
                                        nTitle = ""; nMsg = ""
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                    items(allNotifications, key = { it.id }) { notif ->
                        LuxuryCard {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(notif.title, style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                                    Text(notif.message, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                }
                                IconButton(onClick = { onDeleteNotification(notif.id) }) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = StatusError)
                                }
                            }
                        }
                    }
                }

                AdminTab.ANALYTICS -> {
                    item {
                        var maint by remember(maintenanceMode) { mutableStateOf(maintenanceMode) }
                        LuxuryCard(highlighted = true) {
                            Text("Application Settings", style = MaterialTheme.typography.titleMedium, color = SecondaryGold)
                            Spacer(Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Maintenance Mode", style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                                Switch(
                                    checked = maint,
                                    onCheckedChange = {
                                        maint = it
                                        onUpdateSettings(signupFreeCredits, dailyBonusCredits, it)
                                    }
                                )
                            }
                        }
                    }

                    item { SectionHeader(title = "Tool Usage Analytics (Live DB)") }
                    val groupedUsage = allHistory.groupBy { it.toolName }.entries.sortedByDescending { it.value.size }
                    if (groupedUsage.isEmpty()) {
                        item {
                            LuxuryCard {
                                Text("No tool runs recorded yet.", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            }
                        }
                    } else {
                        items(groupedUsage.take(10)) { (toolName, runs) ->
                            LuxuryCard {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(toolName, style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                                    Text("${runs.size} runs", style = MaterialTheme.typography.labelMedium, color = SecondaryGold)
                                }
                            }
                        }
                    }

                    item { SectionHeader(title = "Support Tickets") }
                    items(allTickets, key = { it.id }) { ticket ->
                        var reply by remember(ticket.id) { mutableStateOf(ticket.adminReply.orEmpty()) }
                        LuxuryCard {
                            Text("${ticket.subject} (${ticket.status})", style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                            Text("From: ${ticket.userEmail}", style = MaterialTheme.typography.labelSmall, color = SecondaryGold)
                            Text(ticket.message, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            if (ticket.status == "OPEN") {
                                Spacer(Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = reply,
                                    onValueChange = { reply = it },
                                    label = { Text("Admin Reply") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(Modifier.height(6.dp))
                                GoldOutlinedButton(
                                    text = "Resolve Ticket",
                                    onClick = { if (reply.isNotBlank()) onReplyTicket(ticket, reply) },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    creditTargetUser?.let { target ->
        var deltaText by remember { mutableStateOf("25") }
        var reason by remember { mutableStateOf("Admin studio credit grant") }
        AlertDialog(
            onDismissRequest = { creditTargetUser = null },
            containerColor = CardObsidian,
            title = { Text("Adjust Credits: ${target.name}", color = TextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Current Balance: ${target.credits} credits", color = SecondaryGold, style = MaterialTheme.typography.bodySmall)
                    OutlinedTextField(
                        value = deltaText,
                        onValueChange = { deltaText = it },
                        label = { Text("Amount (e.g., 25 or -10)") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = reason,
                        onValueChange = { reason = it },
                        label = { Text("Reason") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val delta = deltaText.toIntOrNull() ?: 0
                        if (delta != 0) onAdjustUserCredits(target, delta, reason)
                        creditTargetUser = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGold, contentColor = ObsidianBlack)
                ) { Text("Apply") }
            },
            dismissButton = {
                TextButton(onClick = { creditTargetUser = null }) { Text("Cancel", color = TextSecondary) }
            }
        )
    }
}

@Composable
private fun AdminMetricCard(label: String, value: String, modifier: Modifier = Modifier) {
    LuxuryCard(modifier = modifier, highlighted = true) {
        Text(value, style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold), color = SecondaryGold)
        Text(label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
    }
}
