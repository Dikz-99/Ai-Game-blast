package com.example.learning.pipeline

import com.example.learning.model.LearningUpdateRecord
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object LearningPipeline {

    private val _totalTransitionsEvaluated = MutableStateFlow(0L)
    val totalTransitionsEvaluated: StateFlow<Long> = _totalTransitionsEvaluated.asStateFlow()

    private val _weightAdjustmentsCount = MutableStateFlow(0L)
    val weightAdjustmentsCount: StateFlow<Long> = _weightAdjustmentsCount.asStateFlow()

    private val _latestLearningRecord = MutableStateFlow<LearningUpdateRecord?>(null)
    val latestLearningRecord: StateFlow<LearningUpdateRecord?> = _latestLearningRecord.asStateFlow()

    private val _learningFeed = MutableStateFlow<List<String>>(
        listOf("[LEARNING CORE] Heuristic State Transition Evaluator online.")
    )
    val learningFeed: StateFlow<List<String>> = _learningFeed.asStateFlow()

    private val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    fun recordTransitionEvaluated() {
        _totalTransitionsEvaluated.value += 1
    }

    fun recordUpdate(record: LearningUpdateRecord) {
        _latestLearningRecord.value = record
        _weightAdjustmentsCount.value += 1
        val symbol = if (record.weightDelta >= 0) "+${String.format(Locale.US, "%.2f", record.weightDelta)}" else String.format(Locale.US, "%.2f", record.weightDelta)
        addLearningLog("[WEIGHT ADAPT] '${record.stateSignature}' -> Weight: ${String.format(Locale.US, "%.2f", record.newWeight)} ($symbol) [${record.outcome.label}]")
    }

    fun addLearningLog(message: String) {
        val timestamp = dateFormat.format(Date())
        val entry = "[$timestamp] $message"
        val updated = (_learningFeed.value + entry).takeLast(50)
        _learningFeed.value = updated
    }

    fun reset() {
        _totalTransitionsEvaluated.value = 0L
        _weightAdjustmentsCount.value = 0L
        _latestLearningRecord.value = null
        _learningFeed.value = listOf("[LEARNING CORE] Reset completed.")
    }
}
