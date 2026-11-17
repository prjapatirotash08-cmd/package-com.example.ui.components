package com.example.ui.navigation

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.domain.ExportFormat
import com.example.domain.StudioToolId
import com.example.domain.ToolCategory
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.StudioViewModel

object StudioRoutes {
    const val LANDING = "landing"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val FORGOT_PASSWORD = "forgot_password"
    const val ADMIN_LOGIN = "admin_login"

    const val DASHBOARD = "dashboard"
    const val AI_EDIT_HUB = "ai_edit_hub"
    const val AI_GEN_HUB = "ai_gen_hub"
    const val CREATIVE_HUB = "creative_hub"
    const val ADVANCED_HUB = "advanced_hub"
    const val USER_VAULT = "user_vault"
    const val USER_VAULT_TAB = "user_vault/{tab}"
    const val ADMIN_PANEL = "admin_panel"

    const val WORKSPACE_AI_EDIT = "workspace_ai_edit/{toolId}"
    const val WORKSPACE_AI_GEN = "workspace_ai_gen/{toolId}"
    const val WORKSPACE_CANVAS = "workspace_canvas/{toolId}"

    fun userVault(tab: UserHubTab) = "user_vault/${tab.name}"
    fun aiEdit(tool: StudioToolId) = "workspace_ai_edit/${tool.id}"
    fun aiGen(tool: StudioToolId) = "workspace_ai_gen/${tool.id}"
    fun canvas(tool: StudioToolId) = "workspace_canvas/${tool.id}"
}

private enum class MainBottomTab(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    HOME(StudioRoutes.DASHBOARD, "Home", Icons.Filled.Home),
    AI_EDIT(StudioRoutes.AI_EDIT_HUB, "AI Edit", Icons.Filled.AutoFixHigh),
    GENERATE(StudioRoutes.AI_GEN_HUB, "Generate", Icons.Filled.AutoAwesome),
    CREATIVE(StudioRoutes.CREATIVE_HUB, "Creative", Icons.Filled.Dashboard),
    TOOLS(StudioRoutes.ADVANCED_HUB, "Tools", Icons.Filled.Tune),
    VAULT(StudioRoutes.USER_VAULT, "Vault", Icons.Filled.Folder)
}

