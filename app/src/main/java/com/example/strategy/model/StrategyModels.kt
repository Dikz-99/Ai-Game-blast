package com.example.strategy.model

import com.example.automation.model.ActionCommand
import com.example.core.model.GameGenre
import com.example.data.local.entity.GameObservation
import com.example.data.local.entity.GameProfile
import com.example.vision.model.VisionResult

enum class StrategySource(val label: String) {
    LOCAL_HEURISTIC("Local On-Device AI"),
    CLOUD_GEMINI("Cloud Gemini 3.8 Flash"),
    CACHED_MEMORY("Historical Strategy Memory")
}

data class StrategyRequest(
    val gameProfile: GameProfile,
    val currentVision: VisionResult,
    val recentObservations: List<GameObservation> = emptyList(),
    val activeGenre: GameGenre = GameGenre.TURN_BASED_RPG,
    val userGoal: String = "Maximize combat score and clear stage efficiently"
)

data class StrategyRecommendation(
    val title: String,
    val analysis: String,
    val suggestedAction: ActionCommand? = null,
    val tacticalTips: List<String> = emptyList(),
    val suggestedMacroName: String? = null,
    val source: StrategySource = StrategySource.LOCAL_HEURISTIC,
    val confidence: Float = 0.85f,
    val timestamp: Long = System.currentTimeMillis()
)
