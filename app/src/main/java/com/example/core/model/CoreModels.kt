package com.example.core.model

enum class GameState {
    IDLE,
    SCANNING,
    ANALYZING,
    DECIDING,
    EXECUTING,
    PAUSED,
    EMERGENCY_STOPPED,
    ERROR
}

enum class GameGenre(val displayName: String, val iconName: String) {
    TURN_BASED_RPG("Turn-Based RPG", "Security"),
    IDLE_CLICKER("Idle / Clicker", "TouchApp"),
    MATCH_3_PUZZLE("Match-3 Puzzle", "Extension"),
    ACTION_ARCADE("Action / Arcade", "Gamepad"),
    STRATEGY("Strategy / Tower Defense", "Castle"),
    CUSTOM("Custom Profile", "Tune")
}

enum class ActionType {
    TAP,
    DOUBLE_TAP,
    LONG_PRESS,
    SWIPE,
    WAIT
}

data class VisualBoundingBox(
    val id: String,
    val label: String,
    val confidence: Float,
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float,
    val detectedText: String? = null
)

data class TelemetryData(
    val fps: Double = 0.0,
    val totalTaps: Int = 0,
    val successActions: Int = 0,
    val failedActions: Int = 0,
    val currentLatencyMs: Long = 0,
    val lastActionTimestamp: Long = 0L,
    val isSafetyInterventionActive: Boolean = false,
    val lastState: GameState = GameState.IDLE
)
