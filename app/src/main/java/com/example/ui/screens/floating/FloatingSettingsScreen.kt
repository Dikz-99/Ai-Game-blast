package com.example.ui.screens.floating

import android.content.Context
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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.permission.PermissionManager
import com.example.core.permission.PermissionStatus
import com.example.ui.components.CyberTopAppBar
import com.example.ui.components.FuturisticCard
import com.example.ui.components.NeonButton
import com.example.ui.floating.FloatingAssistantService
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberDark
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.CyberSurfaceBorder
import com.example.ui.theme.CyberSurfaceVariant
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonRed
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningAmber

@Composable
fun FloatingSettingsScreen(
    permissionStatus: PermissionStatus,
    onBackClick: () -> Unit,
    onGrantOverlayClick: () -> Unit,
    onToggleFloatingService: (Boolean) -> Unit
) {
    val isFloatingActive by FloatingAssistantService.isFloatingVisible.collectAsState()

    Scaffold(
        containerColor = CyberDark,
        topBar = {
            CyberTopAppBar(
                title = "FLOATING ASSISTANT HUD",
                onBackClick = onBackClick
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
            // Overlay Permission status
            item {
                FuturisticCard(
                    borderColor = if (permissionStatus.hasOverlayPermission) NeonGreen else WarningAmber,
                    testTag = "overlay_permission_status_card"
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            if (permissionStatus.hasOverlayPermission) Icons.Default.CheckCircle else Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (permissionStatus.hasOverlayPermission) NeonGreen else WarningAmber,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (permissionStatus.hasOverlayPermission) "OVERLAY PERMISSION GRANTED" else "OVERLAY PERMISSION REQUIRED",
                                color = if (permissionStatus.hasOverlayPermission) NeonGreen else WarningAmber,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            Text(
                                text = "Required to display floating controls above target games.",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                        if (!permissionStatus.hasOverlayPermission) {
                            Spacer(Modifier.width(8.dp))
                            Button(
                                onClick = onGrantOverlayClick,
                                colors = ButtonDefaults.buttonColors(containerColor = WarningAmber),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("grant_overlay_button")
                            ) {
                                Text("GRANT", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            // Visual Simulation of Floating HUD
            item {
                Text(
                    text = "FLOATING HUD SIMULATION",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(Modifier.height(8.dp))
                FuturisticCard(
                    borderColor = CyberCyan,
                    testTag = "floating_simulation_card"
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Interactive Floating Widget Preview",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "This draggable compact HUD hovers over any game window, allowing instant state changes and emergency shutdown.",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )

                        Spacer(Modifier.height(16.dp))

                        // Render realistic preview of HUD
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF06070B))
                                .border(1.dp, CyberSurfaceBorder, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            // The mini HUD preview
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xEE0B0C12))
                                    .border(1.dp, CyberCyan.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 16.dp, vertical = 12.dp)
                            ) {
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "⚡ AI BLAST",
                                            color = CyberCyan,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                        Spacer(Modifier.width(10.dp))
                                        Text(
                                            text = "● RUNNING (12 FPS)",
                                            color = NeonGreen,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(Modifier.height(8.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(CyberSurfaceVariant)
                                                .padding(horizontal = 10.dp, vertical = 4.dp)
                                        ) {
                                            Text("PAUSE", color = CyberCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(NeonRed)
                                                .padding(horizontal = 10.dp, vertical = 4.dp)
                                        ) {
                                            Text("KILL", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        // Toggle Live Floating Overlay
                        NeonButton(
                            text = if (isFloatingActive) "STOP FLOATING HUD" else "START FLOATING HUD",
                            icon = Icons.Default.Layers,
                            color = if (isFloatingActive) WarningAmber else CyberCyan,
                            enabled = permissionStatus.hasOverlayPermission,
                            onClick = { onToggleFloatingService(!isFloatingActive) },
                            modifier = Modifier.fillMaxWidth(),
                            testTag = "toggle_floating_service_button"
                        )
                    }
                }
            }
        }
    }
}
