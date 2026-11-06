package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "game_profiles")
data class GameProfile(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val packageName: String,
    val genre: String, // from GameGenre enum
    val description: String = "",
    val targetFps: Int = 10,
    val maxTapsPerSecond: Int = 5,
    val isAutoRestartEnabled: Boolean = false,
    val isCloudSyncEnabled: Boolean = false,
    val isActive: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val lastPlayedAt: Long = System.currentTimeMillis(),
    val configJson: String = "{}"
)

@Entity(tableName = "game_sessions")
data class GameSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val profileId: Long,
    val profileName: String,
    val startTime: Long = System.currentTimeMillis(),
    val endTime: Long? = null,
    val totalActions: Int = 0,
    val successfulActions: Int = 0,
    val totalObservations: Int = 0,
    val status: String = "COMPLETED", // COMPLETED, STOPPED_BY_USER, EMERGENCY_ABORT
    val averageLatencyMs: Long = 0,
    val summaryNotes: String = ""
)

@Entity(tableName = "game_observations")
data class GameObservation(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sessionId: Long,
    val timestamp: Long = System.currentTimeMillis(),
    val detectedText: String = "",
    val detectedEntitiesJson: String = "[]",
    val gameState: String = "SCANNING",
    val confidence: Float = 0.0f
)

@Entity(tableName = "action_history")
data class ActionHistory(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sessionId: Long,
    val timestamp: Long = System.currentTimeMillis(),
    val actionType: String = "TAP",
    val x: Float,
    val y: Float,
    val endX: Float? = null,
    val endY: Float? = null,
    val durationMs: Long = 80,
    val success: Boolean = true,
    val resultOutcome: String = "UNKNOWN"
)

@Entity(tableName = "strategy_memory")
data class StrategyMemory(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val profileId: Long,
    val stateSignature: String,
    val suggestedAction: String,
    val targetCoordinates: String = "", // e.g. "x,y"
    val successCount: Int = 0,
    val failureCount: Int = 0,
    val weight: Float = 1.0f,
    val lastUpdated: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_settings")
data class UserSettings(
    @PrimaryKey
    val id: Int = 1,
    val safeMaxTapsPerSecond: Int = 4,
    val minIntervalBetweenActionsMs: Long = 250,
    val enableNaturalJitter: Boolean = true,
    val emergencyStopSensitivity: String = "HIGH", // HIGH, MEDIUM, LOW
    val enableOverlayHud: Boolean = true,
    val enableAudioHapticFeedback: Boolean = true,
    val cloudAiOptIn: Boolean = false,
    val autoRecordSessions: Boolean = true,
    val darkTheme: Boolean = true
)
