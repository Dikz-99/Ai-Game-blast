package com.example.vision.detector

import com.example.core.model.VisualBoundingBox
import com.example.vision.model.DetectedGameScene
import com.example.vision.model.RecognizedTextSnippet

object GameElementDetector {

    data class DetectionSummary(
        val scene: DetectedGameScene,
        val confidence: Float,
        val boundingBoxes: List<VisualBoundingBox>,
        val primaryTarget: VisualBoundingBox?
    )

    fun detectElements(
        snippets: List<RecognizedTextSnippet>,
        healthBarPercent: Float?
    ): DetectionSummary {
        val detectedBoxes = mutableListOf<VisualBoundingBox>()
        var primaryAction: VisualBoundingBox? = null

        val fullText = snippets.joinToString(" ") { it.text.uppercase() }

        // 1. Check for Victory / Reward Screen
        val isVictory = fullText.contains("VICTORY") ||
                fullText.contains("COMPLETE") ||
                fullText.contains("STAGE CLEAR") ||
                fullText.contains("CLAIM")

        // 2. Check for Defeat / Game Over Screen
        val isDefeat = fullText.contains("DEFEAT") ||
                fullText.contains("GAME OVER") ||
                fullText.contains("FAILED") ||
                fullText.contains("RETRY")

        // 3. Check for Combat / In Battle Screen
        val isBattle = fullText.contains("ATTACK") ||
                fullText.contains("SKILL") ||
                fullText.contains("AUTO") ||
                fullText.contains("WAVE") ||
                fullText.contains("BOSS") ||
                (healthBarPercent != null && healthBarPercent > 0.05f)

        // 4. Check for Dialog / Popup
        val isPopup = fullText.contains("CONFIRM") ||
                fullText.contains("CANCEL") ||
                fullText.contains("CLOSE") ||
                fullText.contains("NOTICE")

        // Extract action buttons from text snippets
        snippets.forEachIndexed { index, snippet ->
            val textUpper = snippet.text.uppercase()

            when {
                textUpper.contains("ATTACK") || textUpper.contains("FIGHT") -> {
                    val box = VisualBoundingBox(
                        id = "btn_attack_$index",
                        label = "ATTACK_BUTTON",
                        confidence = snippet.confidence,
                        left = snippet.left,
                        top = snippet.top,
                        right = snippet.right,
                        bottom = snippet.bottom,
                        detectedText = snippet.text
                    )
                    detectedBoxes.add(box)
                    if (primaryAction == null) primaryAction = box
                }

                textUpper.contains("SKILL") || textUpper.contains("READY") || textUpper.contains("ULT") -> {
                    val box = VisualBoundingBox(
                        id = "btn_skill_$index",
                        label = "SKILL_BUTTON",
                        confidence = snippet.confidence,
                        left = snippet.left,
                        top = snippet.top,
                        right = snippet.right,
                        bottom = snippet.bottom,
                        detectedText = snippet.text
                    )
                    detectedBoxes.add(box)
                }

                textUpper.contains("CLAIM") || textUpper.contains("NEXT") || textUpper.contains("CONTINUE") -> {
                    val box = VisualBoundingBox(
                        id = "btn_claim_$index",
                        label = "CLAIM_BUTTON",
                        confidence = snippet.confidence,
                        left = snippet.left,
                        top = snippet.top,
                        right = snippet.right,
                        bottom = snippet.bottom,
                        detectedText = snippet.text
                    )
                    detectedBoxes.add(box)
                    if (primaryAction == null || isVictory) primaryAction = box
                }

                textUpper.contains("RETRY") || textUpper.contains("TRY AGAIN") -> {
                    val box = VisualBoundingBox(
                        id = "btn_retry_$index",
                        label = "RETRY_BUTTON",
                        confidence = snippet.confidence,
                        left = snippet.left,
                        top = snippet.top,
                        right = snippet.right,
                        bottom = snippet.bottom,
                        detectedText = snippet.text
                    )
                    detectedBoxes.add(box)
                    if (primaryAction == null || isDefeat) primaryAction = box
                }

                textUpper.contains("OK") || textUpper.contains("CONFIRM") || textUpper.contains("CLOSE") -> {
                    val box = VisualBoundingBox(
                        id = "btn_dialog_$index",
                        label = "DIALOG_ACTION",
                        confidence = snippet.confidence,
                        left = snippet.left,
                        top = snippet.top,
                        right = snippet.right,
                        bottom = snippet.bottom,
                        detectedText = snippet.text
                    )
                    detectedBoxes.add(box)
                    if (primaryAction == null || isPopup) primaryAction = box
                }

                textUpper.contains("AUTO") -> {
                    val box = VisualBoundingBox(
                        id = "btn_auto_$index",
                        label = "AUTO_TOGGLE",
                        confidence = snippet.confidence,
                        left = snippet.left,
                        top = snippet.top,
                        right = snippet.right,
                        bottom = snippet.bottom,
                        detectedText = snippet.text
                    )
                    detectedBoxes.add(box)
                }
            }
        }

        // Add HP Bar box if detected
        if (healthBarPercent != null && healthBarPercent > 0.05f) {
            detectedBoxes.add(
                0,
                VisualBoundingBox(
                    id = "hud_hp_bar",
                    label = "BOSS_HEALTH_BAR",
                    confidence = 0.95f,
                    left = 0.05f,
                    top = 0.08f,
                    right = 0.95f,
                    bottom = 0.16f,
                    detectedText = "HP ${(healthBarPercent * 100).toInt()}%"
                )
            )
        }

        // Determine scene
        val (scene, confidence) = when {
            isVictory -> Pair(DetectedGameScene.VICTORY_REWARDS, 0.94f)
            isDefeat -> Pair(DetectedGameScene.DEFEAT_RETRY, 0.92f)
            isPopup -> Pair(DetectedGameScene.POPUP_DIALOG, 0.88f)
            isBattle -> Pair(DetectedGameScene.BATTLE_IN_PROGRESS, 0.90f)
            fullText.contains("START") || fullText.contains("LOBBY") || fullText.contains("PLAY") ->
                Pair(DetectedGameScene.LOBBY_MENU, 0.85f)
            snippets.isEmpty() -> Pair(DetectedGameScene.UNKNOWN, 0.1f)
            else -> Pair(DetectedGameScene.BATTLE_IN_PROGRESS, 0.75f)
        }

        return DetectionSummary(
            scene = scene,
            confidence = confidence,
            boundingBoxes = detectedBoxes,
            primaryTarget = primaryAction
        )
    }
}
