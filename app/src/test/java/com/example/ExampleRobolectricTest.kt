package com.example

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.test.core.app.ApplicationProvider
import com.example.capture.pipeline.ScreenCapturePipeline
import com.example.vision.cv.ColorDetector
import com.example.vision.engine.UnifiedVisionEngine
import com.example.vision.model.DetectedGameScene
import com.example.vision.pipeline.VisionPipeline
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun readStringFromContext() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("AI Game Blast", appName)
    }

    @Test
    fun screenCapturePipeline_generatesSyntheticGameFrameAndTracksMetrics() {
        ScreenCapturePipeline.reset()
        ScreenCapturePipeline.updateTargetFps(15)

        val frame = ScreenCapturePipeline.generateSyntheticGameFrame(
            width = 720,
            height = 1280,
            stage = "STAGE 4-2",
            bossHpPercent = 75
        )

        assertEquals(720, frame.width)
        assertEquals(1280, frame.height)
        assertEquals(1L, frame.sequenceNumber)
        assertTrue(ScreenCapturePipeline.metrics.value.isCapturing)
        assertEquals(15, ScreenCapturePipeline.metrics.value.targetFps)

        ScreenCapturePipeline.recordDroppedFrame()
        assertEquals(1L, ScreenCapturePipeline.metrics.value.droppedFrames)
    }

    @Test
    fun colorDetector_calculatesHealthBarPercentage() {
        val bitmap = Bitmap.createBitmap(100, 20, Bitmap.Config.ARGB_8888)
        // Set left half to RED and right half to BLACK
        for (x in 0 until 50) {
            bitmap.setPixel(x, 10, Color.RED)
        }
        for (x in 50 until 100) {
            bitmap.setPixel(x, 10, Color.BLACK)
        }

        val ratio = ColorDetector.calculateHealthBarPercentage(
            bitmap,
            leftNorm = 0f,
            topNorm = 0f,
            rightNorm = 1f,
            bottomNorm = 1f
        )

        // Expected 50% red bar
        assertTrue("Expected ratio around 0.5, but got $ratio", ratio in 0.45f..0.55f)
    }

    @Test
    fun unifiedVisionEngine_processesFrameAndEmitsResult() = runBlocking {
        VisionPipeline.reset()

        val frame = ScreenCapturePipeline.generateSyntheticGameFrame(
            width = 720,
            height = 1280,
            stage = "STAGE 4-2",
            bossHpPercent = 65
        )

        val result = UnifiedVisionEngine.processFrame(frame)

        assertEquals(DetectedGameScene.BATTLE_IN_PROGRESS, result.scene)
        assertTrue(result.boundingBoxes.isNotEmpty())
        assertTrue(result.boundingBoxes.any { it.label == "ATTACK_BUTTON" })
        assertEquals(result, VisionPipeline.latestResult.value)
    }

    @Test
    fun humanizedPathGenerator_createsCurvedSwipePath() {
        val path = com.example.automation.path.HumanizedPathGenerator.createCurvedSwipePath(
            startX = 200f,
            startY = 500f,
            endX = 800f,
            endY = 500f,
            curveDeviation = 0.05f
        )
        org.junit.Assert.assertFalse(path.isEmpty)
    }

    @Test
    fun humanizedPathGenerator_addsJitterWithinLimits() {
        val originalX = 500f
        val originalY = 1000f
        val jittered = com.example.automation.path.HumanizedPathGenerator.generateJitteredPoint(originalX, originalY, maxJitter = 4f)

        assertTrue(Math.abs(jittered.x - originalX) <= 4.01f)
        assertTrue(Math.abs(jittered.y - originalY) <= 4.01f)
    }

    @Test
    fun actionModels_createExpectedCommands() {
        val tap = com.example.automation.model.ActionCommand.TapAction(100f, 200f)
        val swipe = com.example.automation.model.ActionCommand.SwipeAction(100f, 200f, 300f, 400f)
        val macro = com.example.automation.model.MacroSequence("m1", "Test Macro", "Description", listOf(tap, swipe))

        assertEquals("Tap", tap.label)
        assertEquals("Swipe", swipe.label)
        assertEquals(2, macro.actions.size)
    }

    @Test
    fun gameDecisionEngine_evaluatesSingleStepAndRecordsDecision() = runBlocking {
        com.example.decision.pipeline.DecisionPipeline.reset()
        com.example.core.safety.SafetyController.resetEmergencyStop()

        val vision = com.example.vision.model.VisionResult(
            scene = DetectedGameScene.VICTORY_REWARDS,
            confidence = 0.95f,
            boundingBoxes = listOf(
                com.example.core.model.VisualBoundingBox("c1", "CLAIM_BUTTON", 0.92f, 0.4f, 0.7f, 0.6f, 0.8f, "CLAIM")
            ),
            textSnippets = emptyList(),
            timestamp = System.currentTimeMillis()
        )

        val proposal = com.example.decision.engine.GameDecisionEngine.evaluateSingleStep(vision)

        org.junit.Assert.assertNotNull(proposal)
        assertEquals(DetectedGameScene.VICTORY_REWARDS, proposal?.scene)
        assertEquals(1L, com.example.decision.pipeline.DecisionPipeline.totalDecisions.value)
        assertEquals(proposal, com.example.decision.pipeline.DecisionPipeline.latestProposal.value)
    }

    @Test
    fun gameDecisionEngine_emergencyStopHaltsEvaluations() = runBlocking {
        com.example.core.safety.SafetyController.triggerEmergencyStop("Test kill switch")

        val vision = com.example.vision.model.VisionResult(
            scene = DetectedGameScene.BATTLE_IN_PROGRESS,
            confidence = 0.95f,
            boundingBoxes = emptyList(),
            textSnippets = emptyList(),
            timestamp = System.currentTimeMillis()
        )

        val proposal = com.example.decision.engine.GameDecisionEngine.evaluateSingleStep(vision)

        org.junit.Assert.assertNull(proposal)
        assertEquals(
            com.example.decision.model.DecisionState.EMERGENCY_HALTED,
            com.example.decision.pipeline.DecisionPipeline.decisionState.value
        )
    }

    @Test
    fun hybridStrategyCoordinator_fallbackGracefullyWhenCloudUnavailable() = runBlocking {
        val coordinator = com.example.strategy.coordinator.HybridStrategyCoordinator
        val profile = com.example.data.local.entity.GameProfile(
            id = 3L,
            name = "Neon Jewel Odyssey",
            packageName = "com.game.neonjewel",
            genre = "MATCH_3_PUZZLE"
        )
        val vision = com.example.vision.model.VisionResult(
            scene = DetectedGameScene.BATTLE_IN_PROGRESS,
            confidence = 0.88f
        )
        val request = com.example.strategy.model.StrategyRequest(
            gameProfile = profile,
            currentVision = vision,
            activeGenre = com.example.core.model.GameGenre.MATCH_3_PUZZLE
        )

        val advice = coordinator.requestStrategicAdvice(request, allowCloudAi = true)

        org.junit.Assert.assertNotNull(advice)
        assertTrue(advice.tacticalTips.isNotEmpty())
        assertEquals(com.example.strategy.model.StrategySource.LOCAL_HEURISTIC, advice.source)
    }

    @Test
    fun learningModels_evaluateOutcomeCorrectly() {
        val victoryTransition = com.example.learning.model.StateTransition(
            profileId = 1L,
            beforeScene = DetectedGameScene.BATTLE_IN_PROGRESS,
            actionExecuted = com.example.automation.model.ActionCommand.TapAction(540f, 1850f, label = "Finisher"),
            afterScene = DetectedGameScene.VICTORY_REWARDS
        )
        assertEquals(com.example.learning.model.TransitionOutcome.STAGE_CLEARED, victoryTransition.evaluateOutcome())
        assertEquals(0.20f, victoryTransition.evaluateOutcome().weightScore, 0.001f)

        val defeatTransition = com.example.learning.model.StateTransition(
            profileId = 1L,
            beforeScene = DetectedGameScene.BATTLE_IN_PROGRESS,
            actionExecuted = com.example.automation.model.ActionCommand.TapAction(540f, 1850f, label = "Finisher"),
            afterScene = DetectedGameScene.DEFEAT_RETRY
        )
        assertEquals(com.example.learning.model.TransitionOutcome.DEFEAT, defeatTransition.evaluateOutcome())
        assertEquals(-0.25f, defeatTransition.evaluateOutcome().weightScore, 0.001f)

        val bossDmgTransition = com.example.learning.model.StateTransition(
            profileId = 1L,
            beforeScene = DetectedGameScene.BATTLE_IN_PROGRESS,
            actionExecuted = com.example.automation.model.ActionCommand.TapAction(540f, 1850f, label = "Skill"),
            afterScene = DetectedGameScene.BATTLE_IN_PROGRESS,
            beforeHealth = 0.50f,
            afterHealth = 0.20f
        )
        assertEquals(com.example.learning.model.TransitionOutcome.OBJECTIVE_ADVANCED, bossDmgTransition.evaluateOutcome())
        assertEquals(0.08f, bossDmgTransition.evaluateOutcome().weightScore, 0.001f)
    }

    @Test
    fun sessionLearningEngine_adjustsWeightsAndUpdatesMemory() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = androidx.room.Room.inMemoryDatabaseBuilder(
            context,
            com.example.data.local.db.GameBlastDatabase::class.java
        ).allowMainThreadQueries().build()

        val repository = com.example.data.repository.GameBlastRepository(
            db.gameProfileDao(),
            db.gameSessionDao(),
            db.gameObservationDao(),
            db.actionHistoryDao(),
            db.strategyMemoryDao(),
            db.userSettingsDao()
        )

        // Seed a profile
        val profileId = repository.saveProfile(
            com.example.data.local.entity.GameProfile(
                name = "Test Learning Game",
                packageName = "com.test.learn",
                genre = "TURN_BASED_RPG"
            )
        )

        val engine = com.example.learning.engine.SessionLearningEngine(repository)

        // 1. First transition: STAGE_CLEARED (victory rewards)
        val victoryTransition = com.example.learning.model.StateTransition(
            profileId = profileId,
            beforeScene = DetectedGameScene.BATTLE_IN_PROGRESS,
            actionExecuted = com.example.automation.model.ActionCommand.TapAction(540f, 1850f, label = "Finisher"),
            afterScene = DetectedGameScene.VICTORY_REWARDS
        )

        val record1 = engine.processTransition(victoryTransition)
        assertEquals(com.example.learning.model.TransitionOutcome.STAGE_CLEARED, record1.outcome)
        assertEquals(1.20f, record1.newWeight, 0.01f)

        // Check memory in database
        val memory1 = repository.getMemoryBySignature(profileId, "BATTLE_IN_PROGRESS:Finisher")
        org.junit.Assert.assertNotNull(memory1)
        assertEquals(1.20f, memory1!!.weight, 0.01f)
        assertEquals(1, memory1.successCount)
        assertEquals(0, memory1.failureCount)

        // 2. Second transition: DEFEAT_RETRY (failure penalty)
        val defeatTransition = com.example.learning.model.StateTransition(
            profileId = profileId,
            beforeScene = DetectedGameScene.BATTLE_IN_PROGRESS,
            actionExecuted = com.example.automation.model.ActionCommand.TapAction(540f, 1850f, label = "Finisher"),
            afterScene = DetectedGameScene.DEFEAT_RETRY
        )

        val record2 = engine.processTransition(defeatTransition)
        assertEquals(com.example.learning.model.TransitionOutcome.DEFEAT, record2.outcome)
        // 1.20 - 0.25 = 0.95
        assertEquals(0.95f, record2.newWeight, 0.01f)

        val memory2 = repository.getMemoryBySignature(profileId, "BATTLE_IN_PROGRESS:Finisher")
        assertEquals(0.95f, memory2!!.weight, 0.01f)
        assertEquals(1, memory2.successCount)
        assertEquals(1, memory2.failureCount)

        assertTrue(com.example.learning.pipeline.LearningPipeline.totalTransitionsEvaluated.value >= 2)
        assertTrue(com.example.learning.pipeline.LearningPipeline.weightAdjustmentsCount.value >= 2)

        db.close()
    }

    @Test
    fun cuj_endToEndClosedLoopAutonomousCycle() = runBlocking {
        // Reset safety and pipelines to ensure clean testing environment
        com.example.core.safety.SafetyController.resetEmergencyStop()
        com.example.decision.pipeline.DecisionPipeline.reset()

        // Step 1: Initialize Database, Repository, and Profile
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = androidx.room.Room.inMemoryDatabaseBuilder(
            context,
            com.example.data.local.db.GameBlastDatabase::class.java
        ).allowMainThreadQueries().build()

        val repository = com.example.data.repository.GameBlastRepository(
            db.gameProfileDao(),
            db.gameSessionDao(),
            db.gameObservationDao(),
            db.actionHistoryDao(),
            db.strategyMemoryDao(),
            db.userSettingsDao()
        )

        val profile = com.example.data.local.entity.GameProfile(
            name = "Cyber RPG Arena",
            packageName = "com.aistudio.cyberarena",
            genre = com.example.core.model.GameGenre.TURN_BASED_RPG.name,
            targetFps = 15,
            maxTapsPerSecond = 4
        )
        val profileId = repository.saveProfile(profile)
        val activeProfile = profile.copy(id = profileId)
        val sessionId = repository.startSession(profileId, activeProfile.name)

        // Step 2: Screen Capture Pipeline generates synthetic game frame
        val syntheticFrame = ScreenCapturePipeline.generateSyntheticGameFrame(
            width = 720,
            height = 1280,
            stage = "STAGE 7-3",
            bossHpPercent = 25
        )
        assertEquals(720, syntheticFrame.width)
        assertEquals(1280, syntheticFrame.height)

        // Step 3: Computer Vision & OCR analyzes the frame
        val visionResult = UnifiedVisionEngine.processFrame(syntheticFrame)
        assertEquals(DetectedGameScene.BATTLE_IN_PROGRESS, visionResult.scene)
        assertTrue(visionResult.boundingBoxes.isNotEmpty())

        // Step 4: Modular Game Adapter processes the game state
        val adapter = com.example.adapter.registry.AdapterRegistry.getAdapter(com.example.core.model.GameGenre.TURN_BASED_RPG)
        val adapterState = adapter.extractState(visionResult)
        val adapterDecision = adapter.decideAction(adapterState, 720, 1280)
        assertTrue(adapterDecision.confidence > 0.7f)

        // Step 5: Autonomous Decision Engine produces action proposal
        com.example.decision.engine.GameDecisionEngine.setActiveProfile(activeProfile)
        val decisionProposal = com.example.decision.engine.GameDecisionEngine.evaluateSingleStep(visionResult)
        org.junit.Assert.assertNotNull(decisionProposal)
        assertEquals(DetectedGameScene.BATTLE_IN_PROGRESS, decisionProposal?.scene)

        val action = decisionProposal?.action ?: com.example.automation.model.ActionCommand.TapAction(540f, 1850f, label = "Finisher")

        // Step 6: Safety Controller verifies the proposed action
        com.example.core.safety.SafetyController.resetEmergencyStop()
        val safetyResult = com.example.core.safety.SafetyController.validateAction(
            x = 540f,
            y = 1850f,
            maxActionsPerSecond = 4,
            minIntervalMs = 200L
        )
        assertTrue("Action must pass safety controller", safetyResult is com.example.core.safety.SafetyValidationResult.Approved)

        // Step 7: Hybrid Strategy Advisor provides high-level tactical counsel
        val strategyRequest = com.example.strategy.model.StrategyRequest(
            gameProfile = activeProfile,
            currentVision = visionResult,
            activeGenre = com.example.core.model.GameGenre.TURN_BASED_RPG
        )
        val strategyAdvice = com.example.strategy.coordinator.HybridStrategyCoordinator.requestStrategicAdvice(
            request = strategyRequest,
            allowCloudAi = true
        )
        org.junit.Assert.assertNotNull(strategyAdvice)
        assertTrue(strategyAdvice.tacticalTips.isNotEmpty())

        // Step 8: Closed Loop Learning Engine updates weights upon outcome evaluation
        val learningEngine = com.example.learning.engine.SessionLearningEngine(repository)
        val transition = com.example.learning.model.StateTransition(
            profileId = profileId,
            beforeScene = DetectedGameScene.BATTLE_IN_PROGRESS,
            actionExecuted = action,
            afterScene = DetectedGameScene.VICTORY_REWARDS,
            beforeHealth = 0.25f,
            afterHealth = 0.0f
        )
        val learningRecord = learningEngine.processTransition(transition, sessionId = sessionId)
        assertEquals(com.example.learning.model.TransitionOutcome.STAGE_CLEARED, learningRecord.outcome)
        assertEquals(1.20f, learningRecord.newWeight, 0.01f)

        // Verify persisted strategy memory in Room DB
        val memory = repository.getMemoryBySignature(profileId, "${DetectedGameScene.BATTLE_IN_PROGRESS.name}:${action.label}")
        org.junit.Assert.assertNotNull(memory)
        assertEquals(1.20f, memory!!.weight, 0.01f)
        assertEquals(1, memory.successCount)

        db.close()
    }
}
