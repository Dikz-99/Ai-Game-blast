package com.example.vision.pipeline

import com.example.vision.model.DetectedGameScene
import com.example.vision.model.VisionResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object VisionPipeline {

    private val _latestResult = MutableStateFlow(VisionResult())
    val latestResult: StateFlow<VisionResult> = _latestResult.asStateFlow()

    private val _recentOcrLogs = MutableStateFlow<List<String>>(
        listOf(
            "[VISION READY] Neural Vision & OCR Engine initialized",
            "[OCR ENGINE] TextRecognition pipeline linked to ML Kit",
            "[CV ENGINE] ColorDetector calibrated for HP bar & Button detection"
        )
    )
    val recentOcrLogs: StateFlow<List<String>> = _recentOcrLogs.asStateFlow()

    fun publishResult(result: VisionResult) {
        _latestResult.value = result

        val log = when (result.scene) {
            DetectedGameScene.BATTLE_IN_PROGRESS ->
                "[VISION BATTLE] Targets: ${result.boundingBoxes.size} | HP: ${(result.healthPercentage?.times(100))?.toInt() ?: 0}% | Time: ${result.processingTimeMs}ms"
            DetectedGameScene.VICTORY_REWARDS ->
                "[VISION VICTORY] Claim button detected @ (${result.primaryActionTarget?.left ?: 0f}, ${result.primaryActionTarget?.top ?: 0f})"
            DetectedGameScene.DEFEAT_RETRY ->
                "[VISION DEFEAT] Retry button detected"
            DetectedGameScene.POPUP_DIALOG ->
                "[VISION POPUP] Dialog dismissal button found"
            else ->
                "[VISION SCENE] ${result.scene.displayName} (${(result.confidence * 100).toInt()}%)"
        }

        addLog(log)
    }

    fun addLog(log: String) {
        val current = _recentOcrLogs.value.toMutableList()
        current.add(0, log)
        if (current.size > 40) {
            current.removeAt(current.size - 1)
        }
        _recentOcrLogs.value = current
    }

    fun reset() {
        _latestResult.value = VisionResult()
    }
}
