package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.ActionHistory
import com.example.data.local.entity.GameObservation
import com.example.data.local.entity.GameProfile
import com.example.data.local.entity.GameSession
import com.example.data.local.entity.StrategyMemory
import com.example.data.local.entity.UserSettings
import kotlinx.coroutines.flow.Flow

@Dao
interface GameProfileDao {
    @Query("SELECT * FROM game_profiles ORDER BY lastPlayedAt DESC")
    fun getAllProfiles(): Flow<List<GameProfile>>

    @Query("SELECT * FROM game_profiles WHERE id = :id")
    suspend fun getProfileById(id: Long): GameProfile?

    @Query("SELECT * FROM game_profiles WHERE isActive = 1 LIMIT 1")
    fun getActiveProfile(): Flow<GameProfile?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: GameProfile): Long

    @Update
    suspend fun updateProfile(profile: GameProfile)

    @Delete
    suspend fun deleteProfile(profile: GameProfile)

    @Query("UPDATE game_profiles SET isActive = CASE WHEN id = :profileId THEN 1 ELSE 0 END")
    suspend fun setActiveProfile(profileId: Long)
}

@Dao
interface GameSessionDao {
    @Query("SELECT * FROM game_sessions ORDER BY startTime DESC")
    fun getAllSessions(): Flow<List<GameSession>>

    @Query("SELECT * FROM game_sessions WHERE id = :id")
    suspend fun getSessionById(id: Long): GameSession?

    @Query("SELECT * FROM game_sessions WHERE profileId = :profileId ORDER BY startTime DESC")
    fun getSessionsForProfile(profileId: Long): Flow<List<GameSession>>

    @Query("SELECT * FROM game_sessions ORDER BY startTime DESC LIMIT 1")
    fun getLatestSession(): Flow<GameSession?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: GameSession): Long

    @Update
    suspend fun updateSession(session: GameSession)

    @Query("SELECT COUNT(*) FROM game_sessions")
    suspend fun getTotalSessionCount(): Int
}

@Dao
interface GameObservationDao {
    @Query("SELECT * FROM game_observations WHERE sessionId = :sessionId ORDER BY timestamp DESC LIMIT 50")
    fun getObservationsForSession(sessionId: Long): Flow<List<GameObservation>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertObservation(observation: GameObservation): Long
}

@Dao
interface ActionHistoryDao {
    @Query("SELECT * FROM action_history WHERE sessionId = :sessionId ORDER BY timestamp DESC")
    fun getActionsForSession(sessionId: Long): Flow<List<ActionHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAction(action: ActionHistory): Long

    @Query("SELECT COUNT(*) FROM action_history WHERE sessionId = :sessionId")
    suspend fun getActionCountForSession(sessionId: Long): Int
}

@Dao
interface StrategyMemoryDao {
    @Query("SELECT * FROM strategy_memory WHERE profileId = :profileId ORDER BY weight DESC")
    fun getMemoriesForProfile(profileId: Long): Flow<List<StrategyMemory>>

    @Query("SELECT * FROM strategy_memory WHERE profileId = :profileId AND stateSignature = :signature LIMIT 1")
    suspend fun getMemoryBySignature(profileId: Long, signature: String): StrategyMemory?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemory(memory: StrategyMemory): Long

    @Update
    suspend fun updateMemory(memory: StrategyMemory)
}

@Dao
interface UserSettingsDao {
    @Query("SELECT * FROM user_settings WHERE id = 1 LIMIT 1")
    fun getUserSettings(): Flow<UserSettings?>

    @Query("SELECT * FROM user_settings WHERE id = 1 LIMIT 1")
    suspend fun getUserSettingsDirect(): UserSettings?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateSettings(settings: UserSettings)
}
