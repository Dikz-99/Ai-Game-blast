package com.example.decision.engine

import com.example.adapter.base.IGameAdapter
import com.example.adapter.registry.AdapterRegistry
import com.example.automation.queue.ActionQueueManager
import com.example.core.safety.SafetyController
import com.example.data.local.entity.GameProfile
import com.example.decision.evaluator.PriorityRuleEvaluator
import com.example.decision.model.DecisionProposal
import com.example.decision.model.DecisionState
import com.example.decision.pipeline.DecisionPipeline
import com.example.vision.model.VisionResult
import com.example.vision.pipeline.VisionPipeline
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

object GameDecisionEngine {

    private val engineScope = CoroutineScope(Dispatchers.Default)
    private var loopJob: Job? = null
    private var screenWidth: Int = 1080
    private var screenHeight: Int = 2400
    private var activeGameProfile: GameProfile? = null

    fun updateScreenDimensions(width: Int, height: Int) {
        if (width > 0 && height > 0) {
            screenWidth = width
            screenHeight = height
        }
    }

    fun setActiveProfile(profile: GameProfile?) {
        activeGameProfile = profile
        val adapter = AdapterRegistry.getAdapterForProfile(profile)
        DecisionPipeline.addLog("[ADAPTER LINKED] Switched to ${adapter.adapterName}")
    }

    fun getActiveAdapter(): IGameAdapter {
        return AdapterRegistry.getAdapterForProfile(activeGameProfile)
    }

    /**
     * Starts continuous auto-pilot reasoning loop.
     */
    fun startAutoPilot(decisionIntervalMs: Long = 750L) {
        if (loopJob?.isActive == true) return

        DecisionPipeline.updateAutoPilot(true)

        loopJob = engineScope.launch {
            VisionPipeline.latestResult.collectLatest { vision ->
                if (!DecisionPipeline.isAutoPilotActive.value) return@collectLatest

                if (SafetyController.isEmergencyStopped.value) {
                    DecisionPipeline.updateState(DecisionState.EMERGENCY_HALTED)
                    DecisionPipeline.addLog("[SAFETY VIOLATION] Emergency kill-switch active. Decision loop halted.")
                    stopAutoPilot()
                    return@collectLatest
                }

                if (vision != null) {
                    executeDecisionCycle(vision)
                }

                delay(decisionIntervalMs)
            }
        }
    }

    /**
     * Stops continuous auto-pilot reasoning loop.
     */
    fun stopAutoPilot() {
        loopJob?.cancel()
        loopJob = null
        DecisionPipeline.updateAutoPilot(false)
    }

    /**
     * Executes a single evaluation step on demand (e.g. from UI step button or unit test).
     */
    suspend fun evaluateSingleStep(vision: VisionResult? = VisionPipeline.latestResult.value): DecisionProposal? {
        val targetVision = vision ?: return null

        if (SafetyController.isEmergencyStopped.value) {
            DecisionPipeline.updateState(DecisionState.EMERGENCY_HALTED)
            DecisionPipeline.addLog("[SAFETY] Single-step rejected: Emergency Stop is active.")
            return null
        }

        return executeDecisionCycle(targetVision)
    }

    private suspend fun executeDecisionCycle(vision: VisionResult): DecisionProposal {
        DecisionPipeline.updateState(DecisionState.EVALUATING)

        val profile = activeGameProfile
        val proposal: DecisionProposal = if (profile != null) {
            val adapter = AdapterRegistry.getAdapterForProfile(profile)
            val extractedState = adapter.extractState(vision)
            val adapterDecision = adapter.decideAction(extractedState, screenWidth, screenHeight)

            DecisionProposal(
                scene = vision.scene,
                action = adapterDecision.action,
                macro = adapterDecision.macro,
                explanation = "[${adapter.adapterName}] ${adapterDecision.strategyNotes}",
                confidence = adapterDecision.confidence,
                rulePriority = 85
            )
        } else {
            PriorityRuleEvaluator.evaluate(
                vision = vision,
                screenWidth = screenWidth,
                screenHeight = screenHeight,
                preferSkills = true
            )
        }

        DecisionPipeline.recordProposal(proposal)

        // If an action or macro is recommended, dispatch it through safety & queue
        val action = proposal.action
        val macro = proposal.macro

        if (action != null) {
            DecisionPipeline.updateState(DecisionState.DISPATCHING)
            ActionQueueManager.enqueue(action)
        } else if (macro != null) {
            DecisionPipeline.updateState(DecisionState.DISPATCHING)
            ActionQueueManager.executeMacro(macro)
        }

        DecisionPipeline.updateState(DecisionState.COOLDOWN)
        return proposal
    }
}
