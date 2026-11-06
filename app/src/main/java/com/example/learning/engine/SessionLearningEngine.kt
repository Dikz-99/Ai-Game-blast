package com.example.learning.engine

import com.example.data.local.entity.ActionHistory
import com.example.data.local.entity.StrategyMemory
import com.example.data.repository.GameBlastRepository
import com.example.learning.model.LearningUpdateRecord
import com.example.learning.model.StateTransition
import com.example.learning.model.TransitionOutcome
import com.example.learning.pipeline.LearningPipeline

class SessionLearningEngine(
    private val repository: GameBlastRepository
) {

    /**
     * Evaluates a transition between State(t) and State(t+1) after an action is executed.
     * Updates StrategyMemory weights and increments success/failure metrics.
     */
    suspend fun processTransition(
        transition: StateTransition,
        sessionId: Long? = null
    ): LearningUpdateRecord {
        LearningPipeline.recordTransitionEvaluated()

        val outcome = transition.evaluateOutcome()
        val signature = "${transition.beforeScene.name}:${transition.actionExecuted.label}"
        val existingMemory = repository.getMemoryBySignature(transition.profileId, signature)

        val currentWeight = existingMemory?.weight ?: 1.0f
        val weightDelta = outcome.weightScore
        val newWeight = (currentWeight + weightDelta).coerceIn(0.10f, 3.00f)

        val updatedSuccess = (existingMemory?.successCount ?: 0) + (if (outcome == TransitionOutcome.STAGE_CLEARED || outcome == TransitionOutcome.OBJECTIVE_ADVANCED) 1 else 0)
        val updatedFailure = (existingMemory?.failureCount ?: 0) + (if (outcome == TransitionOutcome.DEFEAT) 1 else 0)

        if (existingMemory != null) {
            val updated = existingMemory.copy(
                weight = newWeight,
                successCount = updatedSuccess,
                failureCount = updatedFailure,
                lastUpdated = System.currentTimeMillis()
            )
            repository.updateMemory(updated)
        } else {
            val newMemory = StrategyMemory(
                profileId = transition.profileId,
                stateSignature = signature,
                suggestedAction = transition.actionExecuted.label,
                targetCoordinates = "",
                successCount = updatedSuccess,
                failureCount = updatedFailure,
                weight = newWeight,
                lastUpdated = System.currentTimeMillis()
            )
            repository.saveMemory(newMemory)
        }

        // If active session ID is provided, log to ActionHistory
        if (sessionId != null) {
            val actionEntry = ActionHistory(
                sessionId = sessionId,
                actionType = transition.actionExecuted.label,
                x = 0f,
                y = 0f,
                success = outcome != TransitionOutcome.DEFEAT,
                resultOutcome = outcome.name
            )
            repository.logAction(actionEntry)
        }

        val record = LearningUpdateRecord(
            stateSignature = signature,
            outcome = outcome,
            weightDelta = weightDelta,
            newWeight = newWeight,
            notes = "Evaluation outcome: ${outcome.label}. Weight shifted by $weightDelta."
        )

        LearningPipeline.recordUpdate(record)
        return record
    }
}
