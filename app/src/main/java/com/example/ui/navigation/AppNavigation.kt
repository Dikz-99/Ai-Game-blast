package com.example.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.screens.addgame.AddGameScreen
import com.example.ui.screens.aimonitor.AiMonitorScreen
import com.example.ui.screens.dashboard.DashboardScreen
import com.example.ui.screens.floating.FloatingSettingsScreen
import com.example.ui.screens.gamemanager.GameManagerScreen
import com.example.ui.screens.history.SessionHistoryScreen
import com.example.ui.screens.profiledetail.GameProfileDetailScreen
import com.example.ui.screens.settings.AiSettingsScreen
import com.example.ui.screens.splash.SplashScreen
import com.example.ui.viewmodel.MainViewModel

object Routes {
    const val SPLASH = "splash"
    const val DASHBOARD = "dashboard"
    const val GAME_MANAGER = "game_manager"
    const val ADD_GAME = "add_game"
    const val AI_MONITOR = "ai_monitor"
    const val SESSION_HISTORY = "session_history"
    const val AI_SETTINGS = "ai_settings"
    const val FLOATING_SETTINGS = "floating_settings"
    const val PROFILE_DETAIL = "profile_detail/{profileId}"
    fun profileDetail(profileId: Long) = "profile_detail/$profileId"
}

