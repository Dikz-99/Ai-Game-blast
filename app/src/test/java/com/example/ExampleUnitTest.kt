package com.example

import com.example.core.model.GameGenre
import com.example.core.safety.SafetyController
import com.example.core.safety.SafetyValidationResult
import com.example.vision.detector.GameElementDetector
import com.example.vision.model.DetectedGameScene
import com.example.vision.model.RecognizedTextSnippet
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ExampleUnitTest {

    @Before
    fun setup() {
        SafetyController.resetEmergencyStop()
        SafetyController.updateScreenDimensions(1080, 2400)
    }

    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun safetyController_approvesCoordinatesWithinBounds() {
        val result1 = SafetyController.validateAction(
            x = 500f,
            y = 1200f,
            maxActionsPerSecond = 5,
            minIntervalMs = 100L
        )
        assertTrue(result1 is SafetyValidationResult.Approved)

        // Rapid tap without delay should be throttled
        val result2 = SafetyController.validateAction(
            x = 500f,
            y = 1200f,
            maxActionsPerSecond = 5,
            minIntervalMs = 100L
        )
        assertTrue(result2 is SafetyValidationResult.Rejected)
    }

    @Test
    fun safetyController_emergencyStopRejectsAllActions() {
        SafetyController.triggerEmergencyStop("Test kill switch")
        assertTrue(SafetyController.isEmergencyStopped.value)

        val result = SafetyController.validateAction(
            x = 500f,
            y = 1000f,
            maxActionsPerSecond = 5,
            minIntervalMs = 50L
        )
        assertTrue(result is SafetyValidationResult.Rejected)
    }

    @Test
    fun gameGenre_propertiesMatchExpected() {
        assertEquals("Turn-Based RPG", GameGenre.TURN_BASED_RPG.displayName)
        assertEquals("Match-3 Puzzle", GameGenre.MATCH_3_PUZZLE.displayName)
    }

    @Test
    fun gameElementDetector_identifiesBattleSceneAndButtons() {
        val snippets = listOf(
            RecognizedTextSnippet("STAGE 4-2", 0.95f, 0.1f, 0.05f, 0.4f, 0.1f),
            RecognizedTextSnippet("ATTACK", 0.92f, 0.6f, 0.8f, 0.9f, 0.9f),
            RecognizedTextSnippet("SKILL 1 READY", 0.90f, 0.2f, 0.8f, 0.5f, 0.9f)
        )

        val result = GameElementDetector.detectElements(snippets, healthBarPercent = 0.65f)

        assertEquals(DetectedGameScene.BATTLE_IN_PROGRESS, result.scene)
        assertTrue(result.confidence > 0.8f)
        assertTrue(result.boundingBoxes.any { it.label == "ATTACK_BUTTON" })
        assertTrue(result.boundingBoxes.any { it.label == "SKILL_BUTTON" })
        assertTrue(result.boundingBoxes.any { it.label == "BOSS_HEALTH_BAR" })
        assertNotNull(result.primaryTarget)
        assertEquals("ATTACK_BUTTON", result.primaryTarget?.label)
    }

    @Test
    fun gameElementDetector_identifiesVictoryScene() {
        val snippets = listOf(
            RecognizedTextSnippet("VICTORY! STAGE COMPLETE", 0.96f, 0.2f, 0.3f, 0.8f, 0.4f),
            RecognizedTextSnippet("CLAIM REWARD", 0.94f, 0.3f, 0.7f, 0.7f, 0.8f)
        )

        val result = GameElementDetector.detectElements(snippets, healthBarPercent = null)

        assertEquals(DetectedGameScene.VICTORY_REWARDS, result.scene)
        assertTrue(result.boundingBoxes.any { it.label == "CLAIM_BUTTON" })
    }

    @Test
    fun gameElementDetector_identifiesDefeatScene() {
        val snippets = listOf(
            RecognizedTextSnippet("DEFEAT", 0.95f, 0.3f, 0.3f, 0.7f, 0.4f),
            RecognizedTextSnippet("RETRY STAGE", 0.91f, 0.3f, 0.7f, 0.7f, 0.8f)
        )

        val result = GameElementDetector.detectElements(snippets, healthBarPercent = null)

        assertEquals(DetectedGameScene.DEFEAT_RETRY, result.scene)
    }

    @Test
    fun priorityRuleEvaluator_victorySceneGeneratesClaimTap() {
        val vision = com.example.vision.model.VisionResult(
            scene = DetectedGameScene.VICTORY_REWARDS,
            confidence = 0.95f,
            boundingBoxes = listOf(
                com.example.core.model.VisualBoundingBox("c1", "CLAIM_BUTTON", 0.92f, 0.4f, 0.7f, 0.6f, 0.8f, "CLAIM")
            ),
            textSnippets = emptyList(),
            timestamp = System.currentTimeMillis()
        )

        val proposal = com.example.decision.evaluator.PriorityRuleEvaluator.evaluate(vision, 1080, 2400)

        assertEquals(DetectedGameScene.VICTORY_REWARDS, proposal.scene)
        assertEquals(100, proposal.rulePriority)
        assertTrue(proposal.action is com.example.automation.model.ActionCommand.TapAction)
        assertEquals("Claim Reward Tap", proposal.action?.label)
    }

    @Test
    fun priorityRuleEvaluator_battleScenePrioritizesTacticalSkill() {
        val vision = com.example.vision.model.VisionResult(
            scene = DetectedGameScene.BATTLE_IN_PROGRESS,
            confidence = 0.91f,
            boundingBoxes = listOf(
                com.example.core.model.VisualBoundingBox("s1", "SKILL_BUTTON", 0.95f, 0.2f, 0.8f, 0.4f, 0.9f, "SKILL"),
                com.example.core.model.VisualBoundingBox("a1", "ATTACK_BUTTON", 0.88f, 0.7f, 0.8f, 0.9f, 0.9f, "ATTACK")
            ),
            textSnippets = emptyList(),
            timestamp = System.currentTimeMillis()
        )

        val proposal = com.example.decision.evaluator.PriorityRuleEvaluator.evaluate(vision, 1080, 2400, preferSkills = true)

        assertEquals(DetectedGameScene.BATTLE_IN_PROGRESS, proposal.scene)
        assertEquals(90, proposal.rulePriority)
        assertEquals("Cast Tactical Skill", proposal.action?.label)
    }

    @Test
    fun priorityRuleEvaluator_loadingSceneGeneratesWaitAction() {
        val vision = com.example.vision.model.VisionResult(
            scene = DetectedGameScene.LOADING_SCREEN,
            confidence = 0.95f,
            boundingBoxes = emptyList(),
            textSnippets = emptyList(),
            timestamp = System.currentTimeMillis()
        )

        val proposal = com.example.decision.evaluator.PriorityRuleEvaluator.evaluate(vision, 1080, 2400)

        assertEquals(DetectedGameScene.LOADING_SCREEN, proposal.scene)
        assertTrue(proposal.action is com.example.automation.model.ActionCommand.WaitAction)
        assertEquals(10, proposal.rulePriority)
    }

    @Test
    fun adapterRegistry_resolvesCorrectAdapterForEachGenre() {
        val rpg = com.example.adapter.registry.AdapterRegistry.getAdapter(GameGenre.TURN_BASED_RPG)
        val idle = com.example.adapter.registry.AdapterRegistry.getAdapter(GameGenre.IDLE_CLICKER)
        val puzzle = com.example.adapter.registry.AdapterRegistry.getAdapter(GameGenre.MATCH_3_PUZZLE)
        val custom = com.example.adapter.registry.AdapterRegistry.getAdapter(GameGenre.CUSTOM)

        assertEquals("Turn-Based RPG Tactical Adapter", rpg.adapterName)
        assertEquals("Quantum Idle Clicker Adapter", idle.adapterName)
        assertEquals("Neon Jewel Match-3 Adapter", puzzle.adapterName)
        assertEquals("Universal Custom Game Adapter", custom.adapterName)
    }

    @Test
    fun turnBasedRpgAdapter_bossFinisherTriggeredWhenHpCritical() {
        val adapter = com.example.adapter.rpg.TurnBasedRpgAdapter()
        val vision = com.example.vision.model.VisionResult(
            scene = DetectedGameScene.BATTLE_IN_PROGRESS,
            confidence = 0.95f,
            healthPercentage = 0.22f, // Critical boss HP (< 30%)
            boundingBoxes = listOf(
                com.example.core.model.VisualBoundingBox("s1", "SKILL_BUTTON", 0.95f, 0.2f, 0.8f, 0.4f, 0.9f, "SKILL")
            )
        )

        val state = adapter.extractState(vision)
        val decision = adapter.decideAction(state, 1080, 2400)

        assertNotNull(decision.macro)
        assertEquals("rpg_boss_finisher", decision.macro?.id)
        assertTrue(decision.confidence > 0.90f)
    }

    @Test
    fun idleClickerAdapter_generatesTurboClickMacro() {
        val adapter = com.example.adapter.idle.IdleClickerAdapter()
        val vision = com.example.vision.model.VisionResult(
            scene = DetectedGameScene.BATTLE_IN_PROGRESS,
            confidence = 0.90f
        )

        val state = adapter.extractState(vision)
        val decision = adapter.decideAction(state, 1080, 2400)

        assertNotNull(decision.macro)
        assertEquals("idle_turbo_burst", decision.macro?.id)
    }

    @Test
    fun match3PuzzleAdapter_generatesValidSwipeAction() {
        val adapter = com.example.adapter.puzzle.Match3PuzzleAdapter()
        val vision = com.example.vision.model.VisionResult(
            scene = DetectedGameScene.BATTLE_IN_PROGRESS,
            confidence = 0.88f
        )

        val state = adapter.extractState(vision)
        val decision = adapter.decideAction(state, 1080, 2400)

        assertNotNull(decision.action)
        assertTrue(decision.action is com.example.automation.model.ActionCommand.SwipeAction)
        val swipe = decision.action as com.example.automation.model.ActionCommand.SwipeAction
        assertTrue(swipe.startX > 0f)
        assertTrue(swipe.endX > 0f)
    }

    @Test
    fun localStrategyGateway_generatesRpgAdviceWithTacticalPointers() = kotlinx.coroutines.runBlocking {
        val gateway = com.example.strategy.gateway.LocalStrategyGateway()
        val profile = com.example.data.local.entity.GameProfile(
            id = 1L,
            name = "Cyber RPG Arena",
            packageName = "com.game.cyberrpg",
            genre = "TURN_BASED_RPG"
        )
        val vision = com.example.vision.model.VisionResult(
            scene = DetectedGameScene.BATTLE_IN_PROGRESS,
            confidence = 0.92f,
            healthPercentage = 0.25f
        )
        val request = com.example.strategy.model.StrategyRequest(
            gameProfile = profile,
            currentVision = vision,
            activeGenre = GameGenre.TURN_BASED_RPG
        )

        val advice = gateway.generateStrategy(request)

        assertNotNull(advice)
        assertEquals(com.example.strategy.model.StrategySource.LOCAL_HEURISTIC, advice.source)
        assertTrue(advice.title.contains("Boss Critical", ignoreCase = true))
        assertTrue(advice.tacticalTips.isNotEmpty())
    }

    @Test
    fun hybridStrategyCoordinator_offlineModeUsesLocalHeuristics() = kotlinx.coroutines.runBlocking {
        val coordinator = com.example.strategy.coordinator.HybridStrategyCoordinator
        val profile = com.example.data.local.entity.GameProfile(
            id = 2L,
            name = "Quantum Idle Miner",
            packageName = "com.game.idleminer",
            genre = "IDLE_CLICKER"
        )
        val vision = com.example.vision.model.VisionResult(
            scene = DetectedGameScene.BATTLE_IN_PROGRESS,
            confidence = 0.90f
        )
        val request = com.example.strategy.model.StrategyRequest(
            gameProfile = profile,
            currentVision = vision,
            activeGenre = GameGenre.IDLE_CLICKER
        )

        val advice = coordinator.requestStrategicAdvice(request, allowCloudAi = false)

        assertNotNull(advice)
        assertEquals(com.example.strategy.model.StrategySource.LOCAL_HEURISTIC, advice.source)
        assertEquals(advice, coordinator.latestRecommendation.value)
    }
}
