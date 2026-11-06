package com.example.automation.queue

import com.example.automation.model.ActionCommand
import com.example.automation.model.ActionExecutionResult
import com.example.automation.model.MacroSequence
import com.example.automation.pipeline.ActionPipeline
import com.example.automation.service.GameAutomationAccessibilityService
import com.example.core.safety.SafetyController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentLinkedQueue

object ActionQueueManager {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val commandQueue = ConcurrentLinkedQueue<ActionCommand>()
    private var isProcessing = false

    fun enqueue(command: ActionCommand) {
        if (SafetyController.isEmergencyStopped.value) {
            ActionPipeline.recordExecution(
                ActionExecutionResult(command, false, "Emergency stop engaged: command rejected")
            )
            return
        }
        commandQueue.add(command)
        processNext()
    }

    fun executeMacro(macro: MacroSequence) {
        if (SafetyController.isEmergencyStopped.value) {
            ActionPipeline.addLog("[MACRO BLOCKED] Emergency stop is currently active")
            return
        }

        scope.launch {
            ActionPipeline.updateMacroState(isExecuting = true, current = 0, total = macro.actions.size)
            var currentStep = 0

            for (action in macro.actions) {
                if (SafetyController.isEmergencyStopped.value) {
                    ActionPipeline.addLog("[MACRO ABORTED] Sequence interrupted by Emergency Kill Switch")
                    ActionPipeline.updateMacroState(isExecuting = false, current = currentStep, total = macro.actions.size)
                    return@launch
                }

                currentStep++
                ActionPipeline.updateMacroState(isExecuting = true, current = currentStep, total = macro.actions.size)

                when (action) {
                    is ActionCommand.WaitAction -> {
                        delay(action.durationMs)
                    }
                    else -> {
                        executeSingleCommand(action)
                        // Safety pause between macro steps
                        delay(180L)
                    }
                }
            }

            ActionPipeline.updateMacroState(isExecuting = false, current = macro.actions.size, total = macro.actions.size)
        }
    }

    private fun processNext() {
        if (isProcessing) return
        val next = commandQueue.poll() ?: return

        isProcessing = true
        scope.launch {
            try {
                if (SafetyController.isEmergencyStopped.value) {
                    commandQueue.clear()
                    ActionPipeline.recordExecution(
                        ActionExecutionResult(next, false, "Queue purged by emergency stop")
                    )
                    return@launch
                }

                executeSingleCommand(next)
            } finally {
                isProcessing = false
                if (commandQueue.isNotEmpty()) {
                    processNext()
                }
            }
        }
    }

    private suspend fun executeSingleCommand(command: ActionCommand) {
        val service = GameAutomationAccessibilityService.instance
        if (service == null) {
            ActionPipeline.recordExecution(
                ActionExecutionResult(
                    action = command,
                    success = false,
                    message = "Accessibility Service is not connected / enabled by user"
                )
            )
            return
        }

        when (command) {
            is ActionCommand.TapAction -> {
                service.performTap(
                    x = command.x,
                    y = command.y,
                    enableJitter = command.enableJitter
                ) { success, msg ->
                    ActionPipeline.recordExecution(ActionExecutionResult(command, success, msg))
                }
            }
            is ActionCommand.DoubleTapAction -> {
                service.performDoubleTap(
                    x = command.x,
                    y = command.y,
                    intervalMs = command.intervalMs
                ) { success, msg ->
                    ActionPipeline.recordExecution(ActionExecutionResult(command, success, msg))
                }
            }
            is ActionCommand.LongPressAction -> {
                service.performLongPress(
                    x = command.x,
                    y = command.y,
                    durationMs = command.durationMs
                ) { success, msg ->
                    ActionPipeline.recordExecution(ActionExecutionResult(command, success, msg))
                }
            }
            is ActionCommand.SwipeAction -> {
                service.performCurvedSwipe(
                    startX = command.startX,
                    startY = command.startY,
                    endX = command.endX,
                    endY = command.endY,
                    durationMs = command.durationMs,
                    curveDeviation = command.curveDeviation
                ) { success, msg ->
                    ActionPipeline.recordExecution(ActionExecutionResult(command, success, msg))
                }
            }
            is ActionCommand.WaitAction -> {
                delay(command.durationMs)
                ActionPipeline.recordExecution(
                    ActionExecutionResult(command, true, "Waited ${command.durationMs}ms")
                )
            }
        }
    }

    fun clearQueue() {
        commandQueue.clear()
    }
}
