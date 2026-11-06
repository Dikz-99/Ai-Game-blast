package com.example.ui.screens.aimonitor

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Gesture
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.automation.model.MacroSequence
import com.example.capture.model.CaptureMetrics
import com.example.capture.model.CapturedFrame
import com.example.capture.service.ScreenCaptureService
import com.example.core.model.GameState
import com.example.core.model.VisualBoundingBox
import com.example.core.safety.SafetyController
import com.example.data.local.entity.GameProfile
import com.example.ui.components.CyberTopAppBar
import com.example.ui.components.FuturisticCard
import com.example.ui.components.MetricItem
import com.example.ui.components.NeonButton
import com.example.ui.components.StatusBadge
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberDark
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.CyberSurfaceBorder
import com.example.ui.theme.CyberSurfaceVariant
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonRed
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.decision.model.DecisionProposal
import com.example.decision.model.DecisionState
import com.example.ui.theme.WarningAmber
import com.example.vision.model.VisionResult

@Composable
fun AiMonitorScreen(
    activeProfile: GameProfile?,
    metrics: CaptureMetrics,
    capturedFrame: CapturedFrame?,
    visionResult: VisionResult,
    ocrLogs: List<String>,
    actionLogs: List<String>,
    isExecutingMacro: Boolean,
    macroProgress: Pair<Int, Int>,
    presetMacros: List<MacroSequence>,
    isAutoPilotActive: Boolean = false,
    decisionState: DecisionState = DecisionState.IDLE,
    latestProposal: DecisionProposal? = null,
    decisionLogs: List<String> = emptyList(),
    totalDecisions: Long = 0L,
    successfulDecisions: Long = 0L,
    strategicRecommendation: com.example.strategy.model.StrategyRecommendation? = null,
    isAnalyzingStrategy: Boolean = false,
    strategySource: com.example.strategy.model.StrategySource = com.example.strategy.model.StrategySource.LOCAL_HEURISTIC,
    totalTransitionsEvaluated: Long = 0L,
    weightAdjustmentsCount: Long = 0L,
    latestLearningRecord: com.example.learning.model.LearningUpdateRecord? = null,
    learningFeed: List<String> = emptyList(),
    onBackClick: () -> Unit,
    onGenerateTestFrame: () -> Unit,
    onExecuteTap: (Float, Float) -> Unit,
    onExecuteSwipe: (Float, Float, Float, Float) -> Unit,
    onExecuteLongPress: (Float, Float) -> Unit,
    onExecuteMacro: (MacroSequence) -> Unit,
    onToggleAutoPilot: () -> Unit = {},
    onStepDecision: () -> Unit = {},
    onRequestStrategicAdvice: () -> Unit = {},
    onTriggerLearningEvaluation: () -> Unit = {}
) {
    val gameState by ScreenCaptureService.currentGameState.collectAsState()
    val isEmergencyStopped by SafetyController.isEmergencyStopped.collectAsState()
    var isStreamPaused by remember { mutableStateOf(false) }

    val activeBoxes = if (visionResult.boundingBoxes.isNotEmpty()) {
        visionResult.boundingBoxes
    } else {
        listOf(
            VisualBoundingBox("b1", "ATTACK_BUTTON", 0.94f, 0.60f, 0.78f, 0.92f, 0.88f, "ATTACK"),
            VisualBoundingBox("b2", "BOSS_HEALTH_BAR", 0.98f, 0.05f, 0.10f, 0.95f, 0.18f, "BOSS HP"),
            VisualBoundingBox("b3", "SKILL_COOLDOWN", 0.89f, 0.20f, 0.78f, 0.52f, 0.88f, "SKILL 1")
        )
    }

    val infiniteTransition = rememberInfiniteTransition(label = "radar_scan")
    val scanLineY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scan_y"
    )

    Scaffold(
        containerColor = CyberDark,
        topBar = {
            CyberTopAppBar(
                title = "AI VISION & AUTOMATION MONITOR",
                onBackClick = onBackClick,
                actions = {
                    StatusBadge(
                        state = if (isEmergencyStopped) GameState.EMERGENCY_STOPPED else gameState
                    )
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Screen Capture Telemetry Card
            item {
                FuturisticCard(
                    borderColor = CyberCyan,
                    testTag = "capture_telemetry_card"
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "SCREEN CAPTURE TELEMETRY",
                                color = CyberCyan,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = if (metrics.isCapturing) "● STREAM ACTIVE" else "○ READY / STANDBY",
                                color = if (metrics.isCapturing) NeonGreen else TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            MetricItem(
                                label = "CAPTURE FPS",
                                value = if (metrics.currentFps > 0) String.format("%.1f", metrics.currentFps) else "${activeProfile?.targetFps ?: 10}",
                                unit = "fps",
                                accentColor = NeonGreen,
                                icon = Icons.Default.Speed,
                                modifier = Modifier.weight(1f)
                            )
                            MetricItem(
                                label = "LATENCY",
                                value = "${metrics.captureLatencyMs}",
                                unit = "ms",
                                accentColor = CyberCyan,
                                icon = Icons.Default.Bolt,
                                modifier = Modifier.weight(1f)
                            )
                            MetricItem(
                                label = "DROPPED",
                                value = "${metrics.droppedFrames}",
                                unit = "frames",
                                accentColor = WarningAmber,
                                icon = Icons.Default.Layers,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // 2. Visual Radar Canvas & Frame Inspector
            item {
                FuturisticCard(
                    borderColor = CyberCyan,
                    glowColor = CyberCyan,
                    testTag = "visual_radar_card"
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Radar, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "FRAME BUFFER INSPECTOR",
                                    color = CyberCyan,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                            }
                            Text(
                                text = if (metrics.captureWidth > 0) "${metrics.captureWidth}x${metrics.captureHeight}" else "720x1280",
                                color = TextSecondary,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Spacer(Modifier.height(12.dp))

                        // Radar Frame Display
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(230.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF07090F))
                                .border(1.dp, CyberSurfaceBorder, RoundedCornerShape(12.dp))
                        ) {
                            // If live or synthetic captured frame is present, render it
                            if (capturedFrame != null) {
                                Image(
                                    bitmap = capturedFrame.bitmap.asImageBitmap(),
                                    contentDescription = "Live Captured Frame",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.FillBounds
                                )
                            }

                            // Overlay canvas with grid lines, bounding boxes, and scan line
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val w = size.width
                                val h = size.height

                                // Draw grid lines if no frame
                                if (capturedFrame == null) {
                                    val gridColor = CyberSurfaceBorder.copy(alpha = 0.4f)
                                    for (i in 1..4) {
                                        drawLine(gridColor, Offset(w * i / 5f, 0f), Offset(w * i / 5f, h), 1f)
                                        drawLine(gridColor, Offset(0f, h * i / 5f), Offset(w, h * i / 5f), 1f)
                                    }
                                }

                                // Draw bounding boxes
                                activeBoxes.forEach { box ->
                                    val left = box.left * w
                                    val top = box.top * h
                                    val right = box.right * w
                                    val bottom = box.bottom * h

                                    val color = when {
                                        box.label.contains("ATTACK") -> NeonGreen
                                        box.label.contains("HEALTH") || box.label.contains("HP") -> NeonRed
                                        box.label.contains("CLAIM") || box.label.contains("VICTORY") -> WarningAmber
                                        box.label.contains("SKILL") -> CyberCyan
                                        else -> CyberCyan
                                    }

                                    drawRect(
                                        color = color.copy(alpha = 0.18f),
                                        topLeft = Offset(left, top),
                                        size = Size(right - left, bottom - top)
                                    )
                                    drawRect(
                                        color = color,
                                        topLeft = Offset(left, top),
                                        size = Size(right - left, bottom - top),
                                        style = Stroke(width = 2.5f)
                                    )
                                }

                                // Draw animated scan line
                                if (!isStreamPaused) {
                                    val currentScanY = scanLineY * h
                                    drawLine(
                                        color = CyberCyan.copy(alpha = 0.85f),
                                        start = Offset(0f, currentScanY),
                                        end = Offset(w, currentScanY),
                                        strokeWidth = 2.5f
                                    )
                                }
                            }

                            // Frame overlay labels
                            Text(
                                text = "FRAME #${capturedFrame?.sequenceNumber ?: metrics.totalFramesCaptured} • SCENE: ${visionResult.scene.displayName} (${(visionResult.confidence * 100).toInt()}%) • ${visionResult.processingTimeMs}ms",
                                color = TextPrimary,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .background(Color(0xCC08090E))
                                    .padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }

                        Spacer(Modifier.height(14.dp))

                        // Controls
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            NeonButton(
                                text = if (isStreamPaused) "RESUME" else "PAUSE",
                                icon = if (isStreamPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                                isOutlined = true,
                                color = CyberCyan,
                                onClick = { isStreamPaused = !isStreamPaused },
                                modifier = Modifier.weight(1f),
                                testTag = "toggle_monitor_stream_button"
                            )

                            NeonButton(
                                text = "TEST FRAME",
                                icon = Icons.Default.Camera,
                                color = NeonGreen,
                                onClick = onGenerateTestFrame,
                                modifier = Modifier.weight(1f),
                                testTag = "generate_test_frame_button"
                            )
                        }
                    }
                }
            }

            // 3. Gesture Automation & Macro Controller
            item {
                Text(
                    text = "AUTOMATION ENGINE & GESTURE DISPATCHER",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(Modifier.height(8.dp))
                FuturisticCard(
                    borderColor = NeonRed.copy(alpha = 0.6f),
                    testTag = "automation_dispatcher_card"
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Gesture, contentDescription = null, tint = NeonRed, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "HUMANIZED GESTURE CONTROLS",
                                    color = NeonRed,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                            }
                            Text(
                                text = "JITTER ACTIVE (±3px)",
                                color = NeonGreen,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(Modifier.height(12.dp))

                        // Gesture Trigger Buttons: Tap, Curved Swipe, Long Press
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            NeonButton(
                                text = "TAP",
                                icon = Icons.Default.TouchApp,
                                color = CyberCyan,
                                isOutlined = true,
                                onClick = { onExecuteTap(540f, 1850f) },
                                modifier = Modifier.weight(1f),
                                testTag = "trigger_tap_button"
                            )
                            NeonButton(
                                text = "SWIPE",
                                icon = Icons.Default.Gesture,
                                color = WarningAmber,
                                isOutlined = true,
                                onClick = { onExecuteSwipe(360f, 1300f, 720f, 1300f) },
                                modifier = Modifier.weight(1f),
                                testTag = "trigger_swipe_button"
                            )
                            NeonButton(
                                text = "LONG PRESS",
                                icon = Icons.Default.Bolt,
                                color = NeonRed,
                                isOutlined = true,
                                onClick = { onExecuteLongPress(540f, 1200f) },
                                modifier = Modifier.weight(1.1f),
                                testTag = "trigger_long_press_button"
                            )
                        }

                        Spacer(Modifier.height(16.dp))

                        // Preset Macro Sequences
                        Text(
                            text = "PRESET GAMING MACROS",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )

                        Spacer(Modifier.height(8.dp))

                        presetMacros.forEach { macro ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(CyberSurfaceVariant)
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = macro.name,
                                        color = TextPrimary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${macro.actions.size} steps: ${macro.description}",
                                        color = TextSecondary,
                                        fontSize = 10.sp,
                                        maxLines = 1
                                    )
                                }
                                Spacer(Modifier.width(8.dp))
                                NeonButton(
                                    text = "RUN",
                                    icon = Icons.Default.PlayArrow,
                                    color = CyberCyan,
                                    enabled = !isExecutingMacro,
                                    onClick = { onExecuteMacro(macro) },
                                    modifier = Modifier.height(36.dp),
                                    testTag = "run_macro_${macro.id}"
                                )
                            }
                        }

                        if (isExecutingMacro) {
                            Spacer(Modifier.height(10.dp))
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Executing Macro Sequence...", color = CyberCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Text("Step ${macroProgress.first} of ${macroProgress.second}", color = NeonGreen, fontSize = 11.sp)
                                }
                                Spacer(Modifier.height(4.dp))
                                LinearProgressIndicator(
                                    progress = {
                                        if (macroProgress.second > 0) macroProgress.first.toFloat() / macroProgress.second.toFloat() else 0f
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(4.dp)
                                        .clip(RoundedCornerShape(2.dp)),
                                    color = NeonRed,
                                    trackColor = CyberSurfaceBorder
                                )
                            }
                        }
                    }
                }
            }

            // 3.5. Phase 5: Autonomous Decision Engine & Strategy Core
            item {
                Text(
                    text = "AUTONOMOUS DECISION CORE (STATE MACHINE)",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(Modifier.height(8.dp))
                FuturisticCard(testTag = "decision_engine_card") {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        // Header with State Indicator
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Radar,
                                    contentDescription = "AI Brain",
                                    tint = CyberCyan,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "REASONING ENGINE",
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }

                            val stateBadgeColor = when (decisionState) {
                                DecisionState.EMERGENCY_HALTED -> NeonRed
                                DecisionState.DISPATCHING -> NeonGreen
                                DecisionState.EVALUATING -> CyberCyan
                                DecisionState.COOLDOWN -> WarningAmber
                                DecisionState.OBSERVING -> CyberCyan
                                DecisionState.IDLE -> TextSecondary
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(stateBadgeColor.copy(alpha = 0.15f))
                                    .border(1.dp, stateBadgeColor, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = decisionState.label.uppercase(),
                                    color = stateBadgeColor,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(Modifier.height(14.dp))

                        // Controls: Autopilot toggle & Step 1-Cycle button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            NeonButton(
                                text = if (isAutoPilotActive) "STOP AUTOPILOT" else "START AUTOPILOT",
                                icon = if (isAutoPilotActive) Icons.Default.Pause else Icons.Default.PlayArrow,
                                color = if (isAutoPilotActive) NeonRed else NeonGreen,
                                onClick = onToggleAutoPilot,
                                modifier = Modifier.weight(1.3f),
                                testTag = "toggle_autopilot_button"
                            )

                            NeonButton(
                                text = "STEP BRAIN",
                                icon = Icons.Default.Bolt,
                                color = CyberCyan,
                                isOutlined = true,
                                enabled = !isAutoPilotActive,
                                onClick = onStepDecision,
                                modifier = Modifier.weight(1f),
                                testTag = "step_decision_button"
                            )
                        }

                        Spacer(Modifier.height(14.dp))

                        // Statistics row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "$totalDecisions", color = CyberCyan, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text(text = "DECISIONS", color = TextSecondary, fontSize = 9.sp)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "$successfulDecisions", color = NeonGreen, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text(text = "DISPATCHED", color = TextSecondary, fontSize = 9.sp)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "${latestProposal?.rulePriority ?: 0}", color = WarningAmber, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text(text = "PRIORITY", color = TextSecondary, fontSize = 9.sp)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                val conf = ((latestProposal?.confidence ?: 0f) * 100).toInt()
                                Text(text = "$conf%", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text(text = "CONFIDENCE", color = TextSecondary, fontSize = 9.sp)
                            }
                        }

                        Spacer(Modifier.height(14.dp))

                        // Latest Proposal details
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(CyberSurfaceVariant)
                                .border(1.dp, CyberSurfaceBorder, RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "TARGET SCENE: ${latestProposal?.scene?.name ?: "IDLE / STANDBY"}",
                                        color = CyberCyan,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "ACTION: ${latestProposal?.action?.label ?: "Waiting"}",
                                        color = if (latestProposal?.action != null) NeonGreen else TextSecondary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    text = latestProposal?.explanation
                                        ?: "AI Decision Engine waiting for frame input. Tap 'STEP BRAIN' or 'START AUTOPILOT'.",
                                    color = TextPrimary,
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp
                                )
                            }
                        }

                        // Decision Log Snippet (Last 3 entries)
                        if (decisionLogs.isNotEmpty()) {
                            Spacer(Modifier.height(10.dp))
                            Text(
                                text = "RECENT DECISION FEED",
                                color = TextSecondary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(Modifier.height(4.dp))
                            decisionLogs.takeLast(3).reversed().forEach { log ->
                                Text(
                                    text = log,
                                    color = if (log.contains("BLOCKED") || log.contains("SAFETY")) NeonRed else TextSecondary,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    maxLines = 1,
                                    modifier = Modifier.padding(vertical = 1.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 3.8. Phase 7: Hybrid Strategic Advisor (Local + Cloud Gateway)
            item {
                Text(
                    text = "HYBRID AI STRATEGIC ADVISOR",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(Modifier.height(8.dp))
                FuturisticCard(testTag = "strategic_advisor_card") {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Radar,
                                    contentDescription = "Tactical Advisor",
                                    tint = CyberCyan,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "METAGAME TACTICIAN",
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }

                            val sourceBadgeColor = when (strategySource) {
                                com.example.strategy.model.StrategySource.CLOUD_GEMINI -> CyberCyan
                                com.example.strategy.model.StrategySource.LOCAL_HEURISTIC -> NeonGreen
                                com.example.strategy.model.StrategySource.CACHED_MEMORY -> WarningAmber
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(sourceBadgeColor.copy(alpha = 0.15f))
                                    .border(1.dp, sourceBadgeColor, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = strategySource.label.uppercase(),
                                    color = sourceBadgeColor,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(Modifier.height(14.dp))

                        NeonButton(
                            text = if (isAnalyzingStrategy) "ANALYZING GAME STATE..." else "GENERATE STRATEGIC ADVICE",
                            icon = Icons.Default.Bolt,
                            color = CyberCyan,
                            enabled = !isAnalyzingStrategy,
                            onClick = onRequestStrategicAdvice,
                            modifier = Modifier.fillMaxWidth(),
                            testTag = "request_strategy_advice_button"
                        )

                        Spacer(Modifier.height(14.dp))

                        if (strategicRecommendation != null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(CyberSurfaceVariant)
                                    .border(1.dp, CyberSurfaceBorder, RoundedCornerShape(8.dp))
                                    .padding(12.dp)
                            ) {
                                Column {
                                    Text(
                                        text = strategicRecommendation.title,
                                        color = CyberCyan,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(Modifier.height(6.dp))
                                    Text(
                                        text = strategicRecommendation.analysis,
                                        color = TextPrimary,
                                        fontSize = 12.sp,
                                        lineHeight = 16.sp
                                    )

                                    if (strategicRecommendation.tacticalTips.isNotEmpty()) {
                                        Spacer(Modifier.height(10.dp))
                                        Text(
                                            text = "KEY TACTICAL POINTERS:",
                                            color = TextSecondary,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(Modifier.height(4.dp))
                                        strategicRecommendation.tacticalTips.forEach { tip ->
                                            Row(modifier = Modifier.padding(vertical = 2.dp)) {
                                                Text(text = "• ", color = NeonGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                Text(text = tip, color = TextPrimary, fontSize = 11.sp)
                                            }
                                        }
                                    }

                                    strategicRecommendation.suggestedMacroName?.let { macroName ->
                                        Spacer(Modifier.height(10.dp))
                                        val matchingMacro = presetMacros.firstOrNull { it.name.contains(macroName, ignoreCase = true) }
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "Recommended: $macroName",
                                                color = WarningAmber,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            if (matchingMacro != null) {
                                                NeonButton(
                                                    text = "RUN MACRO",
                                                    icon = Icons.Default.PlayArrow,
                                                    color = NeonGreen,
                                                    onClick = { onExecuteMacro(matchingMacro) },
                                                    modifier = Modifier.height(32.dp),
                                                    testTag = "run_advised_macro_button"
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            Text(
                                text = "Tap 'GENERATE STRATEGIC ADVICE' to synthesize real-time game telemetry with Neural Heuristics / Cloud Gemini 3.8 Flash.",
                                color = TextSecondary,
                                fontSize = 11.sp,
                                lineHeight = 15.sp
                            )
                        }
                    }
                }
            }

            // 3.9. Phase 8: Adaptive Strategy Memory & Reinforcement Learning
            item {
                Text(
                    text = "ADAPTIVE STRATEGY MEMORY & REINFORCEMENT",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(Modifier.height(8.dp))
                FuturisticCard(testTag = "learning_engine_card") {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Layers,
                                    contentDescription = "Learning Engine",
                                    tint = NeonGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "DYNAMIC WEIGHT EVOLUTION",
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }

                            val outcomeColor = when (latestLearningRecord?.outcome) {
                                com.example.learning.model.TransitionOutcome.STAGE_CLEARED,
                                com.example.learning.model.TransitionOutcome.OBJECTIVE_ADVANCED -> NeonGreen
                                com.example.learning.model.TransitionOutcome.DEFEAT -> NeonRed
                                else -> CyberCyan
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(outcomeColor.copy(alpha = 0.15f))
                                    .border(1.dp, outcomeColor, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = (latestLearningRecord?.outcome?.name ?: "ACTIVE").uppercase(),
                                    color = outcomeColor,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(Modifier.height(14.dp))

                        // Statistics row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "$totalTransitionsEvaluated", color = CyberCyan, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text(text = "TRANSITIONS", color = TextSecondary, fontSize = 9.sp)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "$weightAdjustmentsCount", color = NeonGreen, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text(text = "ADJUSTMENTS", color = TextSecondary, fontSize = 9.sp)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                val deltaStr = latestLearningRecord?.let { String.format("%+.2f", it.weightDelta) } ?: "+0.00"
                                Text(text = deltaStr, color = WarningAmber, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text(text = "LATEST DELTA", color = TextSecondary, fontSize = 9.sp)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                val weightStr = latestLearningRecord?.let { String.format("%.2f", it.newWeight) } ?: "1.00"
                                Text(text = weightStr, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text(text = "CURR WEIGHT", color = TextSecondary, fontSize = 9.sp)
                            }
                        }

                        Spacer(Modifier.height(14.dp))

                        // Latest learning record details
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(CyberSurfaceVariant)
                                .border(1.dp, CyberSurfaceBorder, RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Column {
                                Text(
                                    text = "TARGET: ${latestLearningRecord?.stateSignature ?: "Awaiting game transitions"}",
                                    color = CyberCyan,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = latestLearningRecord?.notes ?: "Autonomous learning engine tracks action outcomes and adjusts priority weights dynamically between 0.10 and 3.00.",
                                    color = TextPrimary,
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp
                                )
                            }
                        }

                        Spacer(Modifier.height(14.dp))

                        NeonButton(
                            text = "SIMULATE TRANSITION EVALUATION",
                            icon = Icons.Default.Bolt,
                            color = NeonGreen,
                            onClick = onTriggerLearningEvaluation,
                            modifier = Modifier.fillMaxWidth(),
                            testTag = "simulate_learning_evaluation_button"
                        )

                        if (learningFeed.isNotEmpty()) {
                            Spacer(Modifier.height(10.dp))
                            Text(
                                text = "RECENT LEARNING STREAM",
                                color = TextSecondary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(Modifier.height(4.dp))
                            learningFeed.takeLast(3).reversed().forEach { feedItem ->
                                Text(
                                    text = feedItem,
                                    color = if (feedItem.contains("DEFEAT")) NeonRed else NeonGreen,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    maxLines = 1,
                                    modifier = Modifier.padding(vertical = 1.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 4. Real-Time OCR Text Stream Log
            item {
                Text(
                    text = "REAL-TIME OCR & PIPELINE LOG",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(Modifier.height(8.dp))
                FuturisticCard(testTag = "ocr_log_card") {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                    ) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(ocrLogs) { log ->
                                val color = when {
                                    log.contains("[DECISION") -> CyberCyan
                                    log.contains("[SAFETY") -> NeonGreen
                                    log.contains("[GESTURE") -> WarningAmber
                                    log.contains("[FRAME") -> NeonGreen
                                    else -> TextPrimary
                                }
                                Text(
                                    text = log,
                                    color = color,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }

            // 5. Gesture Dispatch History Log
            item {
                Text(
                    text = "AUTOMATION GESTURE DISPATCH HISTORY",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(Modifier.height(8.dp))
                FuturisticCard(testTag = "action_log_card") {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                    ) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(actionLogs) { log ->
                                val color = when {
                                    log.contains("SUCCESS") -> NeonGreen
                                    log.contains("BLOCKED") || log.contains("ABORTED") -> NeonRed
                                    log.contains("[MACRO") -> CyberCyan
                                    else -> TextSecondary
                                }
                                Text(
                                    text = log,
                                    color = color,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
