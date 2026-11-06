package com.example.decision.evaluator

import com.example.automation.model.ActionCommand
import com.example.core.model.VisualBoundingBox
import com.example.decision.model.DecisionProposal
import com.example.vision.model.DetectedGameScene
import com.example.vision.model.VisionResult

object PriorityRuleEvaluator {

    /**
     * Evaluates a VisionResult and determines the highest priority automated action.
     */
    fun evaluate(
        vision: VisionResult,
        screenWidth: Int = 1080,
        screenHeight: Int = 2400,
        preferSkills: Boolean = true
    ): DecisionProposal {
        val scene = vision.scene

        return when (scene) {
            DetectedGameScene.VICTORY_REWARDS -> {
                val claimBox = vision.boundingBoxes.firstOrNull { it.label == "CLAIM_BUTTON" }
                val targetPoint = claimBox?.toAbsolutePoint(screenWidth, screenHeight)
                    ?: Pair(screenWidth * 0.5f, screenHeight * 0.78f)

                DecisionProposal(
                    scene = scene,
                    action = ActionCommand.TapAction(
                        x = targetPoint.first,
                        y = targetPoint.second,
                        durationMs = 70L,
                        label = "Claim Reward Tap"
                    ),
                    explanation = "Stage victory achieved. Claiming stage rewards to resume progression.",
                    confidence = vision.confidence,
                    rulePriority = 100
                )
            }

            DetectedGameScene.DEFEAT_RETRY -> {
                val retryBox = vision.boundingBoxes.firstOrNull { it.label == "RETRY_BUTTON" }
                val targetPoint = retryBox?.toAbsolutePoint(screenWidth, screenHeight)
                    ?: Pair(screenWidth * 0.5f, screenHeight * 0.75f)

                DecisionProposal(
                    scene = scene,
                    action = ActionCommand.TapAction(
                        x = targetPoint.first,
                        y = targetPoint.second,
                        durationMs = 75L,
                        label = "Retry Stage Tap"
                    ),
                    explanation = "Defeat screen recognized. Tapping retry to re-enter battle with auto-retry policy.",
                    confidence = vision.confidence,
                    rulePriority = 95
                )
            }

            DetectedGameScene.POPUP_DIALOG -> {
                val dialogBox = vision.boundingBoxes.firstOrNull { it.label == "DIALOG_ACTION" }
                val targetPoint = dialogBox?.toAbsolutePoint(screenWidth, screenHeight)
                    ?: Pair(screenWidth * 0.85f, screenHeight * 0.12f)

                DecisionProposal(
                    scene = scene,
                    action = ActionCommand.TapAction(
                        x = targetPoint.first,
                        y = targetPoint.second,
                        durationMs = 50L,
                        label = "Dismiss Dialog Tap"
                    ),
                    explanation = "System dialog or story prompt detected. Advancing or dismissing modal.",
                    confidence = vision.confidence,
                    rulePriority = 80
                )
            }

            DetectedGameScene.BATTLE_IN_PROGRESS -> {
                val skillBox = vision.boundingBoxes.firstOrNull { it.label == "SKILL_BUTTON" }
                val attackBox = vision.boundingBoxes.firstOrNull { it.label == "ATTACK_BUTTON" }

                if (preferSkills && skillBox != null) {
                    val pt = skillBox.toAbsolutePoint(screenWidth, screenHeight)
                    DecisionProposal(
                        scene = scene,
                        action = ActionCommand.TapAction(
                            x = pt.first,
                            y = pt.second,
                            durationMs = 65L,
                            label = "Cast Tactical Skill"
                        ),
                        explanation = "High priority: Tactical skill is off cooldown. Executing burst damage.",
                        confidence = skillBox.confidence,
                        rulePriority = 90
                    )
                } else if (attackBox != null) {
                    val pt = attackBox.toAbsolutePoint(screenWidth, screenHeight)
                    DecisionProposal(
                        scene = scene,
                        action = ActionCommand.TapAction(
                            x = pt.first,
                            y = pt.second,
                            durationMs = 60L,
                            label = "Primary Attack Tap"
                        ),
                        explanation = "Active battle ongoing. Executing primary combat strike on target.",
                        confidence = attackBox.confidence,
                        rulePriority = 75
                    )
                } else {
                    // Fallback attack tap in bottom right standard battle zone
                    DecisionProposal(
                        scene = scene,
                        action = ActionCommand.TapAction(
                            x = screenWidth * 0.82f,
                            y = screenHeight * 0.82f,
                            durationMs = 60L,
                            label = "Tactical Attack Tap"
                        ),
                        explanation = "Battle active with general layout. Striking standard combat interface coordinates.",
                        confidence = 0.70f,
                        rulePriority = 70
                    )
                }
            }

            DetectedGameScene.LOADING_SCREEN -> {
                DecisionProposal(
                    scene = scene,
                    action = ActionCommand.WaitAction(
                        durationMs = 600L,
                        label = "Wait for Loading Screen"
                    ),
                    explanation = "Game asset / stage loading detected. Suspending input to preserve synchronicity.",
                    confidence = 0.90f,
                    rulePriority = 10
                )
            }

            DetectedGameScene.LOBBY_MENU -> {
                DecisionProposal(
                    scene = scene,
                    action = ActionCommand.WaitAction(
                        durationMs = 400L,
                        label = "Lobby Standby"
                    ),
                    explanation = "Main lobby menu active. Standing by for user stage selection or auto-start routine.",
                    confidence = 0.85f,
                    rulePriority = 15
                )
            }

            DetectedGameScene.UNKNOWN -> {
                DecisionProposal(
                    scene = scene,
                    action = ActionCommand.WaitAction(
                        durationMs = 300L,
                        label = "Idle Wait"
                    ),
                    explanation = "Scene status is ambiguous. Observing next frame update before dispatch.",
                    confidence = 0.50f,
                    rulePriority = 5
                )
            }
        }
    }

    private fun VisualBoundingBox.toAbsolutePoint(width: Int, height: Int): Pair<Float, Float> {
        val centerX = (left + right) / 2f * width
        val centerY = (top + bottom) / 2f * height
        return Pair(centerX, centerY)
    }
}
