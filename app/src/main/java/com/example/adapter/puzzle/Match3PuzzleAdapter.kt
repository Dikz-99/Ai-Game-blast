package com.example.adapter.puzzle

import com.example.adapter.base.IGameAdapter
import com.example.adapter.model.AdapterDecision
import com.example.adapter.model.AdapterGameState
import com.example.automation.model.ActionCommand
import com.example.automation.model.MacroSequence
import com.example.core.model.GameGenre
import com.example.vision.model.DetectedGameScene
import com.example.vision.model.VisionResult

class Match3PuzzleAdapter : IGameAdapter {

    override val genre: GameGenre = GameGenre.MATCH_3_PUZZLE
    override val adapterName: String = "Neon Jewel Match-3 Adapter"
    override val description: String = "Solves board configurations, generates adjacent jewel swaps, and triggers combos."

    private var moveCycle = 0

    override fun extractState(vision: VisionResult): AdapterGameState {
        return AdapterGameState(
            genre = genre,
            scene = vision.scene,
            healthRatio = null,
            availableSkills = listOf("GEM_SWAP_READY"),
            interactableElements = vision.boundingBoxes,
            extractedKeywords = vision.textSnippets.map { it.text.uppercase() },
            isActionReady = true,
            metrics = mapOf(
                "Move Cycle" to "$moveCycle",
                "Board Status" to "Active Match Grid"
            )
        )
    }

    override fun decideAction(
        state: AdapterGameState,
        screenWidth: Int,
        screenHeight: Int
    ): AdapterDecision {
        moveCycle++

        if (state.scene == DetectedGameScene.VICTORY_REWARDS) {
            return AdapterDecision(
                action = ActionCommand.TapAction(screenWidth * 0.5f, screenHeight * 0.78f, label = "Claim Puzzle Victory"),
                confidence = 0.95f,
                strategyNotes = "Puzzle goal completed. Claiming level stars and rewards."
            )
        }

        // Calculate a valid adjacent swap within the puzzle board grid
        val boardTop = screenHeight * 0.38f
        val boardBottom = screenHeight * 0.78f
        val boardHeight = boardBottom - boardTop
        val tileSize = boardHeight / 7f

        // Alternate swap direction and coordinate to explore matches
        val col = (moveCycle % 5) + 1
        val row = ((moveCycle / 5) % 5) + 1

        val tileStartX = screenWidth * 0.15f + (col * tileSize)
        val tileStartY = boardTop + (row * tileSize)

        val isHorizontalSwap = (moveCycle % 2 == 0)
        val tileEndX = if (isHorizontalSwap) tileStartX + tileSize else tileStartX
        val tileEndY = if (isHorizontalSwap) tileStartY else tileStartY + tileSize

        return AdapterDecision(
            action = ActionCommand.SwipeAction(
                startX = tileStartX,
                startY = tileStartY,
                endX = tileEndX,
                endY = tileEndY,
                durationMs = 280L,
                label = if (isHorizontalSwap) "Horizontal Gem Swap" else "Vertical Gem Swap"
            ),
            confidence = 0.89f,
            strategyNotes = "Executing tactical ${if (isHorizontalSwap) "horizontal" else "vertical"} gem swap at grid ($col, $row)."
        )
    }

    override fun getPresetMacros(): List<MacroSequence> {
        return listOf(
            MacroSequence(
                id = "puzzle_cross_swap",
                name = "Cross Cascade Swap",
                description = "Executes two rapid orthogonal swaps to induce cascading matches",
                actions = listOf(
                    ActionCommand.SwipeAction(400f, 1200f, 550f, 1200f, durationMs = 250L, label = "Swap Row"),
                    ActionCommand.WaitAction(300L),
                    ActionCommand.SwipeAction(550f, 1200f, 550f, 1350f, durationMs = 250L, label = "Swap Col")
                )
            ),
            MacroSequence(
                id = "puzzle_turbo_shuffle",
                name = "Emergency Gem Shuffle",
                description = "Quick swipe gestures across center board to reshuffle deadlocks",
                actions = listOf(
                    ActionCommand.SwipeAction(300f, 1400f, 800f, 1400f, durationMs = 200L, label = "Sweep Across"),
                    ActionCommand.WaitAction(180L),
                    ActionCommand.SwipeAction(800f, 1550f, 300f, 1550f, durationMs = 200L, label = "Sweep Back")
                )
            )
        )
    }
}