@Composable
fun StudioAppRoot(viewModel: StudioViewModel) {
    val context = LocalContext.current
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }

    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val hasAdmin by viewModel.hasAdminAccount.collectAsStateWithLifecycle()
    val snackbarMsg by viewModel.snackbarMessage.collectAsStateWithLifecycle()
    val authError by viewModel.authError.collectAsStateWithLifecycle()
    val authLoading by viewModel.authLoading.collectAsStateWithLifecycle()

    val tools by viewModel.tools.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val plans by viewModel.plans.collectAsStateWithLifecycle()
    val banners by viewModel.publishedBanners.collectAsStateWithLifecycle()
    val notifications by viewModel.publishedNotifications.collectAsStateWithLifecycle()
    val projects by viewModel.userProjects.collectAsStateWithLifecycle()
    val favorites by viewModel.userFavorites.collectAsStateWithLifecycle()
    val history by viewModel.userHistory.collectAsStateWithLifecycle()
    val transactions by viewModel.userTransactions.collectAsStateWithLifecycle()
    val payments by viewModel.userPayments.collectAsStateWithLifecycle()
    val tickets by viewModel.userTickets.collectAsStateWithLifecycle()
    val workspace by viewModel.workspace.collectAsStateWithLifecycle()

    // Admin streams
    val allUsers by viewModel.allUsers.collectAsStateWithLifecycle()
    val allProjects by viewModel.allProjects.collectAsStateWithLifecycle()
    val allHistory by viewModel.allHistory.collectAsStateWithLifecycle()
    val allTransactions by viewModel.allTransactions.collectAsStateWithLifecycle()
    val allPayments by viewModel.allPayments.collectAsStateWithLifecycle()
    val allBanners by viewModel.allBanners.collectAsStateWithLifecycle()
    val allNotifications by viewModel.allNotifications.collectAsStateWithLifecycle()
    val allTickets by viewModel.allTickets.collectAsStateWithLifecycle()
    val auditLogs by viewModel.auditLogs.collectAsStateWithLifecycle()
    val signupFree by viewModel.signupFreeCredits.collectAsStateWithLifecycle()
    val dailyBonus by viewModel.dailyBonusCredits.collectAsStateWithLifecycle()
    val maintenance by viewModel.maintenanceMode.collectAsStateWithLifecycle()

    LaunchedEffect(snackbarMsg) {
        snackbarMsg?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSnackbar()
        }
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showMainChrome = currentUser != null && currentRoute in setOf(
        StudioRoutes.DASHBOARD,
        StudioRoutes.AI_EDIT_HUB,
        StudioRoutes.AI_GEN_HUB,
        StudioRoutes.CREATIVE_HUB,
        StudioRoutes.ADVANCED_HUB,
        StudioRoutes.USER_VAULT
    )

    val launchTool: (StudioToolId, String?) -> Unit = { tool, projectPath ->
        viewModel.prepareToolWorkspace(tool, projectPath)
        when (tool.category) {
            ToolCategory.AI_EDITING -> navController.navigate(StudioRoutes.aiEdit(tool))
            ToolCategory.AI_GENERATOR -> navController.navigate(StudioRoutes.aiGen(tool))
            ToolCategory.CREATIVE_STUDIO, ToolCategory.ADVANCED_EDITING -> navController.navigate(StudioRoutes.canvas(tool))
        }
    }

    val startDestination = if (currentUser != null) StudioRoutes.DASHBOARD else StudioRoutes.LANDING

    BoxWithConstraints(modifier = Modifier.fillMaxSize().background(ObsidianBlack)) {
        val isWide = maxWidth >= 600.dp

        Scaffold(
            containerColor = ObsidianBlack,
            snackbarHost = {
                SnackbarHost(snackbarHostState) { data ->
                    Snackbar(
                        snackbarData = data,
                        containerColor = CardObsidian,
                        contentColor = SecondaryGold,
                        actionColor = PrimaryGold
                    )
                }
            },
            bottomBar = {
                if (showMainChrome && !isWide) {
                    NavigationBar(
                        containerColor = CharcoalSurface,
                        contentColor = PrimaryGold,
                        windowInsets = WindowInsets.navigationBars
                    ) {
                        MainBottomTab.entries.forEach { tab ->
                            val selected = currentRoute == tab.route
                            NavigationBarItem(
                                selected = selected,
                                onClick = {
                                    navController.navigate(tab.route) {
                                        popUpTo(StudioRoutes.DASHBOARD) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = { Icon(tab.icon, contentDescription = tab.label) },
                                label = { Text(tab.label, maxLines = 1) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = ObsidianBlack,
                                    selectedTextColor = SecondaryGold,
                                    indicatorColor = PrimaryGold,
                                    unselectedIconColor = TextSecondary,
                                    unselectedTextColor = TextSecondary
                                )
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (showMainChrome && isWide) {
                    NavigationRail(
                        containerColor = CharcoalSurface,
                        contentColor = PrimaryGold
                    ) {
                        Spacer(Modifier.height(12.dp))
                        MainBottomTab.entries.forEach { tab ->
                            val selected = currentRoute == tab.route
                            NavigationRailItem(
                                selected = selected,
                                onClick = {
                                    navController.navigate(tab.route) {
                                        popUpTo(StudioRoutes.DASHBOARD) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = { Icon(tab.icon, contentDescription = tab.label) },
                                label = { Text(tab.label) },
                                colors = NavigationRailItemDefaults.colors(
                                    selectedIconColor = ObsidianBlack,
                                    selectedTextColor = SecondaryGold,
                                    indicatorColor = PrimaryGold,
                                    unselectedIconColor = TextSecondary,
                                    unselectedTextColor = TextSecondary
                                )
                            )
                        }
                    }
                }

                NavHost(
                    navController = navController,
                    startDestination = startDestination,
                    modifier = Modifier.weight(1f)
                ) {
                    composable(StudioRoutes.LANDING) {
                        LandingScreen(
                            plans = plans,
                            aiNotice = viewModel.aiFirebaseNotice,
                            onStartCreating = {
                                if (currentUser != null) {
                                    navController.navigate(StudioRoutes.DASHBOARD) {
                                        popUpTo(StudioRoutes.LANDING) { inclusive = true }
                                    }
                                } else {
                                    viewModel.clearAuthError()
                                    navController.navigate(StudioRoutes.LOGIN)
                                }
                            },
                            onExploreTools = {
                                viewModel.clearAuthError()
                                navController.navigate(if (currentUser != null) StudioRoutes.AI_EDIT_HUB else StudioRoutes.REGISTER)
                            },
                            onAdminLogin = {
                                viewModel.clearAuthError()
                                navController.navigate(StudioRoutes.ADMIN_LOGIN)
                            }
                        )
                    }

                    composable(StudioRoutes.LOGIN) {
                        LoginScreen(
                            isLoading = authLoading,
                            errorMessage = authError,
                            onLogin = { email, password ->
                                viewModel.login(email, password, requireAdmin = false) {
                                    navController.navigate(StudioRoutes.DASHBOARD) {
                                        popUpTo(StudioRoutes.LANDING) { inclusive = true }
                                    }
                                }
                            },
                            onNavigateRegister = {
                                viewModel.clearAuthError()
                                navController.navigate(StudioRoutes.REGISTER)
                            },
                            onNavigateForgotPassword = {
                                viewModel.clearAuthError()
                                navController.navigate(StudioRoutes.FORGOT_PASSWORD)
                            },
                            onNavigateAdminLogin = {
                                viewModel.clearAuthError()
                                navController.navigate(StudioRoutes.ADMIN_LOGIN)
                            },
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable(StudioRoutes.REGISTER) {
                        RegisterScreen(
                            starterCredits = signupFree,
                            isLoading = authLoading,
                            errorMessage = authError,
                            onRegister = { name, email, password, referral ->
                                viewModel.register(name, email, password, referral, asAdmin = false) {
                                    navController.navigate(StudioRoutes.DASHBOARD) {
                                        popUpTo(StudioRoutes.LANDING) { inclusive = true }
                                    }
                                }
                            },
                            onNavigateLogin = {
                                viewModel.clearAuthError()
                                navController.navigate(StudioRoutes.LOGIN)
                            },
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable(StudioRoutes.FORGOT_PASSWORD) {
                        ForgotPasswordScreen(
                            isLoading = authLoading,
                            errorMessage = authError,
                            onResetPassword = { email, newPw ->
                                viewModel.resetPassword(email, newPw) {
                                    navController.popBackStack()
                                }
                            },
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable(StudioRoutes.ADMIN_LOGIN) {
                        AdminLoginScreen(
                            hasAdminAccount = hasAdmin,
                            isLoading = authLoading,
                            errorMessage = authError,
                            onAdminLogin = { email, password ->
                                viewModel.login(email, password, requireAdmin = true) {
                                    navController.navigate(StudioRoutes.ADMIN_PANEL) {
                                        popUpTo(StudioRoutes.LANDING) { inclusive = false }
                                    }
                                }
                            },
                            onCreateInitialAdmin = { name, email, password ->
                                viewModel.register(name, email, password, asAdmin = true) {
                                    navController.navigate(StudioRoutes.ADMIN_PANEL) {
                                        popUpTo(StudioRoutes.LANDING) { inclusive = false }
                                    }
                                }
                            },
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable(StudioRoutes.DASHBOARD) {
                        val u = currentUser
                        if (u == null) {
                            LaunchedEffect(Unit) {
                                navController.navigate(StudioRoutes.LANDING) {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        } else {
                            DashboardScreen(
                                user = u,
                                projects = projects,
                                favorites = favorites,
                                history = history,
                                banners = banners,
                                notifications = notifications,
                                aiNotice = viewModel.aiFirebaseNotice,
                                maintenanceMode = maintenance,
                                onOpenCategory = { cat ->
                                    val route = when (cat) {
                                        ToolCategory.AI_EDITING -> StudioRoutes.AI_EDIT_HUB
                                        ToolCategory.AI_GENERATOR -> StudioRoutes.AI_GEN_HUB
                                        ToolCategory.CREATIVE_STUDIO -> StudioRoutes.CREATIVE_HUB
                                        ToolCategory.ADVANCED_EDITING -> StudioRoutes.ADVANCED_HUB
                                    }
                                    navController.navigate(route)
                                },
                                onOpenProjects = { navController.navigate(StudioRoutes.userVault(UserHubTab.PROJECTS)) },
                                onOpenFavorites = { navController.navigate(StudioRoutes.userVault(UserHubTab.FAVORITES)) },
                                onOpenHistory = { navController.navigate(StudioRoutes.userVault(UserHubTab.HISTORY)) },
                                onOpenSubscriptions = { navController.navigate(StudioRoutes.userVault(UserHubTab.CREDITS)) },
                                onOpenProfile = { navController.navigate(StudioRoutes.userVault(UserHubTab.PROFILE)) },
                                onOpenAdmin = { navController.navigate(StudioRoutes.ADMIN_PANEL) },
                                onOpenToolFromProject = { proj ->
                                    val tool = StudioToolId.byId(proj.toolId) ?: StudioToolId.ADV_FILTERS
                                    launchTool(tool, proj.imagePath)
                                }
                            )
                        }
                    }

                    composable(StudioRoutes.AI_EDIT_HUB) {
                        ToolCategoryHubScreen(
                            category = ToolCategory.AI_EDITING,
                            allTools = tools,
                            aiNotice = viewModel.aiFirebaseNotice,
                            onSelectTool = { launchTool(it, null) }
                        )
                    }

                    composable(StudioRoutes.AI_GEN_HUB) {
                        ToolCategoryHubScreen(
                            category = ToolCategory.AI_GENERATOR,
                            allTools = tools,
                            aiNotice = viewModel.aiFirebaseNotice,
                            onSelectTool = { launchTool(it, null) }
                        )
                    }

                    composable(StudioRoutes.CREATIVE_HUB) {
                        ToolCategoryHubScreen(
                            category = ToolCategory.CREATIVE_STUDIO,
                            allTools = tools,
                            aiNotice = viewModel.aiFirebaseNotice,
                            onSelectTool = { launchTool(it, null) }
                        )
                    }

                    composable(StudioRoutes.ADVANCED_HUB) {
                        ToolCategoryHubScreen(
                            category = ToolCategory.ADVANCED_EDITING,
                            allTools = tools,
                            aiNotice = viewModel.aiFirebaseNotice,
                            onSelectTool = { launchTool(it, null) }
                        )
                    }

                    composable(
                        route = StudioRoutes.WORKSPACE_AI_EDIT,
                        arguments = listOf(navArgument("toolId") { type = NavType.StringType })
                    ) { backStack ->
                        val toolId = backStack.arguments?.getString("toolId").orEmpty()
                        val tool = StudioToolId.byId(toolId) ?: StudioToolId.AI_ENHANCER
                        val cost = tools.firstOrNull { it.id == tool.id }?.creditCost ?: tool.defaultCredits
                        AiPhotoEditorScreen(
                            tool = tool,
                            workspace = workspace,
                            aiNotice = viewModel.aiFirebaseNotice,
                            creditCost = cost,
                            onPickImageUri = viewModel::loadSourceFromUri,
                            onLoadSample = viewModel::loadBundledSample,
                            onRunAiEdit = { intensity, note -> viewModel.runAiPhotoEdit(tool, intensity, note) },
                            onSaveProject = { title -> viewModel.saveProcessedToProjects(title) },
                            onExportGallery = { viewModel.exportProcessedToGallery() },
                            onShare = {
                                val bmp = workspace.processedBitmap ?: workspace.sourceBitmap ?: return@AiPhotoEditorScreen
                                viewModel.buildShareIntent(bmp) { intent ->
                                    context.startActivity(Intent.createChooser(intent, "Share Preview"))
                                }
                            },
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable(
                        route = StudioRoutes.WORKSPACE_AI_GEN,
                        arguments = listOf(navArgument("toolId") { type = NavType.StringType })
                    ) { backStack ->
                        val toolId = backStack.arguments?.getString("toolId").orEmpty()
                        val tool = StudioToolId.byId(toolId) ?: StudioToolId.GEN_TEXT_TO_IMAGE
                        val cost = tools.firstOrNull { it.id == tool.id }?.creditCost ?: tool.defaultCredits
                        AiGeneratorScreen(
                            tool = tool,
                            workspace = workspace,
                            aiNotice = viewModel.aiFirebaseNotice,
                            creditCost = cost,
                            onGenerate = { prompt, neg, style, aspect, quality, count ->
                                viewModel.runAiGenerator(tool, prompt, neg, style, aspect, quality, count)
                            },
                            onSaveProject = { title, idx ->
                                val bmp = workspace.generatedBitmaps.getOrNull(idx)
                                viewModel.saveProcessedToProjects(title, ExportFormat.PNG, 95, bmp)
                            },
                            onExportGallery = { idx ->
                                val bmp = workspace.generatedBitmaps.getOrNull(idx)
                                viewModel.exportProcessedToGallery(ExportFormat.PNG, 95, bmp)
                            },
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable(
                        route = StudioRoutes.WORKSPACE_CANVAS,
                        arguments = listOf(navArgument("toolId") { type = NavType.StringType })
                    ) { backStack ->
                        val toolId = backStack.arguments?.getString("toolId").orEmpty()
                        val tool = StudioToolId.byId(toolId) ?: StudioToolId.ADV_FILTERS
                        CreativeAndAdvancedEditorScreen(
                            tool = tool,
                            workspace = workspace,
                            onPickSingleUri = viewModel::loadSourceFromUri,
                            onPickCollageUris = viewModel::loadCollageExtrasFromUris,
                            onLoadSample = viewModel::loadBundledSample,
                            onApplyCreative = { headline, subtitle, accent ->
                                viewModel.runCreativeTemplate(tool, headline, subtitle, accent)
                            },
                            onApplyAdvanced = { crop, rw, rh, cq, filter, eff, effInt, txt, txtCol, txtScale, pos, sticker, bw, bc, fmt, wm, wmTile ->
                                viewModel.runAdvancedTool(
                                    tool, crop, rw, rh, cq, filter, eff, effInt, txt, txtCol, txtScale, pos, sticker, bw, bc, fmt, wm, wmTile
                                )
                            },
                            onSaveProject = { title, fmt, q -> viewModel.saveProcessedToProjects(title, fmt, q) },
                            onExportGallery = { fmt, q -> viewModel.exportProcessedToGallery(fmt, q) },
                            onShare = { fmt ->
                                val bmp = workspace.processedBitmap ?: workspace.sourceBitmap ?: return@CreativeAndAdvancedEditorScreen
                                viewModel.buildShareIntent(bmp, fmt) { intent ->
                                    context.startActivity(Intent.createChooser(intent, "Share Studio Image"))
                                }
                            },
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable(StudioRoutes.USER_VAULT) {
                        val u = currentUser ?: return@composable
                        UserPanelHubScreen(
                            initialTab = UserHubTab.PROJECTS,
                            user = u,
                            projects = projects,
                            favorites = favorites,
                            history = history,
                            transactions = transactions,
                            plans = plans,
                            payments = payments,
                            tickets = tickets,
                            dailyBonusAmount = dailyBonus,
                            onOpenProject = { proj ->
                                val tool = StudioToolId.byId(proj.toolId) ?: StudioToolId.ADV_FILTERS
                                launchTool(tool, proj.imagePath)
                            },
                            onRenameProject = viewModel::renameProject,
                            onToggleFavorite = viewModel::toggleFavorite,
                            onDeleteProject = viewModel::deleteProject,
                            onExportProject = viewModel::exportProjectFileToGallery,
                            onShareProject = { proj ->
                                viewModel.shareProject(proj) { intent ->
                                    context.startActivity(Intent.createChooser(intent, "Share Project"))
                                }
                            },
                            onClaimDailyBonus = viewModel::claimDailyBonus,
                            onSubscribePlan = viewModel::subscribeToPlan,
                            onRedeemReferral = viewModel::redeemReferralCode,
                            onSubmitTicket = viewModel::submitTicket,
                            onUpdateProfileName = viewModel::updateProfileName,
                            onOpenAdminPanel = { navController.navigate(StudioRoutes.ADMIN_PANEL) },
                            onLogout = {
                                viewModel.logout {
                                    navController.navigate(StudioRoutes.LANDING) {
                                        popUpTo(0) { inclusive = true }
                                    }
                                }
                            },
                            onNotify = viewModel::showMessage
                        )
                    }

                    composable(
                        route = StudioRoutes.USER_VAULT_TAB,
                        arguments = listOf(navArgument("tab") { type = NavType.StringType })
                    ) { backStack ->
                        val u = currentUser ?: return@composable
                        val tabName = backStack.arguments?.getString("tab").orEmpty()
                        val initial = UserHubTab.entries.firstOrNull { it.name == tabName } ?: UserHubTab.PROJECTS
                        UserPanelHubScreen(
                            initialTab = initial,
                            user = u,
                            projects = projects,
                            favorites = favorites,
                            history = history,
                            transactions = transactions,
                            plans = plans,
                            payments = payments,
                            tickets = tickets,
                            dailyBonusAmount = dailyBonus,
                            onOpenProject = { proj ->
                                val tool = StudioToolId.byId(proj.toolId) ?: StudioToolId.ADV_FILTERS
                                launchTool(tool, proj.imagePath)
                            },
                            onRenameProject = viewModel::renameProject,
                            onToggleFavorite = viewModel::toggleFavorite,
                            onDeleteProject = viewModel::deleteProject,
                            onExportProject = viewModel::exportProjectFileToGallery,
                            onShareProject = { proj ->
                                viewModel.shareProject(proj) { intent ->
                                    context.startActivity(Intent.createChooser(intent, "Share Project"))
                                }
                            },
                            onClaimDailyBonus = viewModel::claimDailyBonus,
                            onSubscribePlan = viewModel::subscribeToPlan,
                            onRedeemReferral = viewModel::redeemReferralCode,
                            onSubmitTicket = viewModel::submitTicket,
                            onUpdateProfileName = viewModel::updateProfileName,
                            onOpenAdminPanel = { navController.navigate(StudioRoutes.ADMIN_PANEL) },
                            onLogout = {
                                viewModel.logout {
                                    navController.navigate(StudioRoutes.LANDING) {
                                        popUpTo(0) { inclusive = true }
                                    }
                                }
                            },
                            onNotify = viewModel::showMessage,
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable(StudioRoutes.ADMIN_PANEL) {
                        val u = currentUser ?: return@composable
                        AdminPanelScreen(
                            currentUser = u,
                            allUsers = allUsers,
                            allProjects = allProjects,
                            allHistory = allHistory,
                            allTransactions = allTransactions,
                            allTools = tools,
                            allCategories = categories,
                            allPlans = plans,
                            allPayments = allPayments,
                            allBanners = allBanners,
                            allNotifications = allNotifications,
                            allTickets = allTickets,
                            auditLogs = auditLogs,
                            signupFreeCredits = signupFree,
                            dailyBonusCredits = dailyBonus,
                            maintenanceMode = maintenance,
                            onToggleUserStatus = viewModel::adminToggleUserStatus,
                            onAdjustUserCredits = viewModel::adminAdjustCredits,
                            onUpdateTool = viewModel::adminUpdateTool,
                            onSaveCategory = viewModel::adminSaveCategory,
                            onSavePlan = viewModel::adminSavePlan,
                            onSaveBanner = viewModel::adminSaveBanner,
                            onDeleteBanner = viewModel::adminDeleteBanner,
                            onSaveNotification = viewModel::adminSaveNotification,
                            onDeleteNotification = viewModel::adminDeleteNotification,
                            onReplyTicket = viewModel::adminReplyTicket,
                            onUpdateSettings = viewModel::adminUpdateSettings,
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}
