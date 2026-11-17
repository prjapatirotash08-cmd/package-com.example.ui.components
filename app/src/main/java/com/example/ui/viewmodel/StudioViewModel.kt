package com.example.ui.viewmodel

import android.app.Application
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.annotation.DrawableRes
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.R
import com.example.data.local.*
import com.example.data.processing.ImageProcessingEngine
import com.example.data.repository.StudioRepository
import com.example.domain.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class WorkspaceState(
    val tool: StudioToolId = StudioToolId.ADV_FILTERS,
    val sourceBitmap: Bitmap? = null,
    val collageExtras: List<Bitmap> = emptyList(),
    val processedBitmap: Bitmap? = null,
    val generatedBitmaps: List<Bitmap> = emptyList(),
    val isProcessing: Boolean = false,
    val statusSummary: String? = null,
    val errorMessage: String? = null,
    val isSamplePreviewOutput: Boolean = false,
    val estimatedBytes: Long = 0L
)

@OptIn(ExperimentalCoroutinesApi::class)
class StudioViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = StudioRepository.create(application)
    private val imageEngine = ImageProcessingEngine(application)
    private val aiRepository: AiRepository = PlaceholderAiRepository()

    val aiFirebaseNotice: String = aiRepository.statusNotice
    val isLiveGeminiConnected: Boolean = aiRepository.isLiveGeminiConnected

    private val _hasAdminAccount = MutableStateFlow(true)
    val hasAdminAccount: StateFlow<Boolean> = _hasAdminAccount.asStateFlow()

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    private val _authLoading = MutableStateFlow(false)
    val authLoading: StateFlow<Boolean> = _authLoading.asStateFlow()

    private val _workspace = MutableStateFlow(WorkspaceState())
    val workspace: StateFlow<WorkspaceState> = _workspace.asStateFlow()

    val activeUserId: StateFlow<Long?> = repository.activeUserId.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), null
    )

    val currentUser: StateFlow<UserEntity?> = repository.activeUserId
        .flatMapLatest { uid ->
            if (uid == null) flowOf(null) else repository.observeUser(uid)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val tools: StateFlow<List<ToolConfigEntity>> = repository.observeAllTools()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categories: StateFlow<List<CategoryEntity>> = repository.observeAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val plans: StateFlow<List<SubscriptionPlanEntity>> = repository.observeAllPlans()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val publishedBanners: StateFlow<List<BannerEntity>> = repository.observePublishedBanners()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val publishedNotifications: StateFlow<List<NotificationEntity>> = repository.observePublishedNotifications()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userProjects: StateFlow<List<ProjectEntity>> = repository.activeUserId
        .flatMapLatest { uid -> if (uid == null) flowOf(emptyList()) else repository.observeUserProjects(uid) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userFavorites: StateFlow<List<ProjectEntity>> = repository.activeUserId
        .flatMapLatest { uid -> if (uid == null) flowOf(emptyList()) else repository.observeUserFavorites(uid) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userHistory: StateFlow<List<HistoryEntity>> = repository.activeUserId
        .flatMapLatest { uid -> if (uid == null) flowOf(emptyList()) else repository.observeUserHistory(uid) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userTransactions: StateFlow<List<CreditTransactionEntity>> = repository.activeUserId
        .flatMapLatest { uid -> if (uid == null) flowOf(emptyList()) else repository.observeUserTransactions(uid) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userPayments: StateFlow<List<PaymentRecordEntity>> = repository.activeUserId
        .flatMapLatest { uid -> if (uid == null) flowOf(emptyList()) else repository.observeUserPayments(uid) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userTickets: StateFlow<List<SupportTicketEntity>> = repository.activeUserId
        .flatMapLatest { uid -> if (uid == null) flowOf(emptyList()) else repository.observeUserTickets(uid) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Admin streams
    val allUsers: StateFlow<List<UserEntity>> = repository.observeAllUsers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allProjects: StateFlow<List<ProjectEntity>> = repository.observeAllProjects()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allHistory: StateFlow<List<HistoryEntity>> = repository.observeAllHistory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTransactions: StateFlow<List<CreditTransactionEntity>> = repository.observeAllTransactions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPayments: StateFlow<List<PaymentRecordEntity>> = repository.observeAllPayments()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allBanners: StateFlow<List<BannerEntity>> = repository.observeAllBanners()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allNotifications: StateFlow<List<NotificationEntity>> = repository.observeAllNotifications()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTickets: StateFlow<List<SupportTicketEntity>> = repository.observeAllTickets()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val auditLogs: StateFlow<List<AuditLogEntity>> = repository.observeRecentAuditLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val signupFreeCredits: StateFlow<Int> = repository.signupFreeCredits
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 25)

    val dailyBonusCredits: StateFlow<Int> = repository.dailyBonusCredits
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 5)

    val maintenanceMode: StateFlow<Boolean> = repository.maintenanceMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    init {
        viewModelScope.launch {
            repository.initializeDatabase()
            _hasAdminAccount.value = repository.hasAnyAdmin()
        }
    }

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }

    fun showMessage(msg: String) {
        _snackbarMessage.value = msg
    }

    fun clearAuthError() {
        _authError.value = null
    }

    fun register(
        name: String,
        email: String,
        password: String,
        referralCode: String = "",
        asAdmin: Boolean = false,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _authLoading.value = true
            _authError.value = null
            repository.registerUser(name, email, password, referralCode, asAdmin)
                .onSuccess { user ->
                    _hasAdminAccount.value = repository.hasAnyAdmin()
                    _snackbarMessage.value = "Welcome ${user.name}! ${user.credits} credits added."
                    onSuccess()
                }
                .onFailure { err ->
                    _authError.value = err.message ?: "Registration failed."
                }
            _authLoading.value = false
        }
    }

    fun login(email: String, password: String, requireAdmin: Boolean = false, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _authLoading.value = true
            _authError.value = null
            repository.loginUser(email, password, requireAdmin)
                .onSuccess { user ->
                    _snackbarMessage.value = "Signed in as ${user.name}"
                    onSuccess()
                }
                .onFailure { err ->
                    _authError.value = err.message ?: "Sign in failed."
                }
            _authLoading.value = false
        }
    }

    fun resetPassword(email: String, newPassword: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _authLoading.value = true
            _authError.value = null
            repository.resetPassword(email, newPassword)
                .onSuccess {
                    _snackbarMessage.value = "Password updated. You can now sign in."
                    onSuccess()
                }
                .onFailure { err ->
                    _authError.value = err.message ?: "Password reset failed."
                }
            _authLoading.value = false
        }
    }

    fun logout(onDone: () -> Unit) {
        viewModelScope.launch {
            repository.logout()
            _snackbarMessage.value = "Signed out securely."
            onDone()
        }
    }

    // --- Workspace & Tool Preparation ---

    fun prepareToolWorkspace(tool: StudioToolId, existingProjectPath: String? = null) {
        viewModelScope.launch {
            val loaded = if (!existingProjectPath.isNullOrBlank()) {
                imageEngine.loadBitmapFromFile(existingProjectPath)
            } else null
            val fallbackSample = loaded ?: _workspace.value.sourceBitmap
                ?: imageEngine.loadSampleDrawable(R.drawable.img_showcase_portrait)
            _workspace.value = WorkspaceState(
                tool = tool,
                sourceBitmap = fallbackSample,
                processedBitmap = null,
                generatedBitmaps = emptyList(),
                isProcessing = false,
                statusSummary = null,
                errorMessage = null,
                isSamplePreviewOutput = false,
                estimatedBytes = 0L
            )
        }
    }

    fun loadSourceFromUri(uri: Uri) {
        viewModelScope.launch {
            val bmp = imageEngine.loadBitmapFromUri(uri)
            if (bmp == null) {
                _snackbarMessage.value = "Unable to decode selected image. Please choose PNG, JPG, or WEBP."
                return@launch
            }
            _workspace.update {
                it.copy(
                    sourceBitmap = bmp,
                    processedBitmap = null,
                    statusSummary = "Loaded photo (${bmp.width}×${bmp.height}px)",
                    errorMessage = null
                )
            }
        }
    }

    fun loadCollageExtrasFromUris(uris: List<Uri>) {
        viewModelScope.launch {
            val bitmaps = uris.mapNotNull { imageEngine.loadBitmapFromUri(it, 900) }
            if (bitmaps.isNotEmpty()) {
                _workspace.update {
                    it.copy(
                        sourceBitmap = bitmaps.first(),
                        collageExtras = bitmaps.drop(1),
                        processedBitmap = null,
                        statusSummary = "Loaded ${bitmaps.size} photo(s) for collage",
                        errorMessage = null
                    )
                }
            }
        }
    }

    fun loadBundledSample(@DrawableRes resId: Int) {
        viewModelScope.launch {
            val bmp = imageEngine.loadSampleDrawable(resId)
            _workspace.update {
                it.copy(
                    sourceBitmap = bmp,
                    processedBitmap = null,
                    statusSummary = "Loaded studio sample (${bmp.width}×${bmp.height}px)",
                    errorMessage = null
                )
            }
        }
    }

    // --- AI Photo Editing (Tools 1–10) routed through AiRepository ---

    fun runAiPhotoEdit(tool: StudioToolId, intensity: Float, promptNote: String) {
        val user = currentUser.value
        if (user == null) {
            _snackbarMessage.value = "Please sign in first."
            return
        }
        val source = _workspace.value.sourceBitmap
        if (source == null) {
            _workspace.update { it.copy(errorMessage = "Select a photo first.") }
            return
        }
        viewModelScope.launch {
            _workspace.update { it.copy(isProcessing = true, errorMessage = null) }
            val reserve = repository.reserveCreditsForTool(user.id, tool.id)
            if (reserve.isFailure) {
                _workspace.update {
                    it.copy(isProcessing = false, errorMessage = reserve.exceptionOrNull()?.message ?: "Credit check failed")
                }
                return@launch
            }
            val reservedCredits = reserve.getOrDefault(0)
            try {
                val result = aiRepository.processAiPhotoEdit(
                    AiEditRequest(tool = tool, sourceBitmap = source, intensity = intensity, promptNote = promptNote)
                )
                // Because Firebase AI Logic is not enabled, refund reserved credits if result is a sample preview!
                if (result.isSamplePreview && reservedCredits > 0) {
                    repository.refundCredits(user.id, reservedCredits, "Sample preview refund (${tool.title} requires Firebase)")
                }
                repository.recordHistory(
                    userId = user.id,
                    toolId = tool.id,
                    toolName = tool.title,
                    categoryId = tool.category.id,
                    inputSummary = promptNote.ifBlank { "Intensity ${(intensity * 100).toInt()}%" },
                    outputPath = null,
                    creditsUsed = if (result.isSamplePreview) 0 else reservedCredits,
                    status = if (result.isSamplePreview) "SAMPLE_PREVIEW" else "SUCCESS"
                )
                _workspace.update {
                    it.copy(
                        processedBitmap = result.previewBitmap,
                        isProcessing = false,
                        statusSummary = result.summary,
                        isSamplePreviewOutput = result.isSamplePreview
                    )
                }
            } catch (e: Exception) {
                repository.refundCredits(user.id, reservedCredits, "Operation error refund: ${tool.title}")
                _workspace.update { it.copy(isProcessing = false, errorMessage = e.message ?: "Processing error") }
            }
        }
    }

    // --- AI Image Generator (Tools 11–20) routed through AiRepository ---

    fun runAiGenerator(
        tool: StudioToolId,
        prompt: String,
        negativePrompt: String,
        style: String,
        aspectRatio: String,
        quality: String,
        outputCount: Int
    ) {
        val user = currentUser.value
        if (user == null) {
            _snackbarMessage.value = "Please sign in first."
            return
        }
        if (prompt.trim().length < 3) {
            _workspace.update { it.copy(errorMessage = "Please enter a prompt (at least 3 characters).") }
            return
        }
        viewModelScope.launch {
            _workspace.update { it.copy(isProcessing = true, errorMessage = null) }
            val reserve = repository.reserveCreditsForTool(user.id, tool.id, multiplier = outputCount)
            if (reserve.isFailure) {
                _workspace.update {
                    it.copy(isProcessing = false, errorMessage = reserve.exceptionOrNull()?.message ?: "Credit check failed")
                }
                return@launch
            }
            val reservedCredits = reserve.getOrDefault(0)
            try {
                val result = aiRepository.generateAiImages(
                    AiGenRequest(
                        tool = tool,
                        prompt = prompt.trim(),
                        negativePrompt = negativePrompt.trim(),
                        style = style,
                        aspectRatio = aspectRatio,
                        quality = quality,
                        outputCount = outputCount
                    )
                )
                if (result.isSamplePreview && reservedCredits > 0) {
                    repository.refundCredits(user.id, reservedCredits, "Sample preview refund (${tool.title} requires Firebase)")
                }
                repository.recordHistory(
                    userId = user.id,
                    toolId = tool.id,
                    toolName = tool.title,
                    categoryId = tool.category.id,
                    inputSummary = "${prompt.trim().take(36)} • $style • $aspectRatio",
                    outputPath = null,
                    creditsUsed = if (result.isSamplePreview) 0 else reservedCredits,
                    status = if (result.isSamplePreview) "SAMPLE_PREVIEW" else "SUCCESS"
                )
                _workspace.update {
                    it.copy(
                        generatedBitmaps = result.bitmaps,
                        processedBitmap = result.bitmaps.firstOrNull(),
                        isProcessing = false,
                        statusSummary = result.summary,
                        isSamplePreviewOutput = result.isSamplePreview
                    )
                }
            } catch (e: Exception) {
                repository.refundCredits(user.id, reservedCredits, "Generation error refund: ${tool.title}")
                _workspace.update { it.copy(isProcessing = false, errorMessage = e.message ?: "Generation failed") }
            }
        }
    }

    // --- Real Local Creative Studio (21–30) & Advanced Image Tools (31–40) ---

    fun runCreativeTemplate(
        tool: StudioToolId,
        headline: String,
        subtitle: String,
        accentColorInt: Int
    ) {
        val user = currentUser.value
        val source = _workspace.value.sourceBitmap ?: return
        viewModelScope.launch {
            _workspace.update { it.copy(isProcessing = true, errorMessage = null) }
            val result = imageEngine.renderCreativeStudioTemplate(
                tool = tool,
                primarySource = source,
                collageExtras = _workspace.value.collageExtras,
                headline = headline,
                subtitle = subtitle,
                accentColor = accentColorInt
            )
            if (user != null) {
                repository.recordHistory(
                    userId = user.id,
                    toolId = tool.id,
                    toolName = tool.title,
                    categoryId = tool.category.id,
                    inputSummary = headline.ifBlank { tool.title },
                    outputPath = null,
                    creditsUsed = 0,
                    status = "SUCCESS"
                )
            }
            _workspace.update {
                it.copy(
                    processedBitmap = result.bitmap,
                    estimatedBytes = result.estimatedBytes,
                    statusSummary = result.summary,
                    isProcessing = false,
                    isSamplePreviewOutput = false
                )
            }
        }
    }

    fun runAdvancedTool(
        tool: StudioToolId,
        cropRatio: String,
        resizeWidth: Int,
        resizeHeight: Int,
        compressQuality: Int,
        filterPreset: String,
        effectName: String,
        effectIntensity: Float,
        overlayText: String,
        overlayTextColor: Int,
        overlayTextScale: Float,
        overlayPosition: String,
        stickerType: String,
        borderWidthPx: Int,
        borderColor: Int,
        exportFormat: ExportFormat,
        watermarkText: String,
        watermarkTiled: Boolean
    ) {
        val user = currentUser.value
        val source = _workspace.value.sourceBitmap ?: return
        viewModelScope.launch {
            _workspace.update { it.copy(isProcessing = true, errorMessage = null) }
            val result = imageEngine.processAdvancedTool(
                tool = tool,
                source = source,
                cropRatio = cropRatio,
                resizeWidth = resizeWidth,
                resizeHeight = resizeHeight,
                compressQuality = compressQuality,
                filterPreset = filterPreset,
                effectName = effectName,
                effectIntensity = effectIntensity,
                overlayText = overlayText,
                overlayTextColor = overlayTextColor,
                overlayTextSizeScale = overlayTextScale,
                overlayPosition = overlayPosition,
                stickerType = stickerType,
                borderWidthPx = borderWidthPx,
                borderColor = borderColor,
                exportFormat = exportFormat,
                watermarkText = watermarkText,
                watermarkTiled = watermarkTiled
            )
            if (user != null) {
                repository.recordHistory(
                    userId = user.id,
                    toolId = tool.id,
                    toolName = tool.title,
                    categoryId = tool.category.id,
                    inputSummary = result.summary,
                    outputPath = null,
                    creditsUsed = 0,
                    status = "SUCCESS"
                )
            }
            _workspace.update {
                it.copy(
                    processedBitmap = result.bitmap,
                    estimatedBytes = result.estimatedBytes,
                    statusSummary = result.summary,
                    isProcessing = false,
                    isSamplePreviewOutput = false
                )
            }
        }
    }

    fun saveProcessedToProjects(
        title: String,
        format: ExportFormat = ExportFormat.PNG,
        quality: Int = 92,
        bitmapOverride: Bitmap? = null
    ) {
        val user = currentUser.value ?: run {
            _snackbarMessage.value = "Sign in to save projects."
            return
        }
        val bmp = bitmapOverride ?: _workspace.value.processedBitmap ?: _workspace.value.sourceBitmap ?: return
        val tool = _workspace.value.tool
        viewModelScope.launch {
            val saved = imageEngine.saveBitmapToProjects(bmp, tool.id, format, quality)
            repository.saveProject(
                userId = user.id,
                title = title.ifBlank { "${tool.title} Edit" },
                toolId = tool.id,
                toolName = tool.title,
                categoryId = tool.category.id,
                imagePath = saved.filePath,
                format = format.label,
                fileSizeBytes = saved.fileSizeBytes,
                widthPx = saved.width,
                heightPx = saved.height
            )
            _snackbarMessage.value = "Saved '${title.ifBlank { tool.title }}' to My Projects (${saved.fileSizeBytes / 1024}KB)"
        }
    }

    fun exportProcessedToGallery(
        format: ExportFormat = ExportFormat.PNG,
        quality: Int = 95,
        bitmapOverride: Bitmap? = null
    ) {
        val user = currentUser.value
        val bmp = bitmapOverride ?: _workspace.value.processedBitmap ?: _workspace.value.sourceBitmap ?: return
        val tool = _workspace.value.tool
        viewModelScope.launch {
            val galleryRes = imageEngine.exportToDeviceGallery(bmp, tool.id, format, quality)
            if (user != null) {
                val saved = imageEngine.saveBitmapToProjects(bmp, "${tool.id}_export", format, quality)
                repository.saveProject(
                    userId = user.id,
                    title = "${tool.title} Export",
                    toolId = tool.id,
                    toolName = tool.title,
                    categoryId = tool.category.id,
                    imagePath = saved.filePath,
                    format = format.label,
                    fileSizeBytes = saved.fileSizeBytes,
                    widthPx = saved.width,
                    heightPx = saved.height,
                    exportedToGallery = true
                )
            }
            _snackbarMessage.value = galleryRes.getOrElse { it.message ?: "Export failed" }
        }
    }

    fun exportProjectFileToGallery(project: ProjectEntity) {
        viewModelScope.launch {
            val bmp = imageEngine.loadBitmapFromFile(project.imagePath)
            if (bmp == null) {
                _snackbarMessage.value = "Project file missing on disk."
                return@launch
            }
            val fmt = ExportFormat.entries.firstOrNull { it.label.equals(project.format, true) } ?: ExportFormat.PNG
            val res = imageEngine.exportToDeviceGallery(bmp, project.title, fmt, 95)
            repository.markExported(project.id)
            _snackbarMessage.value = res.getOrElse { it.message ?: "Export failed" }
        }
    }

    fun buildShareIntent(bitmap: Bitmap, format: ExportFormat = ExportFormat.PNG, onReady: (Intent) -> Unit) {
        viewModelScope.launch {
            onReady(imageEngine.createShareIntent(bitmap, format))
        }
    }

    fun shareProject(project: ProjectEntity, onReady: (Intent) -> Unit) {
        viewModelScope.launch {
            val bmp = imageEngine.loadBitmapFromFile(project.imagePath) ?: return@launch
            val fmt = ExportFormat.entries.firstOrNull { it.label.equals(project.format, true) } ?: ExportFormat.PNG
            onReady(imageEngine.createShareIntent(bmp, fmt))
        }
    }

    // --- User Panel Actions ---

    fun renameProject(projectId: Long, newTitle: String) {
        viewModelScope.launch {
            repository.renameProject(projectId, newTitle)
            _snackbarMessage.value = "Project renamed"
        }
    }

    fun toggleFavorite(projectId: Long) {
        viewModelScope.launch {
            repository.toggleFavorite(projectId)
        }
    }

    fun deleteProject(projectId: Long) {
        viewModelScope.launch {
            repository.deleteProject(projectId)
            _snackbarMessage.value = "Project deleted"
        }
    }

    fun updateProfileName(newName: String) {
        val uid = currentUser.value?.id ?: return
        viewModelScope.launch {
            repository.updateProfileName(uid, newName)
                .onSuccess { _snackbarMessage.value = "Profile updated" }
                .onFailure { _snackbarMessage.value = it.message ?: "Update failed" }
        }
    }

    fun claimDailyBonus() {
        val uid = currentUser.value?.id ?: return
        viewModelScope.launch {
            repository.claimDailyFreeCredits(uid)
                .onSuccess { bonus -> _snackbarMessage.value = "+$bonus daily credits claimed!" }
                .onFailure { _snackbarMessage.value = it.message ?: "Unable to claim bonus" }
        }
    }

    fun redeemReferralCode(code: String) {
        val uid = currentUser.value?.id ?: return
        viewModelScope.launch {
            repository.applyReferralCode(uid, code)
                .onSuccess { bonus -> _snackbarMessage.value = "+$bonus referral credits awarded!" }
                .onFailure { _snackbarMessage.value = it.message ?: "Referral failed" }
        }
    }

    fun subscribeToPlan(plan: SubscriptionPlanEntity) {
        val uid = currentUser.value?.id ?: return
        viewModelScope.launch {
            repository.createSubscriptionOrder(uid, plan)
                .onSuccess { record ->
                    _snackbarMessage.value = "Order ${record.transactionRef}: ${record.status}"
                }
                .onFailure { _snackbarMessage.value = it.message ?: "Order creation failed" }
        }
    }

    fun submitTicket(subject: String, message: String, onDone: () -> Unit) {
        val uid = currentUser.value?.id ?: return
        viewModelScope.launch {
            repository.submitSupportTicket(uid, subject, message)
                .onSuccess {
                    _snackbarMessage.value = "Support ticket submitted."
                    onDone()
                }
                .onFailure { _snackbarMessage.value = it.message ?: "Ticket submission failed" }
        }
    }

    // --- Admin Actions ---

    fun adminToggleUserStatus(target: UserEntity) {
        val adminEmail = currentUser.value?.email ?: return
        viewModelScope.launch {
            repository.adminToggleUserStatus(adminEmail, target)
            _snackbarMessage.value = "Updated status for ${target.email}"
        }
    }

    fun adminAdjustCredits(target: UserEntity, delta: Int, reason: String) {
        val adminEmail = currentUser.value?.email ?: return
        viewModelScope.launch {
            repository.adminAdjustCredits(adminEmail, target, delta, reason)
                .onSuccess { _snackbarMessage.value = "Adjusted credits for ${target.name}" }
                .onFailure { _snackbarMessage.value = it.message ?: "Adjustment failed" }
        }
    }

    fun adminUpdateTool(tool: ToolConfigEntity) {
        val adminEmail = currentUser.value?.email ?: return
        viewModelScope.launch {
            repository.adminUpdateTool(adminEmail, tool)
            _snackbarMessage.value = "Updated ${tool.title}"
        }
    }

    fun adminSaveCategory(category: CategoryEntity) {
        val adminEmail = currentUser.value?.email ?: return
        viewModelScope.launch {
            repository.adminSaveCategory(adminEmail, category)
            _snackbarMessage.value = "Saved category ${category.title}"
        }
    }

    fun adminSavePlan(plan: SubscriptionPlanEntity) {
        val adminEmail = currentUser.value?.email ?: return
        viewModelScope.launch {
            repository.adminSavePlan(adminEmail, plan)
            _snackbarMessage.value = "Saved plan ${plan.name}"
        }
    }

    fun adminSaveBanner(banner: BannerEntity) {
        val adminEmail = currentUser.value?.email ?: return
        viewModelScope.launch {
            repository.adminSaveBanner(adminEmail, banner)
            _snackbarMessage.value = "Banner saved"
        }
    }

    fun adminDeleteBanner(id: Long) {
        val adminEmail = currentUser.value?.email ?: return
        viewModelScope.launch {
            repository.adminDeleteBanner(adminEmail, id)
            _snackbarMessage.value = "Banner deleted"
        }
    }

    fun adminSaveNotification(notification: NotificationEntity) {
        val adminEmail = currentUser.value?.email ?: return
        viewModelScope.launch {
            repository.adminSaveNotification(adminEmail, notification)
            _snackbarMessage.value = "Notification published"
        }
    }

    fun adminDeleteNotification(id: Long) {
        val adminEmail = currentUser.value?.email ?: return
        viewModelScope.launch {
            repository.adminDeleteNotification(adminEmail, id)
            _snackbarMessage.value = "Notification deleted"
        }
    }

    fun adminReplyTicket(ticket: SupportTicketEntity, reply: String) {
        val adminEmail = currentUser.value?.email ?: return
        viewModelScope.launch {
            repository.adminReplyTicket(adminEmail, ticket, reply)
            _snackbarMessage.value = "Ticket #${ticket.id} resolved"
        }
    }

    fun adminUpdateSettings(signupFree: Int, dailyBonus: Int, maintenance: Boolean) {
        val adminEmail = currentUser.value?.email ?: return
        viewModelScope.launch {
            repository.adminUpdateSettings(adminEmail, signupFree, dailyBonus, maintenance)
            _snackbarMessage.value = "Application settings updated"
        }
    }
}
