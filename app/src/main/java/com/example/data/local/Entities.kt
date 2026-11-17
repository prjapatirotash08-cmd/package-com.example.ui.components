package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val email: String,
    val passwordHash: String,
    val passwordSalt: String,
    val role: String = "USER", // USER or ADMIN
    val status: String = "ACTIVE", // ACTIVE or SUSPENDED
    val credits: Int = 25,
    val planName: String = "FREE", // FREE, PRO, PREMIUM
    val referralCode: String,
    val referredByCode: String? = null,
    val lastFreeClaimAt: Long = 0L,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val userId: Long,
    val title: String,
    val toolId: String,
    val toolName: String,
    val categoryId: String,
    val imagePath: String,
    val format: String = "PNG",
    val fileSizeBytes: Long = 0L,
    val widthPx: Int = 0,
    val heightPx: Int = 0,
    val isFavorite: Boolean = false,
    val isExportedToGallery: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "editing_history")
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val userId: Long,
    val toolId: String,
    val toolName: String,
    val categoryId: String,
    val inputSummary: String,
    val outputPath: String? = null,
    val creditsUsed: Int = 0,
    val status: String = "SUCCESS", // SUCCESS, SAMPLE_PREVIEW, REFUNDED, FAILED
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "credit_transactions")
data class CreditTransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val userId: Long,
    val amount: Int,
    val balanceAfter: Int,
    val type: String, // SIGNUP_BONUS, TOOL_USAGE, REFUND, ADMIN_GRANT, ADMIN_DEDUCT, DAILY_BONUS, REFERRAL_BONUS
    val description: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "tools")
data class ToolConfigEntity(
    @PrimaryKey val id: String,
    val number: Int,
    val title: String,
    val description: String,
    val categoryId: String,
    val creditCost: Int,
    val isEnabled: Boolean = true,
    val apiStatus: String = "READY" // READY (Local Engine) or REQUIRES_FIREBASE_AI
)

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val id: String,
    val title: String,
    val subtitle: String,
    val sortOrder: Int,
    val isActive: Boolean = true
)

@Entity(tableName = "subscriptions")
data class SubscriptionPlanEntity(
    @PrimaryKey val id: String,
    val name: String,
    val priceUsd: Double,
    val monthlyCredits: Int,
    val featuresSummary: String,
    val isPopular: Boolean = false,
    val isActive: Boolean = true
)

@Entity(tableName = "payments")
data class PaymentRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val userId: Long,
    val userEmail: String,
    val planId: String,
    val planName: String,
    val amountUsd: Double,
    val transactionRef: String,
    val status: String = "PENDING_WEBHOOK_VERIFICATION", // PENDING_WEBHOOK_VERIFICATION, CONFIG_REQUIRED
    val note: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "banners")
data class BannerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val title: String,
    val subtitle: String,
    val ctaLabel: String,
    val targetRoute: String,
    val isPublished: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val title: String,
    val message: String,
    val isPublished: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "support_tickets")
data class SupportTicketEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val userId: Long,
    val userEmail: String,
    val subject: String,
    val message: String,
    val status: String = "OPEN", // OPEN, RESOLVED
    val adminReply: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "audit_logs")
data class AuditLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val actorEmail: String,
    val action: String,
    val details: String,
    val createdAt: Long = System.currentTimeMillis()
)
