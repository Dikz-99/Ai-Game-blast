package com.example.vision.model

import com.example.core.model.VisualBoundingBox

enum class DetectedGameScene(val displayName: String) {
    LOBBY_MENU("Lobby / Main Menu"),
    BATTLE_IN_PROGRESS("Combat / In Battle"),
    VICTORY_REWARDS("Victory / Claim Rewards"),
    DEFEAT_RETRY("Defeat / Retry Screen"),
    POPUP_DIALOG("System / Event Popup"),
    LOADING_SCREEN("Game Loading Screen"),
    UNKNOWN("Analyzing Scene...")
}

data class RecognizedTextSnippet(
    val text: String,
    val confidence: Float,
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float
) {
    val centerX: Float get() = (left + right) / 2f
    val centerY: Float get() = (top + bottom) / 2f
}

data class VisionResult(
    val scene: DetectedGameScene = DetectedGameScene.UNKNOWN,
    val confidence: Float = 0.0f,
    val boundingBoxes: List<VisualBoundingBox> = emptyList(),
    val rawTextDump: String = "",
    val textSnippets: List<RecognizedTextSnippet> = emptyList(),
    val healthPercentage: Float? = null,
    val hasInteractableButton: Boolean = false,
    val primaryActionTarget: VisualBoundingBox? = null,
    val processingTimeMs: Long = 0L,
    val timestamp: Long = System.currentTimeMillis()
)
