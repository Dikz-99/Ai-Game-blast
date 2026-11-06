package com.example.ui.screens.addgame

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.model.GameGenre
import com.example.data.local.entity.GameProfile
import com.example.ui.components.CyberTopAppBar
import com.example.ui.components.FuturisticCard
import com.example.ui.components.NeonButton
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
fun AddGameScreen(
    onBackClick: () -> Unit,
    onSaveProfile: (GameProfile) -> Unit
) {
    var gameName by remember { mutableStateOf("") }
    var packageName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedGenre by remember { mutableStateOf(GameGenre.TURN_BASED_RPG) }
    var targetFps by remember { mutableFloatStateOf(10f) }
    var maxTapsPerSecond by remember { mutableFloatStateOf(4f) }
    var isAutoRestartEnabled by remember { mutableStateOf(false) }

    var validationError by remember { mutableStateOf<String?>(null) }

    Scaffold(
        containerColor = CyberDark,
        topBar = {
            CyberTopAppBar(
                title = "ADD GAME PROFILE",
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
            item {
                FuturisticCard(testTag = "basic_info_card") {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "IDENTITAS PERMAINAN",
                            color = CyberCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )

                        Spacer(Modifier.height(12.dp))

                        OutlinedTextField(
                            value = gameName,
                            onValueChange = { gameName = it; validationError = null },
                            label = { Text("Game Name") },
                            placeholder = { Text("e.g. Cyber RPG Arena") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyberCyan,
                                unfocusedBorderColor = CyberSurfaceBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedLabelColor = CyberCyan,
                                unfocusedLabelColor = TextSecondary
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("game_name_input")
                        )

                        Spacer(Modifier.height(12.dp))

                        OutlinedTextField(
                            value = packageName,
                            onValueChange = { packageName = it; validationError = null },
                            label = { Text("Package Name") },
                            placeholder = { Text("e.g. com.example.game") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyberCyan,
                                unfocusedBorderColor = CyberSurfaceBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedLabelColor = CyberCyan,
                                unfocusedLabelColor = TextSecondary
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("package_name_input")
                        )

                        Spacer(Modifier.height(12.dp))

                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("Description & Strategy Goal") },
                            placeholder = { Text("e.g. Auto battle farming loop with skill rotation") },
                            maxLines = 3,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyberCyan,
                                unfocusedBorderColor = CyberSurfaceBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedLabelColor = CyberCyan,
                                unfocusedLabelColor = TextSecondary
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("description_input")
                        )
                    }
                }
            }

            // Genre Selector
            item {
                FuturisticCard(testTag = "genre_selector_card") {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "GENRE & ADAPTER TARGET",
                            color = CyberCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )

                        Spacer(Modifier.height(12.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            GameGenre.entries.chunked(2).forEach { rowGenres ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    rowGenres.forEach { genre ->
                                        val isSelected = selectedGenre == genre
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(
                                                    if (isSelected) CyberCyan.copy(alpha = 0.2f)
                                                    else CyberSurfaceVariant
                                                )
                                                .border(
                                                    1.dp,
                                                    if (isSelected) CyberCyan else CyberSurfaceBorder,
                                                    RoundedCornerShape(10.dp)
                                                )
                                                .clickable { selectedGenre = genre }
                                                .padding(12.dp)
                                        ) {
                                            Text(
                                                text = genre.displayName,
                                                color = if (isSelected) CyberCyan else TextSecondary,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                fontSize = 12.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Target FPS and Tap Safety Slider
            item {
                FuturisticCard(testTag = "tuning_card") {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "PERFORMANCE & SAFETY THRESHOLDS",
                            color = CyberCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )

                        Spacer(Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Target Frame Rate", color = TextPrimary, fontSize = 13.sp)
                            Text("${targetFps.toInt()} FPS", color = CyberCyan, fontWeight = FontWeight.Bold)
                        }
                        Slider(
                            value = targetFps,
                            onValueChange = { targetFps = it },
                            valueRange = 5f..30f,
                            steps = 24,
                            colors = SliderDefaults.colors(
                                thumbColor = CyberCyan,
                                activeTrackColor = CyberCyan,
                                inactiveTrackColor = CyberSurfaceBorder
                            ),
                            modifier = Modifier.testTag("target_fps_slider")
                        )

                        Spacer(Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Max Safe Actions / Sec", color = TextPrimary, fontSize = 13.sp)
                            Text("${maxTapsPerSecond.toInt()} taps/s", color = WarningAmber, fontWeight = FontWeight.Bold)
                        }
                        Slider(
                            value = maxTapsPerSecond,
                            onValueChange = { maxTapsPerSecond = it },
                            valueRange = 1f..10f,
                            steps = 8,
                            colors = SliderDefaults.colors(
                                thumbColor = WarningAmber,
                                activeTrackColor = WarningAmber,
                                inactiveTrackColor = CyberSurfaceBorder
                            ),
                            modifier = Modifier.testTag("max_taps_slider")
                        )

                        Spacer(Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Auto Restart on Defeat", color = TextPrimary, fontSize = 13.sp)
                                Text("Re-enters lobby automatically", color = TextSecondary, fontSize = 11.sp)
                            }
                            Switch(
                                checked = isAutoRestartEnabled,
                                onCheckedChange = { isAutoRestartEnabled = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = NeonGreen,
                                    checkedTrackColor = NeonGreen.copy(alpha = 0.4f),
                                    uncheckedThumbColor = TextSecondary,
                                    uncheckedTrackColor = CyberSurfaceVariant
                                ),
                                modifier = Modifier.testTag("auto_restart_switch")
                            )
                        }
                    }
                }
            }

            if (validationError != null) {
                item {
                    Text(
                        text = validationError ?: "",
                        color = NeonRed,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Save Button
            item {
                NeonButton(
                    text = "SAVE GAME PROFILE",
                    icon = Icons.Default.Save,
                    color = NeonRed,
                    onClick = {
                        if (gameName.isBlank()) {
                            validationError = "Game Name cannot be blank"
                        } else if (packageName.isBlank()) {
                            validationError = "Package Name cannot be blank"
                        } else {
                            val newProfile = GameProfile(
                                name = gameName.trim(),
                                packageName = packageName.trim(),
                                genre = selectedGenre.name,
                                description = description.trim(),
                                targetFps = targetFps.toInt(),
                                maxTapsPerSecond = maxTapsPerSecond.toInt(),
                                isAutoRestartEnabled = isAutoRestartEnabled
                            )
                            onSaveProfile(newProfile)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "save_profile_button"
                )
            }
        }
    }
}
