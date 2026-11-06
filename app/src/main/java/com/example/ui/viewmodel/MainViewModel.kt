package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.GameBlastApplication
import com.example.automation.service.GameAutomationAccessibilityService
import com.example.capture.service.ScreenCaptureService
import com.example.core.model.GameState
import com.example.core.permission.PermissionManager
import com.example.core.permission.PermissionStatus
import com.example.core.safety.SafetyController
import com.example.data.local.entity.GameProfile
import com.example.data.local.entity.GameSession
import com.example.data.local.entity.StrategyMemory
import com.example.data.local.entity.UserSettings
import com.example.ui.floating.FloatingAssistantService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as GameBlastApplication).repository

    val allProfiles: StateFlow<List<GameProfile>> = repository.allProfiles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeProfile: StateFlow<GameProfile?> = repository.activeProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allSessions: StateFlow<List<GameSession>> = repository.allSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val latestSession: StateFlow<GameSession?> = repository.latestSession
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val userSettings: StateFlow<UserSettings?> = repository.userSettings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _permissionStatus = MutableStateFlow(
        PermissionManager.checkPermissions(application)
    )
    val permissionStatus: StateFlow<PermissionStatus> = _permissionStatus.asStateFlow()

    private val _selectedProfileMemories = MutableStateFlow<List<StrategyMemory>>(emptyList())
    val selectedProfileMemories: StateFlow<List<StrategyMemory>> = _selectedProfileMemories.asStateFlow()

    val captureMetrics = com.example.capture.pipeline.ScreenCapturePipeline.metrics
    val latestCapturedFrame = com.example.capture.pipeline.ScreenCapturePipeline.latestFrame
    val visionResult = com.example.vision.pipeline.VisionPipeline.latestResult
    val ocrLogs = com.example.vision.pipeline.VisionPipeline.recentOcrLogs
    val actionLogs = com.example.automation.pipeline.ActionPipeline.recentActionLogs
    val isExecutingMacro = com.example.automation.pipeline.ActionPipeline.isExecutingMacro
    val macroProgress = com.example.automation.pipeline.ActionPipeline.macroProgress
    val presetMacros = com.example.automation.pipeline.ActionPipeline.defaultPresetMacros

    // Phase 5: Decision Engine state flows
    val isAutoPilotActive = com.example.decision.pipeline.DecisionPipeline.isAutoPilotActive
    val decisionState = com.example.decision.pipeline.DecisionPipeline.decisionState
    val latestDecisionProposal = com.example.decision.pipeline.DecisionPipeline.latestProposal
    val decisionLogs = com.example.decision.pipeline.DecisionPipeline.recentDecisionLogs
    val totalDecisions = com.example.decision.pipeline.DecisionPipeline.totalDecisions
    val successfulDecisions = com.example.decision.pipeline.DecisionPipeline.successfulActions

    fun toggleAutoPilot() {
        val currentlyActive = isAutoPilotActive.value
        if (currentlyActive) {
            com.example.decision.engine.GameDecisionEngine.stopAutoPilot()
        } else {
            com.example.decision.engine.GameDecisionEngine.startAutoPilot()
        }
    }

    fun triggerSingleDecisionStep() {
        viewModelScope.launch {
            com.example.decision.engine.GameDecisionEngine.evaluateSingleStep()
        }
    }

    // Phase 7: Hybrid AI & Cloud Strategic Advisor
    val strategicRecommendation = com.example.strategy.coordinator.HybridStrategyCoordinator.latestRecommendation
    val isAnalyzingStrategy = com.example.strategy.coordinator.HybridStrategyCoordinator.isAnalyzing
    val strategyLogs = com.example.strategy.coordinator.HybridStrategyCoordinator.analysisLogs
    val strategySource = com.example.strategy.coordinator.HybridStrategyCoordinator.activeSource

    fun requestStrategicAdvice(overrideCloud: Boolean? = null) {
        val profile = activeProfile.value ?: allProfiles.value.firstOrNull() ?: return
        val currentVision = visionResult.value
        val allowCloud = overrideCloud ?: (userSettings.value?.cloudAiOptIn ?: false)

        val genre = try {
            com.example.core.model.GameGenre.valueOf(profile.genre)
        } catch (_: Exception) {
            com.example.core.model.GameGenre.TURN_BASED_RPG
        }

        val request = com.example.strategy.model.StrategyRequest(
            gameProfile = profile,
            currentVision = currentVision,
            activeGenre = genre,
            userGoal = "Analyze current stage layout, optimize action efficiency, and maximize victory score"
        )

        viewModelScope.launch {
            com.example.strategy.coordinator.HybridStrategyCoordinator.requestStrategicAdvice(
                request = request,
                allowCloudAi = allowCloud
            )
        }
    }

    // Phase 8: Learning Engine & Strategy Weight Evaluation
    val learningEngine by lazy { com.example.learning.engine.SessionLearningEngine(repository) }
    val totalTransitionsEvaluated = com.example.learning.pipeline.LearningPipeline.totalTransitionsEvaluated
    val weightAdjustmentsCount = com.example.learning.pipeline.LearningPipeline.weightAdjustmentsCount
    val latestLearningRecord = com.example.learning.pipeline.LearningPipeline.latestLearningRecord
    val learningFeed = com.example.learning.pipeline.LearningPipeline.learningFeed

    fun evaluateLearningTransition(
        beforeScene: com.example.vision.model.DetectedGameScene = com.example.vision.model.DetectedGameScene.BATTLE_IN_PROGRESS,
        action: com.example.automation.model.ActionCommand = com.example.automation.model.ActionCommand.TapAction(500f, 1000f, label = "Tactical Strike"),
        afterScene: com.example.vision.model.DetectedGameScene = com.example.vision.model.DetectedGameScene.VICTORY_REWARDS,
        beforeHealth: Float? = 0.50f,
        afterHealth: Float? = 0.20f
    ) {
        val profileId = activeProfile.value?.id ?: 1L
        val transition = com.example.learning.model.StateTransition(
            profileId = profileId,
            beforeScene = beforeScene,
            actionExecuted = action,
            afterScene = afterScene,
            beforeHealth = beforeHealth,
            afterHealth = afterHealth
        )
        viewModelScope.launch {
            learningEngine.processTransition(transition, sessionId = latestSession.value?.id)
        }
    }

    fun executeTap(x: Float, y: Float) {
        com.example.automation.queue.ActionQueueManager.enqueue(
            com.example.automation.model.ActionCommand.TapAction(x, y, label = "Manual Tap")
        )
    }

    fun executeSwipe(startX: Float, startY: Float, endX: Float, endY: Float) {
        com.example.automation.queue.ActionQueueManager.enqueue(
            com.example.automation.model.ActionCommand.SwipeAction(
                startX = startX,
                startY = startY,
                endX = endX,
                endY = endY,
                label = "Manual Swipe"
            )
        )
    }

    fun executeLongPress(x: Float, y: Float) {
        com.example.automation.queue.ActionQueueManager.enqueue(
            com.example.automation.model.ActionCommand.LongPressAction(x, y, label = "Manual Long Press")
        )
    }

    fun executeMacro(macro: com.example.automation.model.MacroSequence) {
        com.example.automation.queue.ActionQueueManager.executeMacro(macro)
    }

    fun triggerSyntheticFrameCapture() {
        val frame = com.example.capture.pipeline.ScreenCapturePipeline.generateSyntheticGameFrame(
            stage = "STAGE ${(1..8).random()}-${(1..5).random()}",
            bossHpPercent = (20..95).random()
        )
        viewModelScope.launch {
            com.example.vision.engine.UnifiedVisionEngine.processFrame(frame)
        }
    }

    init {
        refreshPermissions()
        viewModelScope.launch {
            activeProfile.collect { profile ->
                com.example.decision.engine.GameDecisionEngine.setActiveProfile(profile)
            }
        }
    }

    fun refreshPermissions() {
        _permissionStatus.value = PermissionManager.checkPermissions(getApplication())
    }

    fun setActiveProfile(profileId: Long) {
        viewModelScope.launch {
            repository.setActiveProfile(profileId)
        }
    }

    fun saveGameProfile(profile: GameProfile, onSaved: () -> Unit) {
        viewModelScope.launch {
            val id = repository.saveProfile(profile)
            if (activeProfile.value == null) {
                repository.setActiveProfile(id)
            }
            onSaved()
        }
    }

    fun deleteGameProfile(profile: GameProfile) {
        viewModelScope.launch {
            repository.deleteProfile(profile)
        }
    }

    fun loadProfileMemories(profileId: Long) {
        viewModelScope.launch {
            repository.getMemoriesForProfile(profileId).collect {
                _selectedProfileMemories.value = it
            }
        }
    }

    fun saveUserSettings(settings: UserSettings, onSaved: () -> Unit) {
        viewModelScope.launch {
            repository.updateSettings(settings)
            onSaved()
        }
    }

    fun startAiAutomation(resultCode: Int, resultData: Intent) {
        SafetyController.resetEmergencyStop()
        val context = getApplication<Application>()
        val targetFps = activeProfile.value?.targetFps ?: 10
        val startCaptureIntent = Intent(context, ScreenCaptureService::class.java).apply {
            action = ScreenCaptureService.ACTION_START
            putExtra(ScreenCaptureService.EXTRA_RESULT_CODE, resultCode)
            putExtra(ScreenCaptureService.EXTRA_RESULT_DATA, resultData)
            putExtra(ScreenCaptureService.EXTRA_TARGET_FPS, targetFps)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(startCaptureIntent)
        } else {
            context.startService(startCaptureIntent)
        }

        // Also start floating HUD if enabled
        if (userSettings.value?.enableOverlayHud != false &&
            _permissionStatus.value.hasOverlayPermission
        ) {
            startFloatingHud()
        }

        // Record a new running session
        val currentProfile = activeProfile.value
        if (currentProfile != null) {
            viewModelScope.launch {
                repository.startSession(currentProfile.id, currentProfile.name)
            }
        }
    }

    fun stopAiAutomation() {
        val context = getApplication<Application>()
        val stopCaptureIntent = Intent(context, ScreenCaptureService::class.java).apply {
            action = ScreenCaptureService.ACTION_STOP
        }
        context.startService(stopCaptureIntent)
    }

    fun triggerEmergencyStop() {
        SafetyController.triggerEmergencyStop("Emergency stop requested by user")
        val context = getApplication<Application>()
        val stopIntent = Intent(context, ScreenCaptureService::class.java).apply {
            action = ScreenCaptureService.ACTION_EMERGENCY_STOP
        }
        context.startService(stopIntent)
    }

    fun startFloatingHud() {
        val context = getApplication<Application>()
        val intent = Intent(context, FloatingAssistantService::class.java).apply {
            action = FloatingAssistantService.ACTION_SHOW
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    fun stopFloatingHud() {
        val context = getApplication<Application>()
        val intent = Intent(context, FloatingAssistantService::class.java).apply {
            action = FloatingAssistantService.ACTION_HIDE
        }
        context.startService(intent)
    }
}