@Composable
fun AppNavigation(
    viewModel: MainViewModel,
    onRequestMediaProjection: () -> Unit,
    onOpenOverlaySettings: () -> Unit,
    onOpenAccessibilitySettings: () -> Unit
) {
    val navController = rememberNavController()

    val activeProfile by viewModel.activeProfile.collectAsState()
    val allProfiles by viewModel.allProfiles.collectAsState()
    val allSessions by viewModel.allSessions.collectAsState()
    val latestSession by viewModel.latestSession.collectAsState()
    val userSettings by viewModel.userSettings.collectAsState()
    val permissionStatus by viewModel.permissionStatus.collectAsState()
    val selectedMemories by viewModel.selectedProfileMemories.collectAsState()
    val captureMetrics by viewModel.captureMetrics.collectAsState()
    val latestCapturedFrame by viewModel.latestCapturedFrame.collectAsState()
    val visionResult by viewModel.visionResult.collectAsState()
    val ocrLogs by viewModel.ocrLogs.collectAsState()
    val actionLogs by viewModel.actionLogs.collectAsState()
    val isExecutingMacro by viewModel.isExecutingMacro.collectAsState()
    val macroProgress by viewModel.macroProgress.collectAsState()

    // Phase 5 Decision Engine states
    val isAutoPilotActive by viewModel.isAutoPilotActive.collectAsState()
    val decisionState by viewModel.decisionState.collectAsState()
    val latestDecisionProposal by viewModel.latestDecisionProposal.collectAsState()
    val decisionLogs by viewModel.decisionLogs.collectAsState()
    val totalDecisions by viewModel.totalDecisions.collectAsState()
    val successfulDecisions by viewModel.successfulDecisions.collectAsState()

    // Phase 7 Hybrid Strategy states
    val strategicRecommendation by viewModel.strategicRecommendation.collectAsState()
    val isAnalyzingStrategy by viewModel.isAnalyzingStrategy.collectAsState()
    val strategySource by viewModel.strategySource.collectAsState()

    // Phase 8 Learning Engine states
    val totalTransitionsEvaluated by viewModel.totalTransitionsEvaluated.collectAsState()
    val weightAdjustmentsCount by viewModel.weightAdjustmentsCount.collectAsState()
    val latestLearningRecord by viewModel.latestLearningRecord.collectAsState()
    val learningFeed by viewModel.learningFeed.collectAsState()

    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH
    ) {
        composable(Routes.SPLASH) {
            SplashScreen(
                onSplashFinished = {
                    navController.navigate(Routes.DASHBOARD) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.DASHBOARD) {
            DashboardScreen(
                activeProfile = activeProfile,
                latestSession = latestSession,
                permissionStatus = permissionStatus,
                onStartAiClick = onRequestMediaProjection,
                onStopAiClick = { viewModel.stopAiAutomation() },
                onEmergencyStopClick = { viewModel.triggerEmergencyStop() },
                onNavigateToGameManager = { navController.navigate(Routes.GAME_MANAGER) },
                onNavigateToMonitor = { navController.navigate(Routes.AI_MONITOR) },
                onNavigateToHistory = { navController.navigate(Routes.SESSION_HISTORY) },
                onNavigateToSettings = { navController.navigate(Routes.AI_SETTINGS) },
                onNavigateToFloatingSettings = { navController.navigate(Routes.FLOATING_SETTINGS) },
                onGrantPermissionsClick = {
                    if (!permissionStatus.hasOverlayPermission) {
                        onOpenOverlaySettings()
                    } else if (!permissionStatus.hasAccessibilityPermission) {
                        onOpenAccessibilitySettings()
                    }
                }
            )
        }

        composable(Routes.GAME_MANAGER) {
            GameManagerScreen(
                profiles = allProfiles,
                onBackClick = { navController.popBackStack() },
                onAddGameClick = { navController.navigate(Routes.ADD_GAME) },
                onSelectActiveProfile = { id -> viewModel.setActiveProfile(id) },
                onProfileDetailClick = { id ->
                    viewModel.loadProfileMemories(id)
                    navController.navigate(Routes.profileDetail(id))
                },
                onDeleteProfileClick = { profile -> viewModel.deleteGameProfile(profile) }
            )
        }

        composable(Routes.ADD_GAME) {
            AddGameScreen(
                onBackClick = { navController.popBackStack() },
                onSaveProfile = { newProfile ->
                    viewModel.saveGameProfile(newProfile) {
                        navController.popBackStack()
                    }
                }
            )
        }

        composable(Routes.AI_MONITOR) {
            AiMonitorScreen(
                activeProfile = activeProfile,
                metrics = captureMetrics,
                capturedFrame = latestCapturedFrame,
                visionResult = visionResult,
                ocrLogs = ocrLogs,
                actionLogs = actionLogs,
                isExecutingMacro = isExecutingMacro,
                macroProgress = macroProgress,
                presetMacros = viewModel.presetMacros,
                onBackClick = { navController.popBackStack() },
                onGenerateTestFrame = { viewModel.triggerSyntheticFrameCapture() },
                onExecuteTap = { x, y -> viewModel.executeTap(x, y) },
                onExecuteSwipe = { sX, sY, eX, eY -> viewModel.executeSwipe(sX, sY, eX, eY) },
                onExecuteLongPress = { x, y -> viewModel.executeLongPress(x, y) },
                onExecuteMacro = { macro -> viewModel.executeMacro(macro) },
                isAutoPilotActive = isAutoPilotActive,
                decisionState = decisionState,
                latestProposal = latestDecisionProposal,
                decisionLogs = decisionLogs,
                totalDecisions = totalDecisions,
                successfulDecisions = successfulDecisions,
                onToggleAutoPilot = { viewModel.toggleAutoPilot() },
                onStepDecision = { viewModel.triggerSingleDecisionStep() },
                strategicRecommendation = strategicRecommendation,
                isAnalyzingStrategy = isAnalyzingStrategy,
                strategySource = strategySource,
                totalTransitionsEvaluated = totalTransitionsEvaluated,
                weightAdjustmentsCount = weightAdjustmentsCount,
                latestLearningRecord = latestLearningRecord,
                learningFeed = learningFeed,
                onRequestStrategicAdvice = { viewModel.requestStrategicAdvice() },
                onTriggerLearningEvaluation = { viewModel.evaluateLearningTransition() }
            )
        }

        composable(Routes.SESSION_HISTORY) {
            SessionHistoryScreen(
                sessions = allSessions,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Routes.AI_SETTINGS) {
            AiSettingsScreen(
                currentSettings = userSettings,
                onBackClick = { navController.popBackStack() },
                onSaveSettings = { updated ->
                    viewModel.saveUserSettings(updated) {
                        navController.popBackStack()
                    }
                }
            )
        }

        composable(Routes.FLOATING_SETTINGS) {
            FloatingSettingsScreen(
                permissionStatus = permissionStatus,
                onBackClick = { navController.popBackStack() },
                onGrantOverlayClick = onOpenOverlaySettings,
                onToggleFloatingService = { start ->
                    if (start) viewModel.startFloatingHud() else viewModel.stopFloatingHud()
                }
            )
        }

        composable(
            route = Routes.PROFILE_DETAIL,
            arguments = listOf(navArgument("profileId") { type = NavType.LongType })
        ) { backStackEntry ->
            val profileId = backStackEntry.arguments?.getLong("profileId") ?: 0L
            val profile = allProfiles.find { it.id == profileId }

            GameProfileDetailScreen(
                profile = profile,
                memories = selectedMemories,
                onBackClick = { navController.popBackStack() },
                onAddMemoryRuleClick = { }
            )
        }
    }
}
