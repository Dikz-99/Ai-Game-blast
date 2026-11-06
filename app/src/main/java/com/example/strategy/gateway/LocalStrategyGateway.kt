package com.example.strategy.gateway

import com.example.core.model.GameGenre
import com.example.strategy.model.StrategyRecommendation
import com.example.strategy.model.StrategyRequest
import com.example.strategy.model.StrategySource
import com.example.vision.model.DetectedGameScene

class LocalStrategyGateway : IStrategyGateway {

    override val gatewayName: String = "Local Neural Heuristics Gateway"

    override suspend fun generateStrategy(request: StrategyRequest): StrategyRecommendation {
        val scene = request.currentVision.scene
        val genre = request.activeGenre
        val hp = request.currentVision.healthPercentage
        val profile = request.gameProfile

        return when (genre) {
            GameGenre.TURN_BASED_RPG -> {
                val isBossHpLow = hp != null && hp < 0.35f
                val title = if (isBossHpLow) "Tactical Execution Window: Boss Critical" else "Adaptive Turn Sequence Optimization"
                val analysis = if (isBossHpLow) {
                    "Enemy boss vitality has dropped to ${(hp * 100).toInt()}%. Immediate burst rotation recommended to bypass enrage timer."
                } else {
                    "Combat field stabilized. Standard skill rotation and cooldown pooling in progress for ${profile.name}."
                }

                StrategyRecommendation(
                    title = title,
                    analysis = analysis,
                    tacticalTips = listOf(
                        "Prioritize elemental skills before standard attack turns",
                        "Hold secondary buff for boss shield break phase",
                        "Maintain auto-battle cadence below ${profile.maxTapsPerSecond} taps/sec for anti-cheat safety"
                    ),
                    suggestedMacroName = if (isBossHpLow) "Tactical Skill Combo" else "Standard Attack Rotation",
                    source = StrategySource.LOCAL_HEURISTIC,
                    confidence = 0.91f
                )
            }

            GameGenre.IDLE_CLICKER -> {
                StrategyRecommendation(
                    title = "Exponential Yield & Reinvestment Curve",
                    analysis = "Analyzing income generation rate. Optimal compounding achieved when reinvesting ~70% of idle capital into top-tier multipliers every 6-8 cycles.",
                    tacticalTips = listOf(
                        "Maintain steady click bursts to keep synergy frenzy active",
                        "Prioritize global multiplier upgrades over base flat income",
                        "Use offline banking bonus before triggering stage ascensions"
                    ),
                    suggestedMacroName = "Hyper Click Burst (6x)",
                    source = StrategySource.LOCAL_HEURISTIC,
                    confidence = 0.94f
                )
            }

            GameGenre.MATCH_3_PUZZLE -> {
                StrategyRecommendation(
                    title = "Cascade Probability & Board Clearing Pattern",
                    analysis = "Board structure favors vertical orthogonal alignments in lower quadrants to generate gravity-fed cascading matches across columns.",
                    tacticalTips = listOf(
                        "Always match lowest rows first to maximize cascade chain potential",
                        "Save 4-match special jewels for cross-board combo sweeps",
                        "Clear side columns early to prevent blocked deadlocks"
                    ),
                    suggestedMacroName = "Cross Cascade Swap",
                    source = StrategySource.LOCAL_HEURISTIC,
                    confidence = 0.88f
                )
            }

            GameGenre.ACTION_ARCADE -> {
                StrategyRecommendation(
                    title = "Arcade Reaction & Dodge Pacing",
                    analysis = "High-tempo action gameplay detected. Optimizing micro-cadence and defensive evade timing.",
                    tacticalTips = listOf(
                        "Keep reaction taps spaced by minimum safe intervals",
                        "Prioritize evasive maneuvers when threat indicators flash",
                        "Maintain steady cadence to avoid triggering anti-macro filters"
                    ),
                    suggestedMacroName = "Turbo Tap Burst",
                    source = StrategySource.LOCAL_HEURISTIC,
                    confidence = 0.85f
                )
            }

            GameGenre.STRATEGY -> {
                StrategyRecommendation(
                    title = "Defensive Perimeter & Unit Deployment",
                    analysis = "Tactical strategy layout detected. Prioritizing defensive tower placement and resource pacing.",
                    tacticalTips = listOf(
                        "Deploy defensive units at chokepoints first",
                        "Hold secondary energy reserves for wave surges",
                        "Upgrade core structures before expanding frontline"
                    ),
                    suggestedMacroName = "Tactical Skill Combo",
                    source = StrategySource.LOCAL_HEURISTIC,
                    confidence = 0.87f
                )
            }

            GameGenre.CUSTOM -> {
                StrategyRecommendation(
                    title = "Universal Automation Policy",
                    analysis = "Active profile '${profile.name}' running with custom coordinate heuristics. Scene state: ${scene.displayName}.",
                    tacticalTips = listOf(
                        "Ensure critical UI interaction coordinates match target game resolution",
                        "Keep Safety Limiter enabled to prevent unnatural interaction spikes",
                        "Test macro sequences in AI Monitor before enabling long sessions"
                    ),
                    suggestedMacroName = "Quad Screen Calibration Tap",
                    source = StrategySource.LOCAL_HEURISTIC,
                    confidence = 0.80f
                )
            }
        }
    }
}
