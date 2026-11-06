package com.example.ui.screens.gamemanager

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.GameProfile
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
fun GameManagerScreen(
    profiles: List<GameProfile>,
    onBackClick: () -> Unit,
    onAddGameClick: () -> Unit,
    onSelectActiveProfile: (Long) -> Unit,
    onProfileDetailClick: (Long) -> Unit,
    onDeleteProfileClick: (GameProfile) -> Unit
) {
    Scaffold(
        containerColor = CyberDark,
        topBar = {
            CyberTopAppBar(
                title = "GAME MANAGER",
                onBackClick = onBackClick
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddGameClick,
                containerColor = NeonRed,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.testTag("add_game_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Game")
            }
        }
    ) { innerPadding ->
        if (profiles.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Gamepad,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "No Game Profiles Configured",
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Tap the + button to add a target game profile.",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(profiles, key = { it.id }) { profile ->
                    GameProfileCard(
                        profile = profile,
                        onActivateClick = { onSelectActiveProfile(profile.id) },
                        onDetailClick = { onProfileDetailClick(profile.id) },
                        onDeleteClick = { onDeleteProfileClick(profile) }
                    )
                }
            }
        }
    }
}

@Composable
fun GameProfileCard(
    profile: GameProfile,
    onActivateClick: () -> Unit,
    onDetailClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    FuturisticCard(
        borderColor = if (profile.isActive) CyberCyan else CyberSurfaceBorder,
        glowColor = if (profile.isActive) CyberCyan else Color.Transparent,
        testTag = "game_profile_card_${profile.id}"
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (profile.isActive) CyberCyan.copy(alpha = 0.15f)
                                else CyberSurfaceVariant
                            )
                            .border(
                                1.dp,
                                if (profile.isActive) CyberCyan else CyberSurfaceBorder,
                                RoundedCornerShape(8.dp)
                            )
                    ) {
                        Icon(
                            Icons.Default.Gamepad,
                            contentDescription = null,
                            tint = if (profile.isActive) CyberCyan else TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(Modifier.width(10.dp))

                    Column {
                        Text(
                            text = profile.name,
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = profile.packageName,
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                if (profile.isActive) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(NeonGreen.copy(alpha = 0.15f))
                            .border(1.dp, NeonGreen, RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(12.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("ACTIVE", color = NeonGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (profile.description.isNotEmpty()) {
                Spacer(Modifier.height(10.dp))
                Text(
                    text = profile.description,
                    color = TextSecondary,
                    fontSize = 12.sp,
                    maxLines = 2
                )
            }

            Spacer(Modifier.height(12.dp))

            // Specs Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(CyberSurfaceVariant)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "GENRE: ${profile.genre}",
                    color = CyberCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "TARGET: ${profile.targetFps} FPS",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
                Text(
                    text = "MAX: ${profile.maxTapsPerSecond} TAPS/S",
                    color = WarningAmber,
                    fontSize = 11.sp
                )
            }

            Spacer(Modifier.height(12.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row {
                    IconButton(
                        onClick = onDetailClick,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("profile_details_button_${profile.id}")
                    ) {
                        Icon(Icons.Default.Info, contentDescription = "Details", tint = CyberCyan, modifier = Modifier.size(18.dp))
                    }

                    IconButton(
                        onClick = onDeleteClick,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("profile_delete_button_${profile.id}")
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = NeonRed, modifier = Modifier.size(18.dp))
                    }
                }

                if (!profile.isActive) {
                    NeonButton(
                        text = "SET ACTIVE",
                        onClick = onActivateClick,
                        color = CyberCyan,
                        isOutlined = true,
                        modifier = Modifier.height(36.dp),
                        testTag = "set_active_profile_button_${profile.id}"
                    )
                }
            }
        }
    }
}
