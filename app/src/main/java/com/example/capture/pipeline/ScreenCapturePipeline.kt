package com.example.capture.pipeline

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.example.capture.model.CaptureMetrics
import com.example.capture.model.CapturedFrame
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object ScreenCapturePipeline {

    private val _latestFrame = MutableStateFlow<CapturedFrame?>(null)
    val latestFrame: StateFlow<CapturedFrame?> = _latestFrame.asStateFlow()

    private val _metrics = MutableStateFlow(CaptureMetrics())
    val metrics: StateFlow<CaptureMetrics> = _metrics.asStateFlow()

    private var frameCounter: Long = 0L
    private var lastFpsTimestamp: Long = 0L
    private var fpsWindowFrames: Long = 0L

    fun publishFrame(bitmap: Bitmap, width: Int, height: Int, latencyMs: Long) {
        val now = System.currentTimeMillis()
        frameCounter++
        fpsWindowFrames++

        var calculatedFps = _metrics.value.currentFps
        if (now - lastFpsTimestamp >= 1000L) {
            calculatedFps = (fpsWindowFrames * 1000.0) / (now - lastFpsTimestamp)
            fpsWindowFrames = 0L
            lastFpsTimestamp = now
        }

        val frame = CapturedFrame(
            bitmap = bitmap,
            width = width,
            height = height,
            timestamp = now,
            sequenceNumber = frameCounter
        )

        _latestFrame.value = frame
        _metrics.value = _metrics.value.copy(
            isCapturing = true,
            currentFps = calculatedFps,
            captureLatencyMs = latencyMs,
            totalFramesCaptured = frameCounter,
            captureWidth = width,
            captureHeight = height
        )
    }

    fun recordDroppedFrame() {
        _metrics.value = _metrics.value.copy(
            droppedFrames = _metrics.value.droppedFrames + 1
        )
    }

    fun updateTargetFps(fps: Int) {
        _metrics.value = _metrics.value.copy(targetFps = fps)
    }

    fun updateDisplayConfiguration(width: Int, height: Int, dpi: Int) {
        _metrics.value = _metrics.value.copy(
            captureWidth = width,
            captureHeight = height,
            densityDpi = dpi
        )
    }

    fun reset() {
        _latestFrame.value = null
        frameCounter = 0L
        fpsWindowFrames = 0L
        lastFpsTimestamp = 0L
        _metrics.value = CaptureMetrics()
    }

    /**
     * Creates a synthetic test frame containing simulated game graphics,
     * HUD text, and buttons for pipeline verification without requiring physical screen capture.
     */
    fun generateSyntheticGameFrame(
        width: Int = 720,
        height: Int = 1280,
        stage: String = "STAGE 4-2",
        bossHpPercent: Int = 65
    ): CapturedFrame {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Background dark gradient
        canvas.drawColor(Color.parseColor("#0C0E17"))

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Draw Stage Header
        paint.color = Color.parseColor("#00E5FF")
        paint.textSize = 36f
        paint.isFakeBoldText = true
        canvas.drawText(stage, 40f, 100f, paint)

        // Draw Boss HP Bar background
        paint.color = Color.parseColor("#282E48")
        canvas.drawRect(40f, 140f, width - 40f, 180f, paint)

        // Draw Boss HP Bar active
        paint.color = Color.parseColor("#FF2A55")
        val hpWidth = (width - 80f) * (bossHpPercent / 100f)
        canvas.drawRect(40f, 140f, 40f + hpWidth, 180f, paint)

        // Draw Boss HP text
        paint.color = Color.WHITE
        paint.textSize = 24f
        canvas.drawText("BOSS HP: $bossHpPercent%", 50f, 170f, paint)

        // Draw Action Attack Button
        paint.color = Color.parseColor("#00E676")
        canvas.drawRoundRect(width * 0.6f, height * 0.78f, width * 0.92f, height * 0.88f, 24f, 24f, paint)

        paint.color = Color.BLACK
        paint.textSize = 32f
        paint.isFakeBoldText = true
        canvas.drawText("ATTACK", width * 0.68f, height * 0.84f, paint)

        // Draw Skill Ready button
        paint.color = Color.parseColor("#FFB703")
        canvas.drawRoundRect(width * 0.2f, height * 0.78f, width * 0.52f, height * 0.88f, 24f, 24f, paint)

        paint.color = Color.BLACK
        paint.textSize = 30f
        canvas.drawText("SKILL 1", width * 0.28f, height * 0.84f, paint)

        val frame = CapturedFrame(
            bitmap = bitmap,
            width = width,
            height = height,
            timestamp = System.currentTimeMillis(),
            sequenceNumber = ++frameCounter
        )

        _latestFrame.value = frame
        _metrics.value = _metrics.value.copy(
            isCapturing = true,
            totalFramesCaptured = frameCounter,
            captureWidth = width,
            captureHeight = height
        )

        return frame
    }
}
