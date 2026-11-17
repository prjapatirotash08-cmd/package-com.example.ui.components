package com.example.data.repository

import android.content.Context
import com.example.data.local.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.io.File
import java.util.UUID

class StudioRepository(
    private val db: AppDatabase,
    private val prefs: SessionPreferences
) {
    val activeUserId: Flow<Long?> = prefs.activeUserId
    val signupFreeCredits: Flow<Int> = prefs.signupFreeCredits
    val dailyBonusCredits: Flow<Int> = prefs.dailyBonusCredits
    val maintenanceMode: Flow<Boolean> = prefs.maintenanceMode

    fun observeUser(userId: Long): Flow<UserEntity?> = db.userDao().observeUser(userId)
    fun observeAllUsers(): Flow<List<UserEntity>> = db.userDao().observeAllUsers()
    fun observeUserProjects(userId: Long): Flow<List<ProjectEntity>> = db.projectDao().observeUserProjects(userId)
    fun observeUserFavorites(userId: Long): Flow<List<ProjectEntity>> = db.projectDao().observeUserFavorites(userId)
    fun observeAllProjects(): Flow<List<ProjectEntity>> = db.projectDao().observeAllProjects()
    fun observeUserHistory(userId: Long): Flow<List<HistoryEntity>> = db.historyDao().observeUserHistory(userId)
    fun observeAllHistory(): Flow<List<HistoryEntity>> = db.historyDao().observeAllHistory()
    fun observeUserTransactions(userId: Long): Flow<List<CreditTransactionEntity>> = db.creditTransactionDao().observeUserTransactions(userId)
    fun observeAllTransactions(): Flow<List<CreditTransactionEntity>> = db.creditTransactionDao().observeAllTransactions()
    fun observeAllTools(): Flow<List<ToolConfigEntity>> = db.toolDao().observeAllTools()
    fun observeAllCategories(): Flow<List<CategoryEntity>> = db.categoryDao().observeAllCategories()
    fun observeAllPlans(): Flow<List<SubscriptionPlanEntity>> = db.subscriptionDao().observeAllPlans()
    fun observeUserPayments(userId: Long): Flow<List<PaymentRecordEntity>> = db.paymentDao().observeUserPayments(userId)
    fun observeAllPayments(): Flow<List<PaymentRecordEntity>> = db.paymentDao().observeAllPayments()
    fun observePublishedBanners(): Flow<List<BannerEntity>> = db.bannerDao().observePublishedBanners()
    fun observeAllBanners(): Flow<List<BannerEntity>> = db.bannerDao().observeAllBanners()
    fun observePublishedNotifications(): Flow<List<NotificationEntity>> = db.notificationDao().observePublishedNotifications()
    fun observeAllNotifications(): Flow<List<NotificationEntity>> = db.notificationDao().observeAllNotifications()
    fun observeUserTickets(userId: Long): Flow<List<SupportTicketEntity>> = db.supportTicketDao().observeUserTickets(userId)
    fun observeAllTickets(): Flow<List<SupportTicketEntity>> = db.supportTicketDao().observeAllTickets()
    fun observeRecentAuditLogs(): Flow<List<AuditLogEntity>> = db.auditLogDao().observeRecentLogs()

    suspend fun initializeDatabase() {
        DatabaseSeeder.seedIfNeeded(db)
    }

    suspend fun hasAnyAdmin(): Boolean = db.userDao().getAnyAdmin() != null

    suspend fun registerUser(
        name: String,
        email: String,
        password: String,
        referralCodeInput: String = "",
        asAdmin: Boolean = false
    ): Result<UserEntity> = runCatching {
        val cleanEmail = email.trim().lowercase()
        val cleanName = name.trim()
        require(cleanName.length >= 2) { "Name must be at least 2 characters." }
        require(cleanEmail.contains("@") && cleanEmail.contains(".")) { "Please enter a valid email address." }
        require(password.length >= 6) { "Password must be at least 6 characters." }
        if (db.userDao().getByEmail(cleanEmail) != null) {
            error("An account with this email already exists.")
        }
        if (asAdmin && hasAnyAdmin()) {
            error("An administrator account already exists. Please sign in with admin credentials.")
        }

        val starterCredits = prefs.signupFreeCredits.first()
        val salt = PasswordHasher.generateSalt()
        val hash = PasswordHasher.hashPassword(password, salt)
        val myReferralCode = "AIPS-${UUID.randomUUID().toString().take(6).uppercase()}"

        val cleanRef = referralCodeInput.trim().uppercase()
        val referrer = if (cleanRef.isNotBlank()) db.userDao().getByReferralCode(cleanRef) else null
        val bonusForReferred = if (referrer != null) 10 else 0

        val newUser = UserEntity(
            name = cleanName,
            email = cleanEmail,
            passwordHash = hash,
            passwordSalt = salt,
            role = if (asAdmin) "ADMIN" else "USER",
            status = "ACTIVE",
            credits = starterCredits + bonusForReferred,
            planName = if (asAdmin) "PREMIUM" else "FREE",
            referralCode = myReferralCode,
            referredByCode = referrer?.referralCode
        )
        val userId = db.userDao().insert(newUser)
        val created = newUser.copy(id = userId)

        db.creditTransactionDao().insert(
            CreditTransactionEntity(
                userId = userId,
                amount = starterCredits,
                balanceAfter = starterCredits,
                type = "SIGNUP_BONUS",
                description = "Starter studio credits"
            )
        )
        if (referrer != null) {
            db.creditTransactionDao().insert(
                CreditTransactionEntity(
                    userId = userId,
                    amount = 10,
                    balanceAfter = created.credits,
                    type = "REFERRAL_BONUS",
                    description = "Referral signup bonus (${referrer.referralCode})"
                )
            )
            val updatedReferrer = referrer.copy(credits = referrer.credits + 15)
            db.userDao().update(updatedReferrer)
            db.creditTransactionDao().insert(
                CreditTransactionEntity(
                    userId = referrer.id,
                    amount = 15,
                    balanceAfter = updatedReferrer.credits,
                    type = "REFERRAL_BONUS",
                    description = "Referred $cleanEmail"
                )
            )
        }

        db.auditLogDao().insert(
            AuditLogEntity(
                actorEmail = cleanEmail,
                action = if (asAdmin) "ADMIN_SETUP" else "USER_REGISTERED",
                details = "Account created with ${created.credits} credits"
            )
        )
        prefs.setActiveUserId(userId)
        created
    }

    suspend fun loginUser(email: String, password: String, requireAdmin: Boolean = false): Result<UserEntity> =
        runCatching {
            val cleanEmail = email.trim().lowercase()
            val user = db.userDao().getByEmail(cleanEmail) ?: error("Invalid email or password.")
            if (!PasswordHasher.verify(password, user.passwordSalt, user.passwordHash)) {
                error("Invalid email or password.")
            }
            if (user.status == "SUSPENDED") {
                error("This account has been suspended by an administrator.")
            }
            if (requireAdmin && user.role != "ADMIN") {
                error("Administrator privileges are required for the Admin Portal.")
            }
            prefs.setActiveUserId(user.id)
            db.auditLogDao().insert(
                AuditLogEntity(
                    actorEmail = user.email,
                    action = if (requireAdmin) "ADMIN_LOGIN" else "USER_LOGIN",
                    details = "Signed in (${user.role})"
                )
            )
            user
        }

    suspend fun resetPassword(email: String, newPassword: String): Result<Unit> = runCatching {
        val cleanEmail = email.trim().lowercase()
        require(newPassword.length >= 6) { "New password must be at least 6 characters." }
        val user = db.userDao().getByEmail(cleanEmail) ?: error("No account found for $cleanEmail.")
        val newSalt = PasswordHasher.generateSalt()
        val newHash = PasswordHasher.hashPassword(newPassword, newSalt)
        db.userDao().update(user.copy(passwordSalt = newSalt, passwordHash = newHash))
        db.auditLogDao().insert(
            AuditLogEntity(
                actorEmail = cleanEmail,
                action = "PASSWORD_RESET",
                details = "Password updated locally"
            )
        )
    }

    suspend fun logout() {
        prefs.setActiveUserId(null)
    }

    suspend fun updateProfileName(userId: Long, newName: String): Result<Unit> = runCatching {
        val clean = newName.trim()
        require(clean.length >= 2) { "Display name must be at least 2 characters." }
        val user = db.userDao().getById(userId) ?: error("User not found")
        db.userDao().update(user.copy(name = clean))
    }

    suspend fun claimDailyFreeCredits(userId: Long): Result<Int> = runCatching {
        val user = db.userDao().getById(userId) ?: error("User not found")
        val now = System.currentTimeMillis()
        val oneDayMs = 24L * 60L * 60L * 1000L
        if (now - user.lastFreeClaimAt < oneDayMs) {
            val hoursLeft = ((oneDayMs - (now - user.lastFreeClaimAt)) / (1000L * 60L * 60L)).coerceAtLeast(1)
            error("Daily bonus already claimed. Next claim available in ~${hoursLeft}h.")
        }
        val bonus = prefs.dailyBonusCredits.first()
        val updated = user.copy(credits = user.credits + bonus, lastFreeClaimAt = now)
        db.userDao().update(updated)
        db.creditTransactionDao().insert(
            CreditTransactionEntity(
                userId = userId,
                amount = bonus,
                balanceAfter = updated.credits,
                type = "DAILY_BONUS",
                description = "Daily free studio credit claim"
            )
        )
        bonus
    }

    suspend fun applyReferralCode(userId: Long, code: String): Result<Int> = runCatching {
        val user = db.userDao().getById(userId) ?: error("User not found")
        if (!user.referredByCode.isNullOrBlank()) {
            error("You have already redeemed a referral code (${user.referredByCode}).")
        }
        val clean = code.trim().uppercase()
        require(clean.isNotBlank()) { "Enter a referral code." }
        if (clean == user.referralCode) error("You cannot redeem your own referral code.")
        val referrer = db.userDao().getByReferralCode(clean) ?: error("Referral code not found.")

        val updatedUser = user.copy(credits = user.credits + 10, referredByCode = referrer.referralCode)
        db.userDao().update(updatedUser)
        db.creditTransactionDao().insert(
            CreditTransactionEntity(
                userId = userId,
                amount = 10,
                balanceAfter = updatedUser.credits,
                type = "REFERRAL_BONUS",
                description = "Redeemed referral code ${referrer.referralCode}"
            )
        )
        val updatedReferrer = referrer.copy(credits = referrer.credits + 15)
        db.userDao().update(updatedReferrer)
        db.creditTransactionDao().insert(
            CreditTransactionEntity(
                userId = referrer.id,
                amount = 15,
                balanceAfter = updatedReferrer.credits,
                type = "REFERRAL_BONUS",
                description = "Referral reward from ${user.email}"
            )
        )
        10
    }

    suspend fun reserveCreditsForTool(userId: Long, toolId: String, multiplier: Int = 1): Result<Int> =
        runCatching {
            val user = db.userDao().getById(userId) ?: error("User not found")
            if (user.status == "SUSPENDED") error("Account suspended.")
            val tool = db.toolDao().getById(toolId) ?: error("Tool not found")
            if (!tool.isEnabled) error("${tool.title} is currently disabled by admin.")
            val cost = (tool.creditCost * multiplier.coerceAtLeast(1)).coerceAtLeast(0)
            if (cost > 0 && user.credits < cost) {
                error("Insufficient credits ($cost required, ${user.credits} available).")
            }
            if (cost > 0) {
                val updated = user.copy(credits = user.credits - cost)
                db.userDao().update(updated)
                db.creditTransactionDao().insert(
                    CreditTransactionEntity(
                        userId = userId,
                        amount = -cost,
                        balanceAfter = updated.credits,
                        type = "TOOL_USAGE",
                        description = "Used ${tool.title}"
                    )
                )
            }
            cost
        }

    suspend fun refundCredits(userId: Long, amount: Int, reason: String) {
        if (amount <= 0) return
        val user = db.userDao().getById(userId) ?: return
        val updated = user.copy(credits = user.credits + amount)
        db.userDao().update(updated)
        db.creditTransactionDao().insert(
            CreditTransactionEntity(
                userId = userId,
                amount = amount,
                balanceAfter = updated.credits,
                type = "REFUND",
                description = reason
            )
        )
    }

    suspend fun recordHistory(
        userId: Long,
        toolId: String,
        toolName: String,
        categoryId: String,
        inputSummary: String,
        outputPath: String?,
        creditsUsed: Int,
        status: String
    ) {
        db.historyDao().insert(
            HistoryEntity(
                userId = userId,
                toolId = toolId,
                toolName = toolName,
                categoryId = categoryId,
                inputSummary = inputSummary,
                outputPath = outputPath,
                creditsUsed = creditsUsed,
                status = status
            )
        )
    }

    suspend fun saveProject(
        userId: Long,
        title: String,
        toolId: String,
        toolName: String,
        categoryId: String,
        imagePath: String,
        format: String,
        fileSizeBytes: Long,
        widthPx: Int,
        heightPx: Int,
        exportedToGallery: Boolean = false
    ): Long {
        return db.projectDao().insert(
            ProjectEntity(
                userId = userId,
                title = title.ifBlank { "$toolName Output" },
                toolId = toolId,
                toolName = toolName,
                categoryId = categoryId,
                imagePath = imagePath,
                format = format,
                fileSizeBytes = fileSizeBytes,
                widthPx = widthPx,
                heightPx = heightPx,
                isFavorite = false,
                isExportedToGallery = exportedToGallery
            )
        )
    }

    suspend fun renameProject(projectId: Long, newTitle: String) {
        val project = db.projectDao().getById(projectId) ?: return
        val clean = newTitle.trim()
        if (clean.isNotBlank()) {
            db.projectDao().update(project.copy(title = clean, updatedAt = System.currentTimeMillis()))
        }
    }

    suspend fun toggleFavorite(projectId: Long) {
        val project = db.projectDao().getById(projectId) ?: return
        db.projectDao().update(project.copy(isFavorite = !project.isFavorite, updatedAt = System.currentTimeMillis()))
    }

    suspend fun markExported(projectId: Long) {
        val project = db.projectDao().getById(projectId) ?: return
        db.projectDao().update(project.copy(isExportedToGallery = true, updatedAt = System.currentTimeMillis()))
    }

    suspend fun deleteProject(projectId: Long) {
        val project = db.projectDao().getById(projectId)
        if (project != null) {
            runCatching { File(project.imagePath).delete() }
            db.projectDao().deleteById(projectId)
        }
    }

    suspend fun createSubscriptionOrder(userId: Long, plan: SubscriptionPlanEntity): Result<PaymentRecordEntity> =
        runCatching {
            val user = db.userDao().getById(userId) ?: error("User not found")
            val ref = "ORD-${UUID.randomUUID().toString().take(8).uppercase()}"
            val record = PaymentRecordEntity(
                userId = userId,
                userEmail = user.email,
                planId = plan.id,
                planName = plan.name,
                amountUsd = plan.priceUsd,
                transactionRef = ref,
                status = if (plan.priceUsd <= 0.0) "FREE_TIER_ACTIVE" else "PENDING_WEBHOOK_VERIFICATION",
                note = if (plan.priceUsd <= 0.0) {
                    "Free plan active"
                } else {
                    "Payment gateway webhook not configured. Configure Stripe/Razorpay server webhook to verify & activate ${plan.name}."
                }
            )
            val id = db.paymentDao().insert(record)
            db.auditLogDao().insert(
                AuditLogEntity(
                    actorEmail = user.email,
                    action = "SUBSCRIPTION_ORDER_CREATED",
                    details = "${plan.name} ($ref) — ${record.status}"
                )
            )
            record.copy(id = id)
        }

    suspend fun submitSupportTicket(userId: Long, subject: String, message: String): Result<Unit> =
        runCatching {
            val user = db.userDao().getById(userId) ?: error("User not found")
            require(subject.trim().length >= 3) { "Please enter a brief subject." }
            require(message.trim().length >= 8) { "Please describe your issue (min 8 characters)." }
            db.supportTicketDao().insert(
                SupportTicketEntity(
                    userId = userId,
                    userEmail = user.email,
                    subject = subject.trim(),
                    message = message.trim()
                )
            )
        }

    // --- Admin Management Operations ---

    suspend fun adminToggleUserStatus(adminEmail: String, targetUser: UserEntity) {
        val newStatus = if (targetUser.status == "ACTIVE") "SUSPENDED" else "ACTIVE"
        db.userDao().update(targetUser.copy(status = newStatus))
        db.auditLogDao().insert(
            AuditLogEntity(
                actorEmail = adminEmail,
                action = "ADMIN_USER_STATUS",
                details = "Set ${targetUser.email} to $newStatus"
            )
        )
    }

    suspend fun adminAdjustCredits(adminEmail: String, targetUser: UserEntity, delta: Int, reason: String): Result<Unit> =
        runCatching {
            require(delta != 0) { "Credit adjustment cannot be zero." }
            val newBal = (targetUser.credits + delta).coerceAtLeast(0)
            db.userDao().update(targetUser.copy(credits = newBal))
            db.creditTransactionDao().insert(
                CreditTransactionEntity(
                    userId = targetUser.id,
                    amount = delta,
                    balanceAfter = newBal,
                    type = if (delta > 0) "ADMIN_GRANT" else "ADMIN_DEDUCT",
                    description = reason.ifBlank { "Admin adjustment by $adminEmail" }
                )
            )
            db.auditLogDao().insert(
                AuditLogEntity(
                    actorEmail = adminEmail,
                    action = "ADMIN_CREDIT_ADJUST",
                    details = "${targetUser.email}: $delta credits (new balance: $newBal)"
                )
            )
        }

    suspend fun adminUpdateTool(adminEmail: String, tool: ToolConfigEntity) {
        db.toolDao().update(tool)
        db.auditLogDao().insert(
            AuditLogEntity(
                actorEmail = adminEmail,
                action = "ADMIN_TOOL_UPDATED",
                details = "${tool.title} (enabled=${tool.isEnabled}, cost=${tool.creditCost})"
            )
        )
    }

    suspend fun adminSaveCategory(adminEmail: String, category: CategoryEntity) {
        db.categoryDao().insert(category)
        db.auditLogDao().insert(
            AuditLogEntity(
                actorEmail = adminEmail,
                action = "ADMIN_CATEGORY_SAVED",
                details = "${category.title} (active=${category.isActive})"
            )
        )
    }

    suspend fun adminSavePlan(adminEmail: String, plan: SubscriptionPlanEntity) {
        db.subscriptionDao().upsert(plan)
        db.auditLogDao().insert(
            AuditLogEntity(
                actorEmail = adminEmail,
                action = "ADMIN_PLAN_SAVED",
                details = "${plan.name} ($${plan.priceUsd}, ${plan.monthlyCredits} credits)"
            )
        )
    }

    suspend fun adminSaveBanner(adminEmail: String, banner: BannerEntity) {
        if (banner.id == 0L) db.bannerDao().insert(banner) else db.bannerDao().update(banner)
        db.auditLogDao().insert(
            AuditLogEntity(
                actorEmail = adminEmail,
                action = "ADMIN_BANNER_SAVED",
                details = banner.title
            )
        )
    }

    suspend fun adminDeleteBanner(adminEmail: String, id: Long) {
        db.bannerDao().deleteById(id)
        db.auditLogDao().insert(AuditLogEntity(actorEmail = adminEmail, action = "ADMIN_BANNER_DELETED", details = "Banner #$id"))
    }

    suspend fun adminSaveNotification(adminEmail: String, notification: NotificationEntity) {
        if (notification.id == 0L) db.notificationDao().insert(notification) else db.notificationDao().update(notification)
        db.auditLogDao().insert(
            AuditLogEntity(
                actorEmail = adminEmail,
                action = "ADMIN_NOTIFICATION_SAVED",
                details = notification.title
            )
        )
    }

    suspend fun adminDeleteNotification(adminEmail: String, id: Long) {
        db.notificationDao().deleteById(id)
        db.auditLogDao().insert(AuditLogEntity(actorEmail = adminEmail, action = "ADMIN_NOTIFICATION_DELETED", details = "Notification #$id"))
    }

    suspend fun adminReplyTicket(adminEmail: String, ticket: SupportTicketEntity, reply: String) {
        db.supportTicketDao().update(ticket.copy(adminReply = reply.trim(), status = "RESOLVED"))
        db.auditLogDao().insert(
            AuditLogEntity(
                actorEmail = adminEmail,
                action = "ADMIN_TICKET_RESOLVED",
                details = "Ticket #${ticket.id} (${ticket.userEmail})"
            )
        )
    }

    suspend fun adminUpdateSettings(adminEmail: String, signupFree: Int, dailyBonus: Int, maintenance: Boolean) {
        prefs.updateAppSettings(signupFree, dailyBonus, maintenance)
        db.auditLogDao().insert(
            AuditLogEntity(
                actorEmail = adminEmail,
                action = "ADMIN_SETTINGS_UPDATED",
                details = "signupFree=$signupFree, dailyBonus=$dailyBonus, maintenance=$maintenance"
            )
        )
    }

    companion object {
        fun create(context: Context): StudioRepository {
            val db = AppDatabase.getInstance(context)
            val prefs = SessionPreferences(context)
            return StudioRepository(db, prefs)
        }
    }
}
