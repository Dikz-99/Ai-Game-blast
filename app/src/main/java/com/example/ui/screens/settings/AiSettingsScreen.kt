package com.example.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.UserSettings
import com.example.ui.components.CyberTopAppBar
import com.example.ui.components.FuturisticCard
import com.example.ui.components.NeonButton
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberDark
import com.example.ui.theme.CyberSurfaceBorder
import com.example.ui.theme.CyberSurfaceVariant
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonRed
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningAmber

@Composable
fun AiSettingsScreen(
    currentSettings: UserSettings?,
    onBackClick: () -> Unit,
    onSaveSettings: (UserSettings) -> Unit
) {
    val initialSettings = currentSettings ?: UserSettings()

    var maxTapsPerSec by remember { mutableFloatStateOf(initialSettings.safeMaxTapsPerSecond.toFloat()) }
    var minIntervalMs by remember { mutableFloatStateOf(initialSettings.minIntervalBetweenActionsMs.toFloat()) }
    var enableJitter by remember { mutableStateOf(initialSettings.enableNaturalJitter) }
    var enableOverlayHud by remember { mutableStateOf(initialSettings.enableOverlayHud) }
    var enableAudioHaptic by remember { mutableStateOf(initialSettings.enableAudioHapticFeedback) }
    var cloudAiOptIn by remember { mutableStateOf(initialSettings.cloudAiOptIn) }
    var autoRecordSessions by remember { mutableStateOf(initialSettings.autoRecordSessions) }

    Scaffold(
        containerColor = CyberDark,
        topBar = {
            CyberTopAppBar(
                title = "AI ENGINE SETTINGS",
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
            // Safety Guardrails
            item {
                FuturisticCard(
                    borderColor = NeonRed.copy(alpha = 0.5f),
                    testTag = "safety_guardrails_card"
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "SAFETY CONTROLLER GUARDRAILS",
                            color = NeonRed,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )

                        Spacer(Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Global Max Tap Rate", color = TextPrimary, fontSize = 13.sp)
                            Text("${maxTapsPerSec.toInt()} taps/s", color = WarningAmber, fontWeight = FontWeight.Bold)
                        }
                        Slider(
                            value = maxTapsPerSec,
                            onValueChange = { maxTapsPerSec = it },
                            valueRange = 1f..8f,
                            steps = 6,
                            colors = SliderDefaults.colors(
                                thumbColor = WarningAmber,
                                activeTrackColor = WarningAmber,
                                inactiveTrackColor = CyberSurfaceBorder
                            ),
                            modifier = Modifier.testTag("global_max_taps_slider")
                        )

                        Spacer(Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Min Action Cooldown", color = TextPrimary, fontSize = 13.sp)
                            Text("${minIntervalMs.toInt()} ms", color = CyberCyan, fontWeight = FontWeight.Bold)
                        }
                        Slider(
                            value = minIntervalMs,
                            onValueChange = { minIntervalMs = it },
                            valueRange = 100f..800f,
                            steps = 13,
                            colors = SliderDefaults.colors(
                                thumbColor = CyberCyan,
                                activeTrackColor = CyberCyan,
                                inactiveTrackColor = CyberSurfaceBorder
                            ),
                            modifier = Modifier.testTag("min_cooldown_slider")
                        )

                        Spacer(Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Natural Touch Jitter", color = TextPrimary, fontSize = 13.sp)
                                Text("Adds micro-randomization (±3px) to simulate human touch", color = TextSecondary, fontSize = 11.sp)
                            }
                            Switch(
                                checked = enableJitter,
                                onCheckedChange = { enableJitter = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = NeonGreen,
                                    checkedTrackColor = NeonGreen.copy(alpha = 0.4f),
                                    uncheckedThumbColor = TextSecondary,
                                    uncheckedTrackColor = CyberSurfaceVariant
                                ),
                                modifier = Modifier.testTag("natural_jitter_switch")
                            )
                        }
                    }
                }
            }

            // HUD & Interface Settings
            item {
                FuturisticCard(testTag = "hud_settings_card") {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "INTERFACE & FEEDBACK",
                            color = CyberCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )

                        Spacer(Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Floating Assistant HUD", color = TextPrimary, fontSize = 13.sp)
                                Text("Show on-screen draggable bubble overlay", color = TextSecondary, fontSize = 11.sp)
                            }
                            Switch(
                                checked = enableOverlayHud,
                                onCheckedChange = { enableOverlayHud = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = CyberCyan,
                                    checkedTrackColor = CyberCyan.copy(alpha = 0.4f),
                                    uncheckedThumbColor = TextSecondary,
                                    uncheckedTrackColor = CyberSurfaceVariant
                                ),
                                modifier = Modifier.testTag("floating_hud_switch")
                            )
                        }

                        Spacer(Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Haptic & Audio Feedback", color = TextPrimary, fontSize = 13.sp)
                                Text("Vibrate when actions are triggered", color = TextSecondary, fontSize = 11.sp)
                            }
                            Switch(
                                checked = enableAudioHaptic,
                                onCheckedChange = { enableAudioHaptic = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = CyberCyan,
                                    checkedTrackColor = CyberCyan.copy(alpha = 0.4f),
                                    uncheckedThumbColor = TextSecondary,
                                    uncheckedTrackColor = CyberSurfaceVariant
                                ),
                                modifier = Modifier.testTag("haptic_switch")
                            )
                        }

                        Spacer(Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Auto Record Sessions", color = TextPrimary, fontSize = 13.sp)
                                Text("Log action telemetry and win rates to Room DB", color = TextSecondary, fontSize = 11.sp)
                            }
                            Switch(
                                checked = autoRecordSessions,
                                onCheckedChange = { autoRecordSessions = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = CyberCyan,
                                    checkedTrackColor = CyberCyan.copy(alpha = 0.4f),
                                    uncheckedThumbColor = TextSecondary,
                                    uncheckedTrackColor = CyberSurfaceVariant
                                ),
                                modifier = Modifier.testTag("auto_record_switch")
                            )
                        }
                    }
                }
            }

            // Cloud AI Hybrid Opt-In
            item {
                FuturisticCard(
                    borderColor = CyberSurfaceBorder,
                    testTag = "cloud_ai_card"
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "CLOUD AI HYBRID PROCESSING (OPT-IN)",
                            color = CyberCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )

                        Spacer(Modifier.height(10.dp))

                        Text(
                            text = "When enabled, complex state patterns and macro strategies can be evaluated by Cloud AI models. Local AI handles all real-time screen capture and execution offline.",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )

                        Spacer(Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Enable Cloud Strategy Advisor", color = TextPrimary, fontSize = 13.sp)
                            Switch(
                                checked = cloudAiOptIn,
                                onCheckedChange = { cloudAiOptIn = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = NeonGreen,
                                    checkedTrackColor = NeonGreen.copy(alpha = 0.4f),
                                    uncheckedThumbColor = TextSecondary,
                                    uncheckedTrackColor = CyberSurfaceVariant
                                ),
                                modifier = Modifier.testTag("cloud_ai_optin_switch")
                            )
                        }
                    }
                }
            }

            // Save Settings Button
            item {
                NeonButton(
                    text = "APPLY SETTINGS",
                    color = NeonRed,
                    onClick = {
                        val updated = initialSettings.copy(
                            safeMaxTapsPerSecond = maxTapsPerSec.toInt(),
                            minIntervalBetweenActionsMs = minIntervalMs.toLong(),
                            enableNaturalJitter = enableJitter,
                            enableOverlayHud = enableOverlayHud,
                            enableAudioHapticFeedback = enableAudioHaptic,
                            cloudAiOptIn = cloudAiOptIn,
                            autoRecordSessions = autoRecordSessions
                        )
                        onSaveSettings(updated)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "save_settings_button"
                )
            }
        }
    }
}
