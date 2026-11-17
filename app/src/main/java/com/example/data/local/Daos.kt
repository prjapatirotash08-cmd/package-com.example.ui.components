package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    fun observeUser(id: Long): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): UserEntity?

    @Query("SELECT * FROM users WHERE LOWER(email) = LOWER(:email) LIMIT 1")
    suspend fun getByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE UPPER(referralCode) = UPPER(:code) LIMIT 1")
    suspend fun getByReferralCode(code: String): UserEntity?

    @Query("SELECT * FROM users WHERE role = 'ADMIN' LIMIT 1")
    suspend fun getAnyAdmin(): UserEntity?

    @Query("SELECT * FROM users ORDER BY createdAt DESC")
    fun observeAllUsers(): Flow<List<UserEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(user: UserEntity): Long

    @Update
    suspend fun update(user: UserEntity)
}

@Dao
interface ProjectDao {
    @Query("SELECT * FROM projects WHERE userId = :userId ORDER BY updatedAt DESC")
    fun observeUserProjects(userId: Long): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects WHERE userId = :userId AND isFavorite = 1 ORDER BY updatedAt DESC")
    fun observeUserFavorites(userId: Long): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects ORDER BY updatedAt DESC")
    fun observeAllProjects(): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): ProjectEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(project: ProjectEntity): Long

    @Update
    suspend fun update(project: ProjectEntity)

    @Query("DELETE FROM projects WHERE id = :id")
    suspend fun deleteById(id: Long)
}

@Dao
interface HistoryDao {
    @Query("SELECT * FROM editing_history WHERE userId = :userId ORDER BY createdAt DESC")
    fun observeUserHistory(userId: Long): Flow<List<HistoryEntity>>

    @Query("SELECT * FROM editing_history ORDER BY createdAt DESC")
    fun observeAllHistory(): Flow<List<HistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: HistoryEntity): Long
}

@Dao
interface CreditTransactionDao {
    @Query("SELECT * FROM credit_transactions WHERE userId = :userId ORDER BY createdAt DESC")
    fun observeUserTransactions(userId: Long): Flow<List<CreditTransactionEntity>>

    @Query("SELECT * FROM credit_transactions ORDER BY createdAt DESC")
    fun observeAllTransactions(): Flow<List<CreditTransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(tx: CreditTransactionEntity): Long
}

@Dao
interface ToolDao {
    @Query("SELECT * FROM tools ORDER BY number ASC")
    fun observeAllTools(): Flow<List<ToolConfigEntity>>

    @Query("SELECT * FROM tools WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): ToolConfigEntity?

    @Query("SELECT COUNT(*) FROM tools")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tools: List<ToolConfigEntity>)

    @Update
    suspend fun update(tool: ToolConfigEntity)
}

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories ORDER BY sortOrder ASC")
    fun observeAllCategories(): Flow<List<CategoryEntity>>

    @Query("SELECT COUNT(*) FROM categories")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(categories: List<CategoryEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(category: CategoryEntity)

    @Update
    suspend fun update(category: CategoryEntity)
}

@Dao
interface SubscriptionDao {
    @Query("SELECT * FROM subscriptions ORDER BY priceUsd ASC")
    fun observeAllPlans(): Flow<List<SubscriptionPlanEntity>>

    @Query("SELECT COUNT(*) FROM subscriptions")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(plans: List<SubscriptionPlanEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(plan: SubscriptionPlanEntity)
}

@Dao
interface PaymentDao {
    @Query("SELECT * FROM payments WHERE userId = :userId ORDER BY createdAt DESC")
    fun observeUserPayments(userId: Long): Flow<List<PaymentRecordEntity>>

    @Query("SELECT * FROM payments ORDER BY createdAt DESC")
    fun observeAllPayments(): Flow<List<PaymentRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(payment: PaymentRecordEntity): Long

    @Update
    suspend fun update(payment: PaymentRecordEntity)
}

@Dao
interface BannerDao {
    @Query("SELECT * FROM banners WHERE isPublished = 1 ORDER BY createdAt DESC")
    fun observePublishedBanners(): Flow<List<BannerEntity>>

    @Query("SELECT * FROM banners ORDER BY createdAt DESC")
    fun observeAllBanners(): Flow<List<BannerEntity>>

    @Query("SELECT COUNT(*) FROM banners")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(banner: BannerEntity): Long

    @Update
    suspend fun update(banner: BannerEntity)

    @Query("DELETE FROM banners WHERE id = :id")
    suspend fun deleteById(id: Long)
}

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications WHERE isPublished = 1 ORDER BY createdAt DESC")
    fun observePublishedNotifications(): Flow<List<NotificationEntity>>

    @Query("SELECT * FROM notifications ORDER BY createdAt DESC")
    fun observeAllNotifications(): Flow<List<NotificationEntity>>

    @Query("SELECT COUNT(*) FROM notifications")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(notification: NotificationEntity): Long

    @Update
    suspend fun update(notification: NotificationEntity)

    @Query("DELETE FROM notifications WHERE id = :id")
    suspend fun deleteById(id: Long)
}

@Dao
interface SupportTicketDao {
    @Query("SELECT * FROM support_tickets WHERE userId = :userId ORDER BY createdAt DESC")
    fun observeUserTickets(userId: Long): Flow<List<SupportTicketEntity>>

    @Query("SELECT * FROM support_tickets ORDER BY createdAt DESC")
    fun observeAllTickets(): Flow<List<SupportTicketEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(ticket: SupportTicketEntity): Long

    @Update
    suspend fun update(ticket: SupportTicketEntity)
}

@Dao
interface AuditLogDao {
    @Query("SELECT * FROM audit_logs ORDER BY createdAt DESC LIMIT 100")
    fun observeRecentLogs(): Flow<List<AuditLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: AuditLogEntity): Long
}
