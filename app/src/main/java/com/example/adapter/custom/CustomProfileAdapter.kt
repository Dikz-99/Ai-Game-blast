package com.example.adapter.custom

import com.example.adapter.base.IGameAdapter
import com.example.adapter.model.AdapterDecision
import com.example.adapter.model.AdapterGameState
import com.example.automation.model.ActionCommand
import com.example.automation.model.MacroSequence
import com.example.core.model.GameGenre
import com.example.vision.model.DetectedGameScene
import com.example.vision.model.VisionResult

class CustomProfileAdapter : IGameAdapter {

    override val genre: GameGenre = GameGenre.CUSTOM
    override val adapterName: String = "Universal Custom Game Adapter"
    override val description: String = "User-customizable rules, coordinate mappings, and generic game interactions."

    override fun extractState(vision: VisionResult): AdapterGameState {
        return AdapterGameState(
            genre = genre,
            scene = vision.scene,
            healthRatio = vision.healthPercentage,
            availableSkills = emptyList(),
            interactableElements = vision.boundingBoxes,
            extractedKeywords = vision.textSnippets.map { it.text },
            isActionReady = vision.hasInteractableButton,
            metrics = mapOf(
                "Custom Rule Active" to "true",
                "Targets Detected" to "${vision.boundingBoxes.size}"
            )
        )
    }

    override fun decideAction(
        state: AdapterGameState,
        screenWidth: Int,
        screenHeight: Int
    ): AdapterDecision {
        // If an interactable button is spotted, target its center
        val targetBox = state.interactableElements.firstOrNull()
        if (targetBox != null) {
            val targetX = (targetBox.left + targetBox.right) / 2f * screenWidth
            val targetY = (targetBox.top + targetBox.bottom) / 2f * screenHeight

            return AdapterDecision(
                action = ActionCommand.TapAction(targetX, targetY, label = "Custom Target Tap"),
                confidence = targetBox.confidence,
                strategyNotes = "Custom adapter striking detected '${targetBox.label}' element."
            )
        }

        // Default: center tap or wait
        return if (state.scene == DetectedGameScene.LOADING_SCREEN) {
            AdapterDecision(
                action = ActionCommand.WaitAction(500L, label = "Loading Hold"),
                confidence = 0.90f,
                strategyNotes = "Custom profile waiting for asset loading."
            )
        } else {
            AdapterDecision(
                action = ActionCommand.TapAction(screenWidth * 0.5f, screenHeight * 0.5f, label = "Center Default Tap"),
                confidence = 0.65f,
                strategyNotes = "Executing default center interaction in absence of specific UI markers."
            )
        }
    }

    override fun getPresetMacros(): List<MacroSequence> {
        return listOf(
            MacroSequence(
                id = "custom_quad_tap",
                name = "Quad Screen Calibration Tap",
                description = "Taps four corners and center to calibrate touchscreen responsiveness",
                actions = listOf(
                    ActionCommand.TapAction(200f, 400f, label = "Top Left"),
                    ActionCommand.WaitAction(100L),
                    ActionCommand.TapAction(880f, 400f, label = "Top Right"),
                    ActionCommand.WaitAction(100L),
                    ActionCommand.TapAction(200f, 2000f, label = "Bottom Left"),
                    ActionCommand.WaitAction(100L),
                    ActionCommand.TapAction(880f, 2000f, label = "Bottom Right")
                )
            )
        )
    }
}
