package com.example.adapter.model

import com.example.automation.model.ActionCommand
import com.example.automation.model.MacroSequence
import com.example.core.model.GameGenre
import com.example.core.model.VisualBoundingBox
import com.example.vision.model.DetectedGameScene

/**
 * Normalized genre-specific game state parsed from Vision inputs.
 */
data class AdapterGameState(
    val genre: GameGenre,
    val scene: DetectedGameScene,
    val healthRatio: Float? = null,
    val availableSkills: List<String> = emptyList(),
    val interactableElements: List<VisualBoundingBox> = emptyList(),
    val extractedKeywords: List<String> = emptyList(),
    val isActionReady: Boolean = true,
    val metrics: Map<String, String> = emptyMap()
)

/**
 * Decision returned by an IGameAdapter for the current frame.
 */
data class AdapterDecision(
    val action: ActionCommand? = null,
    val macro: MacroSequence? = null,
    val confidence: Float = 0.85f,
    val strategyNotes: String = ""
)
