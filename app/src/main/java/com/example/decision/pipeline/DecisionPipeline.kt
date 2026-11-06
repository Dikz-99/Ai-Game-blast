package com.example.decision.pipeline

import com.example.automation.model.ActionExecutionResult
import com.example.decision.model.DecisionProposal
import com.example.decision.model.DecisionState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DecisionPipeline {

    private val _isAutoPilotActive = MutableStateFlow(false)
    val isAutoPilotActive: StateFlow<Boolean> = _isAutoPilotActive.asStateFlow()

    private val _decisionState = MutableStateFlow(DecisionState.IDLE)
    val decisionState: StateFlow<DecisionState> = _decisionState.asStateFlow()

    private val _latestProposal = MutableStateFlow<DecisionProposal?>(null)
    val latestProposal: StateFlow<DecisionProposal?> = _latestProposal.asStateFlow()

    private val _totalDecisions = MutableStateFlow(0L)
    val totalDecisions: StateFlow<Long> = _totalDecisions.asStateFlow()

    private val _successfulActions = MutableStateFlow(0L)
    val successfulActions: StateFlow<Long> = _successfulActions.asStateFlow()

    private val _recentDecisionLogs = MutableStateFlow<List<String>>(
        listOf("[DECISION CORE] Priority rule engine initialized and online.")
    )
    val recentDecisionLogs: StateFlow<List<String>> = _recentDecisionLogs.asStateFlow()

    private val _lastExecutionResult = MutableStateFlow<ActionExecutionResult?>(null)
    val lastExecutionResult: StateFlow<ActionExecutionResult?> = _lastExecutionResult.asStateFlow()

    private val dateFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())

    fun updateAutoPilot(active: Boolean) {
        _isAutoPilotActive.value = active
        if (!active && _decisionState.value != DecisionState.EMERGENCY_HALTED) {
            _decisionState.value = DecisionState.IDLE
        }
        addLog(if (active) "[AUTOPILOT ENGAGED] AI Decision Loop running." else "[AUTOPILOT DISENGAGED] AI Decision Loop standby.")
    }

    fun updateState(state: DecisionState) {
        _decisionState.value = state
    }

    fun recordProposal(proposal: DecisionProposal) {
        _latestProposal.value = proposal
        _totalDecisions.value += 1
        addLog("[EVAL] [${proposal.scene.name}] -> ${proposal.explanation} (Pri: ${proposal.rulePriority})")
    }

    fun recordExecution(result: ActionExecutionResult) {
        _lastExecutionResult.value = result
        if (result.success) {
            _successfulActions.value += 1
            addLog("[DISPATCH OK] ${result.action.label} executed: ${result.message}")
        } else {
            addLog("[DISPATCH BLOCKED] ${result.action.label}: ${result.message}")
        }
    }

    fun addLog(message: String) {
        val timestamp = dateFormat.format(Date())
        val entry = "[$timestamp] $message"
        val updated = (_recentDecisionLogs.value + entry).takeLast(60)
        _recentDecisionLogs.value = updated
    }

    fun reset() {
        _isAutoPilotActive.value = false
        _decisionState.value = DecisionState.IDLE
        _latestProposal.value = null
        _totalDecisions.value = 0L
        _successfulActions.value = 0L
        _recentDecisionLogs.value = listOf("[DECISION CORE] System reset to clean state.")
        _lastExecutionResult.value = null
    }
}
