package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.domain.StudioToolId
import com.example.domain.ToolCategory

@Database(
    entities = [
        UserEntity::class,
        ProjectEntity::class,
        HistoryEntity::class,
        CreditTransactionEntity::class,
        ToolConfigEntity::class,
        CategoryEntity::class,
        SubscriptionPlanEntity::class,
        PaymentRecordEntity::class,
        BannerEntity::class,
        NotificationEntity::class,
        SupportTicketEntity::class,
        AuditLogEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun projectDao(): ProjectDao
    abstract fun historyDao(): HistoryDao
    abstract fun creditTransactionDao(): CreditTransactionDao
    abstract fun toolDao(): ToolDao
    abstract fun categoryDao(): CategoryDao
    abstract fun subscriptionDao(): SubscriptionDao
    abstract fun paymentDao(): PaymentDao
    abstract fun bannerDao(): BannerDao
    abstract fun notificationDao(): NotificationDao
    abstract fun supportTicketDao(): SupportTicketDao
    abstract fun auditLogDao(): AuditLogDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ai_photo_studio.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}

object DatabaseSeeder {
    suspend fun seedIfNeeded(db: AppDatabase) {
        if (db.categoryDao().count() == 0) {
            db.categoryDao().insertAll(
                ToolCategory.entries.mapIndexed { idx, cat ->
                    CategoryEntity(
                        id = cat.id,
                        title = cat.title,
                        subtitle = cat.subtitle,
                        sortOrder = idx,
                        isActive = true
                    )
                }
            )
        }
        if (db.toolDao().count() == 0) {
            db.toolDao().insertAll(
                StudioToolId.entries.map { tool ->
                    ToolConfigEntity(
                        id = tool.id,
                        number = tool.number,
                        title = tool.title,
                        description = tool.description,
                        categoryId = tool.category.id,
                        creditCost = tool.defaultCredits,
                        isEnabled = true,
                        apiStatus = if (tool.category.requiresFirebaseAi) "REQUIRES_FIREBASE_AI" else "LOCAL_READY"
                    )
                }
            )
        }
        if (db.subscriptionDao().count() == 0) {
            db.subscriptionDao().insertAll(
                listOf(
                    SubscriptionPlanEntity(
                        id = "free",
                        name = "FREE",
                        priceUsd = 0.0,
                        monthlyCredits = 25,
                        featuresSummary = "25 starter credits • Full Creative & Advanced Studio • Local PNG/JPG/WEBP export",
                        isPopular = false,
                        isActive = true
                    ),
                    SubscriptionPlanEntity(
                        id = "pro",
                        name = "PRO",
                        priceUsd = 14.99,
                        monthlyCredits = 300,
                        featuresSummary = "300 monthly credits • All 40 Studio Tools • 4K export presets • Priority queue",
                        isPopular = true,
                        isActive = true
                    ),
                    SubscriptionPlanEntity(
                        id = "premium",
                        name = "PREMIUM",
                        priceUsd = 34.99,
                        monthlyCredits = 1000,
                        featuresSummary = "1,000 monthly credits • All 40 Studio Tools • Batch templates • Dedicated support",
                        isPopular = false,
                        isActive = true
                    )
                )
            )
        }
        if (db.bannerDao().count() == 0) {
            db.bannerDao().insert(
                BannerEntity(
                    title = "Creative & Advanced Studios Active",
                    subtitle = "All 20 Canvas & Bitmap editing tools run locally at full speed with zero wait.",
                    ctaLabel = "Open Studio",
                    targetRoute = "creative_hub",
                    isPublished = true
                )
            )
            db.bannerDao().insert(
                BannerEntity(
                    title = "Connect Firebase AI Logic for Live Gemini",
                    subtitle = "Enable Firebase in project setup to unlock live Gemini image generation & AI editing.",
                    ctaLabel = "Explore AI Tools",
                    targetRoute = "ai_edit_hub",
                    isPublished = true
                )
            )
        }
        if (db.notificationDao().count() == 0) {
            db.notificationDao().insert(
                NotificationEntity(
                    title = "Welcome to AI Photo Studio",
                    message = "Your local workspace is ready. Creative Studio (21-30) & Advanced Tools (31-40) export real PNG/JPG/WEBP files. AI tools (1-20) show labeled sample previews until Firebase AI Logic is enabled.",
                    isPublished = true
                )
            )
        }
    }
}
