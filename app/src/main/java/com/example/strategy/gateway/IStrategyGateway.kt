package com.example.strategy.gateway

import com.example.strategy.model.StrategyRecommendation
import com.example.strategy.model.StrategyRequest

interface IStrategyGateway {
    val gatewayName: String

    /**
     * Synthesizes game state telemetry and yields strategic gameplay advice.
     */
    suspend fun generateStrategy(request: StrategyRequest): StrategyRecommendation
}
