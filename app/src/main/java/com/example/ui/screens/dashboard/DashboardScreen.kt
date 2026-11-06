package com.example.ui.screens.dashboard

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.capture.service.ScreenCaptureService
import com.example.core.model.GameState
import com.example.core.permission.PermissionStatus
import com.example.core.safety.SafetyController
import com.example.data.local.entity.GameProfile
import com.example.data.local.entity.GameSession
import com.example.ui.components.FuturisticCard
import com.example.ui.components.MetricItem
import com.example.ui.components.NeonButton
import com.example.ui.components.StatusBadge
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberCyanGlow
import com.example.ui.theme.CyberDark
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.CyberSurfaceBorder
import com.example.ui.theme.CyberSurfaceVariant
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonRed
import com.example.ui.theme.NeonRedGlow
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningAmber

@Composable
fun DashboardScreen(
    activeProfile: GameProfile?,
    latestSession: GameSession?,
    permissionStatus: PermissionStatus,
    onStartAiClick: () -> Unit,
    onStopAiClick: () -> Unit,
    onEmergencyStopClick: () -> Unit,
    onNavigateToGameManager: () -> Unit,
    onNavigateToMonitor: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToFloatingSettings: () -> Unit,
    onGrantPermissionsClick: () -> Unit
) {
    val gameState by ScreenCaptureService.currentGameState.collectAsState()
    val isEmergencyStopped by SafetyController.isEmergencyStopped.collectAsState()
    val safetyInterventions by SafetyController.safetyInterventionCount.collectAsState()

    Scaffold(
        containerColor = CyberDark,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    isEmergencyStopped -> NeonRed
                                    gameState == GameState.EXECUTING -> NeonGreen
                                    gameState == GameState.SCANNING -> CyberCyan
                                    else -> TextSecondary
                                }
                            )
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = "AI GAME BLAST",
                        color = TextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                }

                // Header Emergency Kill Switch
                IconButton(
                    onClick = onEmergencyStopClick,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(NeonRed.copy(alpha = 0.15f))
                        .border(1.dp, NeonRed, CircleShape)
                        .testTag("dashboard_header_kill_switch")
                ) {
                    Icon(
                        Icons.Default.Stop,
                        contentDescription = "Emergency Kill Switch",
                        tint = NeonRed,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Permission Warning Banner if not all essential granted
            if (!permissionStatus.isAllEssentialGranted) {
                item {
                    FuturisticCard(
                        borderColor = WarningAmber,
                        glowColor = WarningAmber,
                        testTag = "permission_warning_card"
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = null,
                                tint = WarningAmber,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "SYSTEM PERMISSIONS REQUIRED",
                                    color = WarningAmber,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = "Accessibility & Overlay permissions are needed for gesture automation & HUD.",
                                    color = TextSecondary,
                                    fontSize = 12.sp
                                )
                            }
                            Spacer(Modifier.width(8.dp))
                            Button(
                                onClick = onGrantPermissionsClick,
                                colors = ButtonDefaults.buttonColors(containerColor = WarningAmber),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("grant_permissions_button")
                            ) {
                                Text("GRANT", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            // 2. Main AI Status Card
            item {
                FuturisticCard(
                    borderColor = if (isEmergencyStopped) NeonRed else CyberCyan,
                    glowColor = if (isEmergencyStopped) NeonRedGlow else CyberCyanGlow,
                    testTag = "ai_status_card"
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "CORE AI ENGINE",
                                color = TextSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 1.sp
                            )
                            StatusBadge(
                                state = if (isEmergencyStopped) GameState.EMERGENCY_STOPPED else gameState
                            )
                        }

                        Spacer(Modifier.height(16.dp))

                        // Active Game Profile display
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(CyberSurfaceVariant)
                                .padding(12.dp)
                        ) {
                            Icon(
                                Icons.Default.Gamepad,
                                contentDescription = null,
                                tint = CyberCyan,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = activeProfile?.name ?: "No Game Profile Active",
                                    color = TextPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = activeProfile?.genre ?: "Tap to choose game",
                                    color = TextSecondary,
                                    fontSize = 12.sp
                                )
                            }
                            NeonButton(
                                text = "SWITCH",
                                onClick = onNavigateToGameManager,
                                isOutlined = true,
                                color = CyberCyan,
                                testTag = "switch_profile_button",
                                modifier = Modifier.height(36.dp)
                            )
                        }

                        Spacer(Modifier.height(20.dp))

                        // Main Action Controls: Start / Stop / Kill
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            if (gameState == GameState.EXECUTING || gameState == GameState.SCANNING) {
                                NeonButton(
                                    text = "STOP AI",
                                    icon = Icons.Default.Stop,
                                    color = WarningAmber,
                                    onClick = onStopAiClick,
                                    modifier = Modifier.weight(1f),
                                    testTag = "stop_ai_button"
                                )
                            } else {
                                NeonButton(
                                    text = if (isEmergencyStopped) "RESET & START" else "START AI",
                                    icon = Icons.Default.PlayArrow,
                                    color = if (isEmergencyStopped) NeonRed else CyberCyan,
                                    onClick = onStartAiClick,
                                    modifier = Modifier.weight(1f),
                                    testTag = "start_ai_button"
                                )
                            }

                            NeonButton(
                                text = "KILL SWITCH",
                                icon = Icons.Default.Bolt,
                                color = NeonRed,
                                onClick = onEmergencyStopClick,
                                modifier = Modifier.weight(0.9f),
                                testTag = "dashboard_emergency_kill_button"
                            )
                        }
                    }
                }
            }

            // 3. Telemetry Metrics HUD
            item {
                Text(
                    text = "REAL-TIME TELEMETRY",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricItem(
                        label = "TARGET FPS",
                        value = "${activeProfile?.targetFps ?: 10}",
                        unit = "fps",
                        accentColor = CyberCyan,
                        icon = Icons.Default.Speed,
                        modifier = Modifier.weight(1f)
                    )
                    MetricItem(
                        label = "MAX TAPS",
                        value = "${activeProfile?.maxTapsPerSecond ?: 4}",
                        unit = "/sec",
                        accentColor = WarningAmber,
                        icon = Icons.Default.Bolt,
                        modifier = Modifier.weight(1f)
                    )
                    MetricItem(
                        label = "SAFETY HALTS",
                        value = "$safetyInterventions",
                        unit = "events",
                        accentColor = NeonRed,
                        icon = Icons.Default.Security,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // 4. Quick Access Hub Grid
            item {
                Text(
                    text = "CONTROL MODULES",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        QuickAccessCard(
                            title = "Game Manager",
                            subtitle = "Profiles & Adapters",
                            icon = Icons.Default.Gamepad,
                            iconColor = CyberCyan,
                            onClick = onNavigateToGameManager,
                            modifier = Modifier.weight(1f),
                            testTag = "nav_game_manager_card"
                        )
                        QuickAccessCard(
                            title = "AI Monitor",
                            subtitle = "Visual Radar & OCR",
                            icon = Icons.Default.Visibility,
                            iconColor = NeonGreen,
                            onClick = onNavigateToMonitor,
                            modifier = Modifier.weight(1f),
                            testTag = "nav_ai_monitor_card"
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        QuickAccessCard(
                            title = "Floating HUD",
                            subtitle = "Overlay Controller",
                            icon = Icons.Default.Layers,
                            iconColor = CyberCyan,
                            onClick = onNavigateToFloatingSettings,
                            modifier = Modifier.weight(1f),
                            testTag = "nav_floating_hud_card"
                        )
                        QuickAccessCard(
                            title = "Session History",
                            subtitle = "Logs & Experience",
                            icon = Icons.Default.History,
                            iconColor = WarningAmber,
                            onClick = onNavigateToHistory,
                            modifier = Modifier.weight(1f),
                            testTag = "nav_session_history_card"
                        )
                    }

                    QuickAccessCard(
                        title = "AI Engine Settings",
                        subtitle = "Safety Limits, Rate Limiting, Cloud AI Opt-In",
                        icon = Icons.Default.Settings,
                        iconColor = TextPrimary,
                        onClick = onNavigateToSettings,
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "nav_settings_card"
                    )
                }
            }

            // 5. Recent Session Summary
            if (latestSession != null) {
                item {
                    Text(
                        text = "LAST SESSION SUMMARY",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    FuturisticCard(
                        testTag = "last_session_summary_card"
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = latestSession.profileName,
                                    color = TextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = latestSession.status,
                                    color = if (latestSession.status == "COMPLETED") NeonGreen else NeonRed,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = "Total Actions: ${latestSession.totalActions} (${latestSession.successfulActions} successful) • Latency: ${latestSession.averageLatencyMs}ms",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                            if (latestSession.summaryNotes.isNotEmpty()) {
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = latestSession.summaryNotes,
                                    color = CyberCyan,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun QuickAccessCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = "quick_access_card"
) {
    FuturisticCard(
        onClick = onClick,
        modifier = modifier,
        testTag = testTag
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(iconColor.copy(alpha = 0.12f))
                    .border(1.dp, iconColor.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
            ) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    color = TextSecondary,
                    fontSize = 11.sp,
                    maxLines = 1
                )
            }
        }
    }
}
