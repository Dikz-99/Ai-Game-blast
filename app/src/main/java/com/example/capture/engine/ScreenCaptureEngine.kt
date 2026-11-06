package com.example.capture.engine

import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.Image
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.os.Handler
import android.os.HandlerThread
import com.example.capture.model.CapturedFrame
import com.example.capture.pipeline.ScreenCapturePipeline
import com.example.vision.engine.UnifiedVisionEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.nio.ByteBuffer

class ScreenCaptureEngine(
    private val mediaProjection: MediaProjection,
    private val screenWidth: Int,
    private val screenHeight: Int,
    private val screenDensityDpi: Int,
    private var targetFps: Int = 10
) {
    private var virtualDisplay: VirtualDisplay? = null
    private var imageReader: ImageReader? = null
    private var backgroundThread: HandlerThread? = null
    private var backgroundHandler: Handler? = null

    // Downscaled analysis resolution (clamped to max 720p width for high OCR accuracy and low CPU)
    val captureWidth: Int
    val captureHeight: Int

    init {
        val scale = if (screenWidth > 720) 720f / screenWidth else 1.0f
        captureWidth = (screenWidth * scale).toInt()
        captureHeight = (screenHeight * scale).toInt()
    }

    private var lastFrameTime = 0L

    fun start() {
        backgroundThread = HandlerThread("ScreenCaptureThread").apply { start() }
        backgroundHandler = Handler(backgroundThread!!.looper)

        imageReader = ImageReader.newInstance(
            captureWidth,
            captureHeight,
            PixelFormat.RGBA_8888,
            2
        )

        virtualDisplay = mediaProjection.createVirtualDisplay(
            "GameBlastCapture",
            captureWidth,
            captureHeight,
            screenDensityDpi,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            imageReader!!.surface,
            null,
            backgroundHandler
        )

        ScreenCapturePipeline.updateTargetFps(targetFps)
        ScreenCapturePipeline.updateDisplayConfiguration(captureWidth, captureHeight, screenDensityDpi)

        imageReader!!.setOnImageAvailableListener({ reader ->
            val now = System.currentTimeMillis()
            val minInterval = 1000L / targetFps

            // Throttle frame processing according to configured target FPS
            if (now - lastFrameTime < minInterval) {
                val image = reader.acquireLatestImage()
                image?.close()
                ScreenCapturePipeline.recordDroppedFrame()
                return@setOnImageAvailableListener
            }

            var image: Image? = null
            try {
                val startTime = System.currentTimeMillis()
                image = reader.acquireLatestImage() ?: return@setOnImageAvailableListener
                lastFrameTime = now

                val planes = image.planes
                val buffer: ByteBuffer = planes[0].buffer
                val pixelStride = planes[0].pixelStride
                val rowStride = planes[0].rowStride
                val rowPadding = rowStride - pixelStride * captureWidth

                val bitmap = Bitmap.createBitmap(
                    captureWidth + rowPadding / pixelStride,
                    captureHeight,
                    Bitmap.Config.ARGB_8888
                )
                bitmap.copyPixelsFromBuffer(buffer)

                val finalBitmap = if (rowPadding > 0) {
                    val cropped = Bitmap.createBitmap(bitmap, 0, 0, captureWidth, captureHeight)
                    bitmap.recycle()
                    cropped
                } else {
                    bitmap
                }

                val latency = System.currentTimeMillis() - startTime
                ScreenCapturePipeline.publishFrame(finalBitmap, captureWidth, captureHeight, latency)

                CoroutineScope(Dispatchers.Default).launch {
                    UnifiedVisionEngine.processFrame(
                        CapturedFrame(
                            bitmap = finalBitmap,
                            width = captureWidth,
                            height = captureHeight,
                            timestamp = now
                        )
                    )
                }

            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                image?.close()
            }
        }, backgroundHandler)
    }

    fun stop() {
        virtualDisplay?.release()
        virtualDisplay = null
        imageReader?.close()
        imageReader = null
        backgroundThread?.quitSafely()
        backgroundThread = null
        backgroundHandler = null
        ScreenCapturePipeline.reset()
    }
}
