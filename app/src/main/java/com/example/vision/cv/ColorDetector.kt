package com.example.vision.cv

import android.graphics.Bitmap
import android.graphics.Color
import kotlin.math.abs

object ColorDetector {

    /**
     * Calculates the fill ratio of a horizontal bar (such as HP or Mana bar)
     * by scanning sampled points from left to right.
     * Returns a float from 0.0f to 1.0f.
     */
    fun calculateBarPercentage(
        bitmap: Bitmap,
        leftNorm: Float,
        topNorm: Float,
        rightNorm: Float,
        bottomNorm: Float,
        isBarColor: (r: Int, g: Int, b: Int) -> Boolean
    ): Float {
        val width = bitmap.width
        val height = bitmap.height

        val xStart = (leftNorm * width).toInt().coerceIn(0, width - 1)
        val xEnd = (rightNorm * width).toInt().coerceIn(0, width - 1)
        val yCenter = (((topNorm + bottomNorm) / 2f) * height).toInt().coerceIn(0, height - 1)

        if (xStart >= xEnd) return 0f

        val totalSamples = 30
        var filledSamples = 0
        val step = (xEnd - xStart).toFloat() / totalSamples

        for (i in 0 until totalSamples) {
            val sampleX = (xStart + (i * step)).toInt().coerceIn(0, width - 1)
            val pixel = bitmap.getPixel(sampleX, yCenter)
            val r = Color.red(pixel)
            val g = Color.green(pixel)
            val b = Color.blue(pixel)

            if (isBarColor(r, g, b)) {
                filledSamples++
            }
        }

        return filledSamples.toFloat() / totalSamples.toFloat()
    }

    /**
     * Checks if red health color dominates the given bar coordinates.
     */
    fun calculateHealthBarPercentage(
        bitmap: Bitmap,
        leftNorm: Float = 0.05f,
        topNorm: Float = 0.08f,
        rightNorm: Float = 0.95f,
        bottomNorm: Float = 0.16f
    ): Float {
        return calculateBarPercentage(bitmap, leftNorm, topNorm, rightNorm, bottomNorm) { r, g, b ->
            // High red relative to green & blue (HP bar) OR high green (Full HP)
            val isRedBar = (r > 130 && r > g * 1.3f && r > b * 1.3f)
            val isGreenBar = (g > 130 && g > r * 1.2f && g > b * 1.2f)
            isRedBar || isGreenBar
        }
    }

    /**
     * Computes the average brightness (0..255) of a Region of Interest.
     * Useful for detecting loading screens (near 0) or flash events.
     */
    fun calculateAverageLuminance(
        bitmap: Bitmap,
        leftNorm: Float = 0f,
        topNorm: Float = 0f,
        rightNorm: Float = 1f,
        bottomNorm: Float = 1f
    ): Float {
        val width = bitmap.width
        val height = bitmap.height

        val xStart = (leftNorm * width).toInt().coerceIn(0, width - 1)
        val xEnd = (rightNorm * width).toInt().coerceIn(0, width - 1)
        val yStart = (topNorm * height).toInt().coerceIn(0, height - 1)
        val yEnd = (bottomNorm * height).toInt().coerceIn(0, height - 1)

        var totalLuminance = 0L
        var sampleCount = 0

        val stepX = ((xEnd - xStart) / 10).coerceAtLeast(1)
        val stepY = ((yEnd - yStart) / 10).coerceAtLeast(1)

        for (x in xStart..xEnd step stepX) {
            for (y in yStart..yEnd step stepY) {
                val pixel = bitmap.getPixel(x, y)
                val r = Color.red(pixel)
                val g = Color.green(pixel)
                val b = Color.blue(pixel)
                // Standard ITU-R luminance formula
                val lum = (0.299f * r + 0.587f * g + 0.114f * b).toLong()
                totalLuminance += lum
                sampleCount++
            }
        }

        return if (sampleCount > 0) totalLuminance.toFloat() / sampleCount else 0f
    }
}
