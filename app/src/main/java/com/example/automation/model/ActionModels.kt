package com.example.automation.model

sealed class ActionCommand {
    abstract val label: String

    data class TapAction(
        val x: Float,
        val y: Float,
        val durationMs: Long = 65L,
        val enableJitter: Boolean = true,
        override val label: String = "Tap"
    ) : ActionCommand()

    data class DoubleTapAction(
        val x: Float,
        val y: Float,
        val intervalMs: Long = 120L,
        override val label: String = "Double Tap"
    ) : ActionCommand()

    data class LongPressAction(
        val x: Float,
        val y: Float,
        val durationMs: Long = 650L,
        override val label: String = "Long Press"
    ) : ActionCommand()

    data class SwipeAction(
        val startX: Float,
        val startY: Float,
        val endX: Float,
        val endY: Float,
        val durationMs: Long = 320L,
        val curveDeviation: Float = 0.04f,
        override val label: String = "Swipe"
    ) : ActionCommand()

    data class WaitAction(
        val durationMs: Long,
        override val label: String = "Wait"
    ) : ActionCommand()
}

data class MacroSequence(
    val id: String,
    val name: String,
    val description: String,
    val actions: List<ActionCommand>
)

data class ActionExecutionResult(
    val action: ActionCommand,
    val success: Boolean,
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)
