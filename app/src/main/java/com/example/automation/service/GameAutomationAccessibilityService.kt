package com.example.automation.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.os.Build
import android.view.accessibility.AccessibilityEvent
import com.example.automation.path.HumanizedPathGenerator
import com.example.core.safety.SafetyController
import com.example.core.safety.SafetyValidationResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Random

class GameAutomationAccessibilityService : AccessibilityService() {

    private val random = Random()
    private val serviceScope = CoroutineScope(Dispatchers.Default)

    companion object {
        @Volatile
        var instance: GameAutomationAccessibilityService? = null
            private set
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Observes window state changes if needed
    }

    override fun onInterrupt() {
        SafetyController.triggerEmergencyStop("Accessibility service was interrupted")
    }

    override fun onDestroy() {
        super.onDestroy()
        if (instance == this) {
            instance = null
        }
    }

    /**
     * Dispatches a tap gesture with safety verification and micro-jitter.
     */
    fun performTap(
        x: Float,
        y: Float,
        maxActionsPerSec: Int = 4,
        minIntervalMs: Long = 250L,
        enableJitter: Boolean = true,
        onComplete: (Boolean, String) -> Unit = { _, _ -> }
    ) {
        val validation = SafetyController.validateAction(
            x,
            y,
            maxActionsPerSec,
            minIntervalMs
        )

        if (validation is SafetyValidationResult.Rejected) {
            onComplete(false, validation.reason)
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val path = HumanizedPathGenerator.createTapPath(x, y, enableJitter)
            // Tap duration between 45ms to 85ms
            val duration = 45L + random.nextInt(40)
            val stroke = GestureDescription.StrokeDescription(path, 0, duration)
            val gesture = GestureDescription.Builder().addStroke(stroke).build()

            dispatchGesture(gesture, object : GestureResultCallback() {
                override fun onCompleted(gestureDescription: GestureDescription?) {
                    super.onCompleted(gestureDescription)
                    onComplete(true, "Tap completed at (${x.toInt()}, ${y.toInt()})")
                }

                override fun onCancelled(gestureDescription: GestureDescription?) {
                    super.onCancelled(gestureDescription)
                    onComplete(false, "Tap cancelled by system")
                }
            }, null)
        } else {
            onComplete(false, "Android Nougat (API 24+) required for gesture dispatch")
        }
    }

    /**
     * Dispatches a double tap gesture.
     */
    fun performDoubleTap(
        x: Float,
        y: Float,
        intervalMs: Long = 120L,
        maxActionsPerSec: Int = 5,
        minIntervalMs: Long = 100L,
        onComplete: (Boolean, String) -> Unit = { _, _ -> }
    ) {
        performTap(x, y, maxActionsPerSec, minIntervalMs) { firstSuccess, firstMsg ->
            if (!firstSuccess) {
                onComplete(false, firstMsg)
                return@performTap
            }
            serviceScope.launch {
                delay(intervalMs)
                performTap(x, y, maxActionsPerSec, minIntervalMs) { secondSuccess, secondMsg ->
                    onComplete(secondSuccess, if (secondSuccess) "Double tap completed" else secondMsg)
                }
            }
        }
    }

    /**
     * Dispatches a long press gesture.
     */
    fun performLongPress(
        x: Float,
        y: Float,
        durationMs: Long = 650L,
        maxActionsPerSec: Int = 3,
        minIntervalMs: Long = 300L,
        onComplete: (Boolean, String) -> Unit = { _, _ -> }
    ) {
        val validation = SafetyController.validateAction(
            x,
            y,
            maxActionsPerSec,
            minIntervalMs
        )

        if (validation is SafetyValidationResult.Rejected) {
            onComplete(false, validation.reason)
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val path = HumanizedPathGenerator.createTapPath(x, y, enableJitter = true)
            val stroke = GestureDescription.StrokeDescription(path, 0, durationMs)
            val gesture = GestureDescription.Builder().addStroke(stroke).build()

            dispatchGesture(gesture, object : GestureResultCallback() {
                override fun onCompleted(gestureDescription: GestureDescription?) {
                    super.onCompleted(gestureDescription)
                    onComplete(true, "Long press completed (${durationMs}ms)")
                }

                override fun onCancelled(gestureDescription: GestureDescription?) {
                    super.onCancelled(gestureDescription)
                    onComplete(false, "Long press cancelled by system")
                }
            }, null)
        } else {
            onComplete(false, "Android Nougat required")
        }
    }

    /**
     * Dispatches a curved humanized swipe gesture.
     */
    fun performCurvedSwipe(
        startX: Float,
        startY: Float,
        endX: Float,
        endY: Float,
        durationMs: Long = 320L,
        curveDeviation: Float = 0.05f,
        maxActionsPerSec: Int = 3,
        minIntervalMs: Long = 350L,
        onComplete: (Boolean, String) -> Unit = { _, _ -> }
    ) {
        val validation = SafetyController.validateAction(
            startX,
            startY,
            maxActionsPerSec,
            minIntervalMs
        )

        if (validation is SafetyValidationResult.Rejected) {
            onComplete(false, validation.reason)
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val path = HumanizedPathGenerator.createCurvedSwipePath(
                startX = startX,
                startY = startY,
                endX = endX,
                endY = endY,
                curveDeviation = curveDeviation
            )
            val stroke = GestureDescription.StrokeDescription(path, 0, durationMs)
            val gesture = GestureDescription.Builder().addStroke(stroke).build()

            dispatchGesture(gesture, object : GestureResultCallback() {
                override fun onCompleted(gestureDescription: GestureDescription?) {
                    super.onCompleted(gestureDescription)
                    onComplete(true, "Curved swipe dispatched successfully")
                }

                override fun onCancelled(gestureDescription: GestureDescription?) {
                    super.onCancelled(gestureDescription)
                    onComplete(false, "Swipe cancelled by system")
                }
            }, null)
        } else {
            onComplete(false, "Android Nougat required")
        }
    }
}
