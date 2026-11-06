package com.example.adapter.rpg

import com.example.adapter.base.IGameAdapter
import com.example.adapter.model.AdapterDecision
import com.example.adapter.model.AdapterGameState
import com.example.automation.model.ActionCommand
import com.example.automation.model.MacroSequence
import com.example.core.model.GameGenre
import com.example.core.model.VisualBoundingBox
import com.example.vision.model.DetectedGameScene
import com.example.vision.model.VisionResult

class TurnBasedRpgAdapter : IGameAdapter {

    override val genre: GameGenre = GameGenre.TURN_BASED_RPG
    override val adapterName: String = "Turn-Based RPG Tactical Adapter"
    override val description: String = "Optimizes skill rotations, boss targeting, and auto-battle pacing."

    override fun extractState(vision: VisionResult): AdapterGameState {
        val skills = vision.boundingBoxes
            .filter { it.label.contains("SKILL", ignoreCase = true) }
            .map { it.label }

        val keywords = vision.textSnippets.map { it.text.uppercase() }

        return AdapterGameState(
            genre = genre,
            scene = vision.scene,
            healthRatio = vision.healthPercentage,
            availableSkills = skills,
            interactableElements = vision.boundingBoxes,
            extractedKeywords = keywords,
            isActionReady = vision.hasInteractableButton,
            metrics = mapOf(
                "Skill Count" to "${skills.size}",
                "Boss HP" to (vision.healthPercentage?.let { "${(it * 100).toInt()}%" } ?: "N/A"),
                "Combat Active" to "${vision.scene == DetectedGameScene.BATTLE_IN_PROGRESS}"
            )
        )
    }

    override fun decideAction(
        state: AdapterGameState,
        screenWidth: Int,
        screenHeight: Int
    ): AdapterDecision {
        return when (state.scene) {
            DetectedGameScene.VICTORY_REWARDS -> {
                val claimBox = state.interactableElements.firstOrNull { it.label == "CLAIM_BUTTON" }
                val target = claimBox?.toCenterPoint(screenWidth, screenHeight)
                    ?: Pair(screenWidth * 0.5f, screenHeight * 0.78f)

                AdapterDecision(
                    action = ActionCommand.TapAction(target.first, target.second, label = "RPG Claim Rewards"),
                    confidence = 0.95f,
                    strategyNotes = "RPG Stage Cleared. Claiming experience and loot."
                )
            }

            DetectedGameScene.DEFEAT_RETRY -> {
                val retryBox = state.interactableElements.firstOrNull { it.label == "RETRY_BUTTON" }
                val target = retryBox?.toCenterPoint(screenWidth, screenHeight)
                    ?: Pair(screenWidth * 0.5f, screenHeight * 0.75f)

                AdapterDecision(
                    action = ActionCommand.TapAction(target.first, target.second, label = "RPG Retry Battle"),
                    confidence = 0.92f,
                    strategyNotes = "Party defeated. Re-initiating dungeon combat."
                )
            }

            DetectedGameScene.BATTLE_IN_PROGRESS -> {
                val skillBox = state.interactableElements.firstOrNull { it.label == "SKILL_BUTTON" }
                val attackBox = state.interactableElements.firstOrNull { it.label == "ATTACK_BUTTON" }

                // If boss HP is critical (< 30%), execute full burst macro
                if (state.healthRatio != null && state.healthRatio < 0.30f && skillBox != null) {
                    val skillPt = skillBox.toCenterPoint(screenWidth, screenHeight)
                    val attackPt = attackBox?.toCenterPoint(screenWidth, screenHeight)
                        ?: Pair(screenWidth * 0.8f, screenHeight * 0.8f)

                    AdapterDecision(
                        macro = MacroSequence(
                            id = "rpg_boss_finisher",
                            name = "RPG Boss Finisher",
                            description = "Executes skill then follow-up strikes",
                            actions = listOf(
                                ActionCommand.TapAction(skillPt.first, skillPt.second, label = "Burst Skill"),
                                ActionCommand.WaitAction(150L),
                                ActionCommand.TapAction(attackPt.first, attackPt.second, label = "Finishing Strike")
                            )
                        ),
                        confidence = 0.94f,
                        strategyNotes = "Boss health is critical (<30%). Triggering tactical burst finisher."
                    )
                } else if (skillBox != null) {
                    val pt = skillBox.toCenterPoint(screenWidth, screenHeight)
                    AdapterDecision(
                        action = ActionCommand.TapAction(pt.first, pt.second, label = "Cast RPG Skill"),
                        confidence = 0.88f,
                        strategyNotes = "Skill off cooldown. Casting tactical ability."
                    )
                } else if (attackBox != null) {
                    val pt = attackBox.toCenterPoint(screenWidth, screenHeight)
                    AdapterDecision(
                        action = ActionCommand.TapAction(pt.first, pt.second, label = "RPG Standard Attack"),
                        confidence = 0.82f,
                        strategyNotes = "Standard turn ready. Striking active enemy target."
                    )
                } else {
                    AdapterDecision(
                        action = ActionCommand.TapAction(screenWidth * 0.80f, screenHeight * 0.80f, label = "RPG Battle Tap"),
                        confidence = 0.70f,
                        strategyNotes = "Engaging combat in default action zone."
                    )
                }
            }

            DetectedGameScene.POPUP_DIALOG -> {
                AdapterDecision(
                    action = ActionCommand.TapAction(screenWidth * 0.85f, screenHeight * 0.12f, label = "Skip Narrative"),
                    confidence = 0.80f,
                    strategyNotes = "Dialogue sequence detected. Skipping dialogue to advance quest."
                )
            }

            DetectedGameScene.LOADING_SCREEN -> {
                AdapterDecision(
                    action = ActionCommand.WaitAction(600L, label = "RPG Loading Pause"),
                    confidence = 0.90f,
                    strategyNotes = "Loading dungeon assets. Holding input queue."
                )
            }

            DetectedGameScene.LOBBY_MENU, DetectedGameScene.UNKNOWN -> {
                AdapterDecision(
                    action = ActionCommand.WaitAction(300L, label = "RPG Standby"),
                    confidence = 0.50f,
                    strategyNotes = "Standing by for party command or battle initialization."
                )
            }
        }
    }

    override fun getPresetMacros(): List<MacroSequence> {
        return listOf(
            MacroSequence(
                id = "rpg_skill_combo",
                name = "Tactical Skill Combo",
                description = "Casts Skill 1, Skill 2, and Primary Attack in sequence",
                actions = listOf(
                    ActionCommand.TapAction(350f, 1950f, label = "Skill 1"),
                    ActionCommand.WaitAction(120L),
                    ActionCommand.TapAction(550f, 1950f, label = "Skill 2"),
                    ActionCommand.WaitAction(120L),
                    ActionCommand.TapAction(880f, 1900f, label = "Primary Attack")
                )
            ),
            MacroSequence(
                id = "rpg_ultimate_burst",
                name = "Ultimate Burst Rotation",
                description = "Buffs party then releases ultimate ability",
                actions = listOf(
                    ActionCommand.TapAction(250f, 1850f, label = "Team Buff"),
                    ActionCommand.WaitAction(200L),
                    ActionCommand.TapAction(750f, 1800f, durationMs = 800L, label = "Charged Ultimate")
                )
            )
        )
    }

    private fun VisualBoundingBox.toCenterPoint(width: Int, height: Int): Pair<Float, Float> {
        return Pair((left + right) / 2f * width, (top + bottom) / 2f * height)
    }
}
