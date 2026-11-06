package com.example.vision.ocr

import android.graphics.Bitmap
import com.example.vision.model.RecognizedTextSnippet
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

object MlKitOcrEngine {

    private val recognizer by lazy {
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    }

    suspend fun recognizeText(bitmap: Bitmap): List<RecognizedTextSnippet> {
        return suspendCancellableCoroutine { continuation ->
            try {
                val inputImage = InputImage.fromBitmap(bitmap, 0)
                recognizer.process(inputImage)
                    .addOnSuccessListener { visionText ->
                        val snippets = mutableListOf<RecognizedTextSnippet>()
                        val imageWidth = bitmap.width.toFloat().coerceAtLeast(1f)
                        val imageHeight = bitmap.height.toFloat().coerceAtLeast(1f)

                        for (block in visionText.textBlocks) {
                            for (line in block.lines) {
                                val box = line.boundingBox
                                if (box != null) {
                                    snippets.add(
                                        RecognizedTextSnippet(
                                            text = line.text.trim(),
                                            confidence = line.confidence ?: 0.9f,
                                            left = (box.left / imageWidth).coerceIn(0f, 1f),
                                            top = (box.top / imageHeight).coerceIn(0f, 1f),
                                            right = (box.right / imageWidth).coerceIn(0f, 1f),
                                            bottom = (box.bottom / imageHeight).coerceIn(0f, 1f)
                                        )
                                    )
                                }
                            }
                        }
                        if (continuation.isActive) {
                            continuation.resume(snippets)
                        }
                    }
                    .addOnFailureListener { exception ->
                        // Fallback safely to prevent crashing
                        exception.printStackTrace()
                        if (continuation.isActive) {
                            continuation.resume(emptyList())
                        }
                    }
            } catch (e: Exception) {
                e.printStackTrace()
                if (continuation.isActive) {
                    continuation.resume(emptyList())
                }
            }
        }
    }
}
