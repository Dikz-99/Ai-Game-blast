package com.example.capture.model

import android.graphics.Bitmap

data class CapturedFrame(
    val bitmap: Bitmap,
    val width: Int,
    val height: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val sequenceNumber: Long = 0L
)

data class CaptureMetrics(
    val isCapturing: Boolean = false,
    val currentFps: Double = 0.0,
    val targetFps: Int = 10,
    val captureLatencyMs: Long = 0L,
    val totalFramesCaptured: Long = 0L,
    val droppedFrames: Long = 0L,
    val captureWidth: Int = 0,
    val captureHeight: Int = 0,
    val densityDpi: Int = 0
)
