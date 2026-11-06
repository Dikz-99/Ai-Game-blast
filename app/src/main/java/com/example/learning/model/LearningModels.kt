package com.example.learning.model

import com.example.automation.model.ActionCommand
import com.example.vision.model.DetectedGameScene

enum class TransitionOutcome(val label: String, val weightScore: Float) {
    STAGE_CLEARED("Stage Cleared", 0.20f),
    OBJECTIVE_ADVANCED("Objective Advanced", 0.08f),
    NEUTRAL("Neutral Transition", 0.00f),
    DAMAGE_SUSTAINED("Damage Sustained", -0.05f),
    DEFEAT("Defeat Encountered", -0.25f)
}

data class StateTransition(
    val profileId: Long,
    val beforeScene: DetectedGameScene,
    val actionExecuted: ActionCommand,
    val afterScene: DetectedGameScene,
    val beforeHealth: Float? = null,
    val afterHealth: Float? = null,
    val timestamp: Long = System.currentTimeMillis()
) {
    fun evaluateOutcome(): TransitionOutcome {
        return when {
            afterScene == DetectedGameScene.VICTORY_REWARDS -> TransitionOutcome.STAGE_CLEARED
            afterScene == DetectedGameScene.DEFEAT_RETRY -> TransitionOutcome.DEFEAT
            beforeHealth != null && afterHealth != null && afterHealth < beforeHealth -> TransitionOutcome.OBJECTIVE_ADVANCED
            else -> TransitionOutcome.NEUTRAL
        }
    }
}

data class LearningUpdateRecord(
    val stateSignature: String,
    val outcome: TransitionOutcome,
    val weightDelta: Float,
    val newWeight: Float,
    val notes: String
)
