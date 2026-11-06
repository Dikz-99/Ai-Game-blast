package com.example.adapter.idle

import com.example.adapter.base.IGameAdapter
import com.example.adapter.model.AdapterDecision
import com.example.adapter.model.AdapterGameState
import com.example.automation.model.ActionCommand
import com.example.automation.model.MacroSequence
import com.example.core.model.GameGenre
import com.example.core.model.VisualBoundingBox
import com.example.vision.model.DetectedGameScene
import com.example.vision.model.VisionResult

class IdleClickerAdapter : IGameAdapter {

    override val genre: GameGenre = GameGenre.IDLE_CLICKER
    override val adapterName: String = "Quantum Idle Clicker Adapter"
    override val description: String = "Optimizes auto-click cadence, gold accumulation, and generator upgrade cycles."

    private var clickCycleCounter = 0

    override fun extractState(vision: VisionResult): AdapterGameState {
        val keywords = vision.textSnippets.map { it.text.uppercase() }
        val hasUpgrade = keywords.any { it.contains("UPGRADE") || it.contains("BUY") || it.contains("LEVEL") }

        return AdapterGameState(
            genre = genre,
            scene = vision.scene,
            healthRatio = null,
            availableSkills = if (hasUpgrade) listOf("UPGRADE_AVAILABLE") else emptyList(),
            interactableElements = vision.boundingBoxes,
            extractedKeywords = keywords,
            isActionReady = true,
            metrics = mapOf(
                "Upgrades Available" to "$hasUpgrade",
                "Cycle Count" to "$clickCycleCounter"
            )
        )
    }

    override fun decideAction(
        state: AdapterGameState,
        screenWidth: Int,
        screenHeight: Int
    ): AdapterDecision {
        clickCycleCounter++

        // Every 6 cycles, scan and tap upgrade buttons if available
        val upgradeKeywordSnippet = state.extractedKeywords.firstOrNull {
            it.contains("UPGRADE") || it.contains("BUY") || it.contains("LEVEL")
        }

        if (clickCycleCounter % 6 == 0 && upgradeKeywordSnippet != null) {
            val upgradeBox = state.interactableElements.firstOrNull {
                it.label.contains("UPGRADE") || it.label.contains("CLAIM")
            }
            val target = upgradeBox?.toCenterPoint(screenWidth, screenHeight)
                ?: Pair(screenWidth * 0.82f, screenHeight * 0.45f)

            return AdapterDecision(
                action = ActionCommand.TapAction(target.first, target.second, label = "Reinvest Upgrade"),
                confidence = 0.90f,
                strategyNotes = "Reinvesting accumulated idle currency into generators."
            )
        }

        // Otherwise: Turbo click on main center resource crystal
        val centerX = screenWidth * 0.50f
        val centerY = screenHeight * 0.42f

        return AdapterDecision(
            macro = MacroSequence(
                id = "idle_turbo_burst",
                name = "Turbo Resource Harvest",
                description = "Rapid 4-tap burst on resource hub",
                actions = listOf(
                    ActionCommand.TapAction(centerX, centerY, durationMs = 45L, label = "Click 1"),
                    ActionCommand.WaitAction(40L),
                    ActionCommand.TapAction(centerX, centerY, durationMs = 45L, label = "Click 2"),
                    ActionCommand.WaitAction(40L),
                    ActionCommand.TapAction(centerX, centerY, durationMs = 45L, label = "Click 3"),
                    ActionCommand.WaitAction(40L),
                    ActionCommand.TapAction(centerX, centerY, durationMs = 45L, label = "Click 4")
                )
            ),
            confidence = 0.96f,
            strategyNotes = "Executing high-cadence resource extraction taps."
        )
    }

    override fun getPresetMacros(): List<MacroSequence> {
        return listOf(
            MacroSequence(
                id = "idle_hyper_click",
                name = "Hyper Click Burst (6x)",
                description = "Accelerates idle generator income with ultra-fast bursts",
                actions = (1..6).flatMap { i ->
                    listOf(
                        ActionCommand.TapAction(540f, 1000f, durationMs = 40L, label = "Burst Tap $i"),
                        ActionCommand.WaitAction(30L)
                    )
                }
            ),
            MacroSequence(
                id = "idle_upgrade_sweep",
                name = "Auto-Reinvest Upgrade Sweep",
                description = "Taps upgrade row from top to bottom",
                actions = listOf(
                    ActionCommand.TapAction(850f, 600f, label = "Upgrade Tier 1"),
                    ActionCommand.WaitAction(120L),
                    ActionCommand.TapAction(850f, 800f, label = "Upgrade Tier 2"),
                    ActionCommand.WaitAction(120L),
                    ActionCommand.TapAction(850f, 1000f, label = "Upgrade Tier 3")
                )
            )
        )
    }

    private fun VisualBoundingBox.toCenterPoint(width: Int, height: Int): Pair<Float, Float> {
        return Pair((left + right) / 2f * width, (top + bottom) / 2f * height)
    }
}
