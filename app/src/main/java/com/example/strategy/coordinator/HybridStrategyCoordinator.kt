package com.example.strategy.coordinator

import android.util.Log
import com.example.strategy.gateway.CloudGeminiStrategyGateway
import com.example.strategy.gateway.LocalStrategyGateway
import com.example.strategy.model.StrategyRecommendation
import com.example.strategy.model.StrategyRequest
import com.example.strategy.model.StrategySource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object HybridStrategyCoordinator {

    private const val TAG = "HybridCoordinator"

    private val localGateway = LocalStrategyGateway()
    private val cloudGateway by lazy { CloudGeminiStrategyGateway() }

    private val _latestRecommendation = MutableStateFlow<StrategyRecommendation?>(null)
    val latestRecommendation: StateFlow<StrategyRecommendation?> = _latestRecommendation.asStateFlow()

    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing: StateFlow<Boolean> = _isAnalyzing.asStateFlow()

    private val _activeSource = MutableStateFlow(StrategySource.LOCAL_HEURISTIC)
    val activeSource: StateFlow<StrategySource> = _activeSource.asStateFlow()

    private val _analysisLogs = MutableStateFlow<List<String>>(
        listOf("[STRATEGY CORE] Hybrid Strategic Advisor initialized. Local & Cloud gateways ready.")
    )
    val analysisLogs: StateFlow<List<String>> = _analysisLogs.asStateFlow()

    private val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    suspend fun requestStrategicAdvice(
        request: StrategyRequest,
        allowCloudAi: Boolean = false
    ): StrategyRecommendation {
        _isAnalyzing.value = true
        addLog("[ANALYZE REQ] Analyzing game '${request.gameProfile.name}' (Scene: ${request.currentVision.scene.displayName}).")

        val recommendation: StrategyRecommendation = if (allowCloudAi) {
            try {
                addLog("[CLOUD DISPATCH] Querying Cloud Gemini 3.8 Flash for high-level tactical metagame advice...")
                val cloudResult = cloudGateway.generateStrategy(request)
                _activeSource.value = StrategySource.CLOUD_GEMINI
                addLog("[CLOUD OK] Gemini Strategic Assessment received successfully.")
                cloudResult
            } catch (e: Throwable) {
                Log.w(TAG, "Cloud AI unavailable or failed, falling back to local heuristic", e)
                addLog("[FALLBACK TO LOCAL] Cloud gateway unreachable (${e.localizedMessage ?: "Network/AppCheck hold"}). Engaging Local Neural Heuristic.")
                _activeSource.value = StrategySource.LOCAL_HEURISTIC
                localGateway.generateStrategy(request)
            }
        } else {
            addLog("[LOCAL MODE] Offline-first policy active. Generating on-device tactical recommendation.")
            _activeSource.value = StrategySource.LOCAL_HEURISTIC
            localGateway.generateStrategy(request)
        }

        _latestRecommendation.value = recommendation
        _isAnalyzing.value = false
        addLog("[ADVICE READY] '${recommendation.title}' (${recommendation.source.label})")
        return recommendation
    }

    fun addLog(message: String) {
        val timestamp = dateFormat.format(Date())
        val entry = "[$timestamp] $message"
        val updated = (_analysisLogs.value + entry).takeLast(40)
        _analysisLogs.value = updated
    }

    fun reset() {
        _latestRecommendation.value = null
        _isAnalyzing.value = false
        _activeSource.value = StrategySource.LOCAL_HEURISTIC
        _analysisLogs.value = listOf("[STRATEGY CORE] Reset completed.")
    }
}
