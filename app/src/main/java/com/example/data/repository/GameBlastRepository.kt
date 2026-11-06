package com.example.data.repository

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
import kotlinx.coroutines.flow.Flow

class GameBlastRepository(
    private val profileDao: GameProfileDao,
    private val sessionDao: GameSessionDao,
    private val observationDao: GameObservationDao,
    private val actionHistoryDao: ActionHistoryDao,
    private val strategyMemoryDao: StrategyMemoryDao,
    private val userSettingsDao: UserSettingsDao
) {
    // Profiles
    val allProfiles: Flow<List<GameProfile>> = profileDao.getAllProfiles()
    val activeProfile: Flow<GameProfile?> = profileDao.getActiveProfile()

    suspend fun getProfileById(id: Long): GameProfile? = profileDao.getProfileById(id)
    suspend fun saveProfile(profile: GameProfile): Long = profileDao.insertProfile(profile)
    suspend fun updateProfile(profile: GameProfile) = profileDao.updateProfile(profile)
    suspend fun deleteProfile(profile: GameProfile) = profileDao.deleteProfile(profile)
    suspend fun setActiveProfile(profileId: Long) = profileDao.setActiveProfile(profileId)

    // Sessions
    val allSessions: Flow<List<GameSession>> = sessionDao.getAllSessions()
    val latestSession: Flow<GameSession?> = sessionDao.getLatestSession()

    suspend fun getSessionById(id: Long): GameSession? = sessionDao.getSessionById(id)
    suspend fun startSession(profileId: Long, profileName: String): Long {
        val newSession = GameSession(
            profileId = profileId,
            profileName = profileName,
            startTime = System.currentTimeMillis(),
            status = "RUNNING"
        )
        return sessionDao.insertSession(newSession)
    }

    suspend fun updateSession(session: GameSession) = sessionDao.updateSession(session)
    fun getSessionsForProfile(profileId: Long): Flow<List<GameSession>> = sessionDao.getSessionsForProfile(profileId)

    // Observations & Actions
    fun getObservationsForSession(sessionId: Long): Flow<List<GameObservation>> = observationDao.getObservationsForSession(sessionId)
    suspend fun logObservation(observation: GameObservation) = observationDao.insertObservation(observation)

    fun getActionsForSession(sessionId: Long): Flow<List<ActionHistory>> = actionHistoryDao.getActionsForSession(sessionId)
    suspend fun logAction(action: ActionHistory) = actionHistoryDao.insertAction(action)

    // Strategy Memory
    fun getMemoriesForProfile(profileId: Long): Flow<List<StrategyMemory>> = strategyMemoryDao.getMemoriesForProfile(profileId)
    suspend fun getMemoryBySignature(profileId: Long, signature: String): StrategyMemory? =
        strategyMemoryDao.getMemoryBySignature(profileId, signature)
    suspend fun saveMemory(memory: StrategyMemory) = strategyMemoryDao.insertMemory(memory)
    suspend fun updateMemory(memory: StrategyMemory) = strategyMemoryDao.updateMemory(memory)

    // User Settings
    val userSettings: Flow<UserSettings?> = userSettingsDao.getUserSettings()
    suspend fun getUserSettingsDirect(): UserSettings? = userSettingsDao.getUserSettingsDirect()
    suspend fun updateSettings(settings: UserSettings) = userSettingsDao.insertOrUpdateSettings(settings)
}
