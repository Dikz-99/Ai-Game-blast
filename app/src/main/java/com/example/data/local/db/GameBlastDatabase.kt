package com.example.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.core.model.GameGenre
import com.example.data.local.dao.ActionHistoryDao
import com.example.data.local.dao.GameObservationDao
import com.example.data.local.dao.GameProfileDao
import com.example.data.local.dao.GameSessionDao
import com.example.data.local.dao.StrategyMemoryDao
import com.example.data.local.dao.UserSettingsDao
import com.example.data.local.entity.ActionHistory
import com.example.data.local.entity.GameObservation
import com.example.data.local.entity.GameProfile
import com.example.data.local.entity.GameSession
import com.example.data.local.entity.StrategyMemory
import com.example.data.local.entity.UserSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        GameProfile::class,
        GameSession::class,
        GameObservation::class,
        ActionHistory::class,
        StrategyMemory::class,
        UserSettings::class
    ],
    version = 1,
    exportSchema = false
)
abstract class GameBlastDatabase : RoomDatabase() {
    abstract fun gameProfileDao(): GameProfileDao
    abstract fun gameSessionDao(): GameSessionDao
    abstract fun gameObservationDao(): GameObservationDao
    abstract fun actionHistoryDao(): ActionHistoryDao
    abstract fun strategyMemoryDao(): StrategyMemoryDao
    abstract fun userSettingsDao(): UserSettingsDao

    companion object {
        @Volatile
        private var INSTANCE: GameBlastDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): GameBlastDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GameBlastDatabase::class.java,
                    "game_blast_database"
                )
                    .addCallback(DatabaseCallback(scope))
                    .fallbackToDestructiveMigration(false)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback(
        private val scope: CoroutineScope
    ) : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateInitialData(database)
                }
            }
        }

        private suspend fun populateInitialData(database: GameBlastDatabase) {
            val profileDao = database.gameProfileDao()
            val settingsDao = database.userSettingsDao()

            // Default User Settings
            settingsDao.insertOrUpdateSettings(
                UserSettings(
                    id = 1,
                    safeMaxTapsPerSecond = 4,
                    minIntervalBetweenActionsMs = 250,
                    enableNaturalJitter = true,
                    emergencyStopSensitivity = "HIGH",
                    enableOverlayHud = true,
                    enableAudioHapticFeedback = true,
                    cloudAiOptIn = false,
                    autoRecordSessions = true,
                    darkTheme = true
                )
            )

            // Seed initial Game Profiles
            val p1 = GameProfile(
                name = "Cyber RPG Arena",
                packageName = "com.aistudio.cyberarena",
                genre = GameGenre.TURN_BASED_RPG.name,
                description = "Turn-based tactical RPG with automated turn execution, skill cooldown tracking and party health monitoring.",
                targetFps = 15,
                maxTapsPerSecond = 3,
                isAutoRestartEnabled = true,
                isActive = true,
                configJson = """{"autoSkillPriority": ["ULTIMATE", "SKILL_2", "BASIC_ATTACK"], "healThresholdPercent": 35}"""
            )

            val p2 = GameProfile(
                name = "Quantum Idle Miner",
                packageName = "com.aistudio.quantumidle",
                genre = GameGenre.IDLE_CLICKER.name,
                description = "Idle incremental clicker with auto upgrade balancing, booster activation, and offline revenue claimer.",
                targetFps = 8,
                maxTapsPerSecond = 6,
                isAutoRestartEnabled = false,
                isActive = false,
                configJson = """{"upgradeFrequencySec": 15, "claimChestOnSight": true}"""
            )

            val p3 = GameProfile(
                name = "Neon Jewel Odyssey",
                packageName = "com.aistudio.neonjewel",
                genre = GameGenre.MATCH_3_PUZZLE.name,
                description = "Color tile match-3 game with pattern recognition, combo priority detection, and automated cascade moves.",
                targetFps = 12,
                maxTapsPerSecond = 2,
                isAutoRestartEnabled = false,
                isActive = false,
                configJson = """{"comboDepth": 3, "preferFourMatch": true}"""
            )

            val p1Id = profileDao.insertProfile(p1)
            val p2Id = profileDao.insertProfile(p2)
            profileDao.insertProfile(p3)

            // Seed initial memory for Cyber RPG Arena
            val memoryDao = database.strategyMemoryDao()
            memoryDao.insertMemory(
                StrategyMemory(
                    profileId = p1Id,
                    stateSignature = "BATTLE_TURN_READY",
                    suggestedAction = "TAP",
                    targetCoordinates = "540,1850",
                    successCount = 28,
                    failureCount = 1,
                    weight = 0.96f
                )
            )
            memoryDao.insertMemory(
                StrategyMemory(
                    profileId = p1Id,
                    stateSignature = "VICTORY_REWARD_CLAIM",
                    suggestedAction = "TAP",
                    targetCoordinates = "540,1500",
                    successCount = 14,
                    failureCount = 0,
                    weight = 1.0f
                )
            )

            // Seed sample past session
            val sessionDao = database.gameSessionDao()
            val sessionId = sessionDao.insertSession(
                GameSession(
                    profileId = p1Id,
                    profileName = "Cyber RPG Arena",
                    startTime = System.currentTimeMillis() - 7200000,
                    endTime = System.currentTimeMillis() - 5400000,
                    totalActions = 84,
                    successfulActions = 82,
                    totalObservations = 210,
                    status = "COMPLETED",
                    averageLatencyMs = 42,
                    summaryNotes = "Auto battle sequence completed 4 stages successfully."
                )
            )

            // Seed sample action history
            val actionDao = database.actionHistoryDao()
            actionDao.insertAction(
                ActionHistory(
                    sessionId = sessionId,
                    timestamp = System.currentTimeMillis() - 6500000,
                    actionType = "TAP",
                    x = 540f,
                    y = 1850f,
                    durationMs = 85,
                    success = true,
                    resultOutcome = "SKILL_ACTIVATED"
                )
            )
            actionDao.insertAction(
                ActionHistory(
                    sessionId = sessionId,
                    timestamp = System.currentTimeMillis() - 6480000,
                    actionType = "TAP",
                    x = 880f,
                    y = 1720f,
                    durationMs = 90,
                    success = true,
                    resultOutcome = "TARGET_SELECTED"
                )
            )
        }
    }
}
