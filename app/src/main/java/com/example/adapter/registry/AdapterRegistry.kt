package com.example.adapter.registry

import com.example.adapter.base.IGameAdapter
import com.example.adapter.custom.CustomProfileAdapter
import com.example.adapter.idle.IdleClickerAdapter
import com.example.adapter.puzzle.Match3PuzzleAdapter
import com.example.adapter.rpg.TurnBasedRpgAdapter
import com.example.core.model.GameGenre
import com.example.data.local.entity.GameProfile

object AdapterRegistry {

    private val rpgAdapter = TurnBasedRpgAdapter()
    private val idleAdapter = IdleClickerAdapter()
    private val puzzleAdapter = Match3PuzzleAdapter()
    private val customAdapter = CustomProfileAdapter()

    private val adaptersByGenre = mapOf(
        GameGenre.TURN_BASED_RPG to rpgAdapter,
        GameGenre.IDLE_CLICKER to idleAdapter,
        GameGenre.MATCH_3_PUZZLE to puzzleAdapter,
        GameGenre.CUSTOM to customAdapter
    )

    fun getAdapter(genre: GameGenre): IGameAdapter {
        return adaptersByGenre[genre] ?: customAdapter
    }

    fun getAdapterForProfile(profile: GameProfile?): IGameAdapter {
        if (profile == null) return rpgAdapter

        val resolvedGenre = try {
            GameGenre.valueOf(profile.genre)
        } catch (_: Exception) {
            GameGenre.CUSTOM
        }

        return getAdapter(resolvedGenre)
    }

    fun getAllAdapters(): List<IGameAdapter> {
        return listOf(rpgAdapter, idleAdapter, puzzleAdapter, customAdapter)
    }
}
