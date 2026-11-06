package com.example.automation.path

import android.graphics.Path
import android.graphics.PointF
import java.util.Random
import kotlin.math.sqrt

object HumanizedPathGenerator {

    private val random = Random()

    /**
     * Generates a touch point with natural human micro-jitter (±1.5 to 3.5 px).
     */
    fun generateJitteredPoint(x: Float, y: Float, maxJitter: Float = 3.5f): PointF {
        val jitterX = x + (random.nextFloat() * (maxJitter * 2) - maxJitter)
        val jitterY = y + (random.nextFloat() * (maxJitter * 2) - maxJitter)
        return PointF(jitterX, jitterY)
    }

    /**
     * Builds a tap Path at the target point, optionally jittered.
     */
    fun createTapPath(x: Float, y: Float, enableJitter: Boolean = true): Path {
        val pt = if (enableJitter) generateJitteredPoint(x, y) else PointF(x, y)
        return Path().apply {
            moveTo(pt.x, pt.y)
        }
    }

    /**
     * Builds a humanized curved swipe path using a cubic Bézier curve.
     * Human fingers naturally move in subtle arcs rather than mathematical straight lines.
     */
    fun createCurvedSwipePath(
        startX: Float,
        startY: Float,
        endX: Float,
        endY: Float,
        curveDeviation: Float = 0.05f
    ): Path {
        val path = Path()

        // Add micro-jitter to start and end
        val startPt = generateJitteredPoint(startX, startY, 2f)
        val endPt = generateJitteredPoint(endX, endY, 2f)

        path.moveTo(startPt.x, startPt.y)

        val dx = endPt.x - startPt.x
        val dy = endPt.y - startPt.y
        val distance = sqrt(dx * dx + dy * dy).coerceAtLeast(1f)

        // Perpendicular vector for the natural thumb arc
        val perpX = -dy / distance
        val perpY = dx / distance

        // Dynamic curvature offset (randomized arc direction and depth)
        val arcMagnitude = distance * curveDeviation * (0.8f + random.nextFloat() * 0.4f)
        val arcSign = if (random.nextBoolean()) 1f else -1f

        // Control point 1 (approx 35% along the trajectory)
        val c1X = startPt.x + dx * 0.35f + perpX * arcMagnitude * arcSign
        val c1Y = startPt.y + dy * 0.35f + perpY * arcMagnitude * arcSign

        // Control point 2 (approx 70% along the trajectory)
        val c2X = startPt.x + dx * 0.70f + perpX * (arcMagnitude * 0.8f) * arcSign
        val c2Y = startPt.y + dy * 0.70f + perpY * (arcMagnitude * 0.8f) * arcSign

        path.cubicTo(c1X, c1Y, c2X, c2Y, endPt.x, endPt.y)

        return path
    }
}
