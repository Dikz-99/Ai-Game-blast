package com.example.adapter.base

import com.example.adapter.model.AdapterDecision
import com.example.adapter.model.AdapterGameState
import com.example.automation.model.MacroSequence
import com.example.core.model.GameGenre
import com.example.vision.model.VisionResult

interface IGameAdapter {
    val genre: GameGenre
    val adapterName: String
    val description: String

    /**
     * Extracts genre-specific game telemetry from universal vision results.
     */
    fun extractState(vision: VisionResult): AdapterGameState

    /**
     * Generates a genre-specific tactical decision based on extracted state.
     */
    fun decideAction(
        state: AdapterGameState,
        screenWidth: Int,
        screenHeight: Int
    ): AdapterDecision

    /**
     * Provides genre-tailored macro routines (e.g. Skill combo, gem swap, turbo taps).
     */
    fun getPresetMacros(): List<MacroSequence>
}
