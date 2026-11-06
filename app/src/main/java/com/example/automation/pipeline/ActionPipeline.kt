package com.example.automation.pipeline

import com.example.automation.model.ActionCommand
import com.example.automation.model.ActionExecutionResult
import com.example.automation.model.MacroSequence
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object ActionPipeline {

    private val _lastExecutedAction = MutableStateFlow<ActionExecutionResult?>(null)
    val lastExecutedAction: StateFlow<ActionExecutionResult?> = _lastExecutedAction.asStateFlow()

    private val _isExecutingMacro = MutableStateFlow(false)
    val isExecutingMacro: StateFlow<Boolean> = _isExecutingMacro.asStateFlow()

    private val _macroProgress = MutableStateFlow(Pair(0, 0)) // current, total
    val macroProgress: StateFlow<Pair<Int, Int>> = _macroProgress.asStateFlow()

    private val _recentActionLogs = MutableStateFlow<List<String>>(
        listOf(
            "[AUTOMATION ENGINE] Gesture dispatcher loaded",
            "[PATH GENERATOR] Cubic Bézier humanized curve model ready",
            "[QUEUE MANAGER] Rate-limiter synchronized with SafetyController"
        )
    )
    val recentActionLogs: StateFlow<List<String>> = _recentActionLogs.asStateFlow()

    val defaultPresetMacros: List<MacroSequence> = listOf(
        MacroSequence(
            id = "macro_rpg_combo",
            name = "Tactical Skill Combo",
            description = "Selects enemy, activates primary skill, and confirms action",
            actions = listOf(
                ActionCommand.TapAction(x = 540f, y = 900f, label = "Target Lock"),
                ActionCommand.WaitAction(durationMs = 200L, label = "Pacing Cooldown"),
                ActionCommand.TapAction(x = 350f, y = 1850f, label = "Skill 1 Select"),
                ActionCommand.WaitAction(durationMs = 250L, label = "Pacing Cooldown"),
                ActionCommand.TapAction(x = 750f, y = 1850f, label = "Skill Confirm")
            )
        ),
        MacroSequence(
            id = "macro_idle_burst",
            name = "Turbo Tap Burst (3x)",
            description = "Executes rapid taps with randomized human micro-jitter",
            actions = listOf(
                ActionCommand.TapAction(x = 540f, y = 1400f, durationMs = 50L, label = "Turbo Tap 1"),
                ActionCommand.WaitAction(durationMs = 220L, label = "Jitter Cooldown"),
                ActionCommand.TapAction(x = 540f, y = 1400f, durationMs = 50L, label = "Turbo Tap 2"),
                ActionCommand.WaitAction(durationMs = 220L, label = "Jitter Cooldown"),
                ActionCommand.TapAction(x = 540f, y = 1400f, durationMs = 50L, label = "Turbo Tap 3")
            )
        ),
        MacroSequence(
            id = "macro_puzzle_swap",
            name = "Gem Horizontal Swap",
            description = "Curved swipe gesture to swap two puzzle gems",
            actions = listOf(
                ActionCommand.SwipeAction(
                    startX = 360f,
                    startY = 1300f,
                    endX = 540f,
                    endY = 1300f,
                    durationMs = 280L,
                    label = "Gem Slide Right"
                )
            )
        )
    )

    fun recordExecution(result: ActionExecutionResult) {
        _lastExecutedAction.value = result

        val statusText = if (result.success) "SUCCESS" else "BLOCKED"
        val log = "[GESTURE $statusText] ${result.action.label} -> ${result.message}"
        addLog(log)
    }

    fun updateMacroState(isExecuting: Boolean, current: Int = 0, total: Int = 0) {
        _isExecutingMacro.value = isExecuting
        _macroProgress.value = Pair(current, total)
        if (isExecuting) {
            addLog("[MACRO] Progress: step $current of $total")
        } else if (total > 0 && current == total) {
            addLog("[MACRO] Sequence execution completed successfully")
        }
    }

    fun addLog(log: String) {
        val current = _recentActionLogs.value.toMutableList()
        current.add(0, log)
        if (current.size > 30) {
            current.removeAt(current.size - 1)
        }
        _recentActionLogs.value = current
    }
}
