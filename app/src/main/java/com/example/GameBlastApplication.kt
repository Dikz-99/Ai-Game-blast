package com.example

import android.app.Application
import com.example.data.local.db.GameBlastDatabase
import com.example.data.repository.GameBlastRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class GameBlastApplication : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    val database by lazy {
        GameBlastDatabase.getDatabase(this, applicationScope)
    }

    val repository by lazy {
        GameBlastRepository(
            profileDao = database.gameProfileDao(),
            sessionDao = database.gameSessionDao(),
            observationDao = database.gameObservationDao(),
            actionHistoryDao = database.actionHistoryDao(),
            strategyMemoryDao = database.strategyMemoryDao(),
            userSettingsDao = database.userSettingsDao()
        )
    }

    companion object {
        lateinit var instance: GameBlastApplication
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}
