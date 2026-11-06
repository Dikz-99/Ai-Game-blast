package com.example.core.safety

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.ConcurrentLinkedQueue

data class ScreenBounds(val left: Float, val top: Float, val right: Float, val bottom: Float) {
    fun contains(x: Float, y: Float): Boolean = x in left..right && y in top..bottom
}

object SafetyController {

    private val _isEmergencyStopped = MutableStateFlow(false)
    val isEmergencyStopped: StateFlow<Boolean> = _isEmergencyStopped.asStateFlow()

    private val _safetyInterventionCount = MutableStateFlow(0)
    val safetyInterventionCount: StateFlow<Int> = _safetyInterventionCount.asStateFlow()

    private val actionTimestamps = ConcurrentLinkedQueue<Long>()
    private var lastActionTime: Long = 0L

    // Screen dimensions updated dynamically by Activity or WindowManager
    var screenWidth: Int = 1080
    var screenHeight: Int = 2400

    // Safe boundaries: exclude status bar (top ~120px) and navigation bar (bottom ~160px)
    var safeArea: ScreenBounds = ScreenBounds(0f, 120f, 1080f, 2240f)

    fun triggerEmergencyStop(reason: String = "User requested emergency stop") {
        _isEmergencyStopped.value = true
        _safetyInterventionCount.value += 1
    }

    fun resetEmergencyStop() {
        _isEmergencyStopped.value = false
        actionTimestamps.clear()
        lastActionTime = 0L
    }

    /**
     * Validates whether an action can safely proceed.
     * Returns true if allowed, false if rejected by safety rules.
     */
    fun validateAction(
        x: Float,
        y: Float,
        maxActionsPerSecond: Int,
        minIntervalMs: Long
    ): SafetyValidationResult {
        if (_isEmergencyStopped.value) {
            return SafetyValidationResult.Rejected("Emergency stop is currently engaged")
        }

        val now = System.currentTimeMillis()

        // 1. Boundary check
        if (!safeArea.contains(x, y)) {
            _safetyInterventionCount.value += 1
            return SafetyValidationResult.Rejected(
                "Coordinate ($x, $y) is outside safe boundary ($safeArea)"
            )
        }

        // 2. Minimum interval check
        if (now - lastActionTime < minIntervalMs) {
            _safetyInterventionCount.value += 1
            return SafetyValidationResult.Rejected(
                "Action throttled: interval too fast (${now - lastActionTime}ms < ${minIntervalMs}ms)"
            )
        }

        // 3. Sliding window rate limiter
        while (actionTimestamps.isNotEmpty() && (now - (actionTimestamps.peek() ?: 0L)) > 1000L) {
            actionTimestamps.poll()
        }

        if (actionTimestamps.size >= maxActionsPerSecond) {
            _safetyInterventionCount.value += 1
            return SafetyValidationResult.Rejected(
                "Rate limit exceeded: ${actionTimestamps.size} actions in the last 1000ms (max: $maxActionsPerSecond)"
            )
        }

        // Action approved
        actionTimestamps.add(now)
        lastActionTime = now
        return SafetyValidationResult.Approved
    }

    fun updateScreenDimensions(width: Int, height: Int) {
        screenWidth = width
        screenHeight = height
        val topMargin = (height * 0.04f)
        val bottomMargin = height - (height * 0.06f)
        safeArea = ScreenBounds(0f, topMargin, width.toFloat(), bottomMargin)
    }
}

sealed class SafetyValidationResult {
    data object Approved : SafetyValidationResult()
    data class Rejected(val reason: String) : SafetyValidationResult()
}
