package com.example.ui.screens.profiledetail

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.GameProfile
import com.example.data.local.entity.StrategyMemory
import com.example.ui.components.CyberTopAppBar
import com.example.ui.components.FuturisticCard
import com.example.ui.components.MetricItem
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
fun GameProfileDetailScreen(
    profile: GameProfile?,
    memories: List<StrategyMemory>,
    onBackClick: () -> Unit,
    onAddMemoryRuleClick: () -> Unit
) {
    Scaffold(
        containerColor = CyberDark,
        topBar = {
            CyberTopAppBar(
                title = profile?.name?.uppercase() ?: "PROFILE DETAIL",
                onBackClick = onBackClick
            )
        }
    ) { innerPadding ->
        if (profile == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("Profile not found", color = TextSecondary)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Profile Specs Overview
                item {
                    FuturisticCard(
                        borderColor = if (profile.isActive) CyberCyan else CyberSurfaceBorder,
                        testTag = "profile_detail_overview_card"
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = profile.name,
                                        color = TextPrimary,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = profile.packageName,
                                        color = TextSecondary,
                                        fontSize = 12.sp
                                    )
                                }

                                Text(
                                    text = profile.genre,
                                    color = CyberCyan,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            if (profile.description.isNotEmpty()) {
                                Spacer(Modifier.height(10.dp))
                                Text(
                                    text = profile.description,
                                    color = TextSecondary,
                                    fontSize = 12.sp
                                )
                            }

                            Spacer(Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                MetricItem(
                                    label = "FPS",
                                    value = "${profile.targetFps}",
                                    unit = "fps",
                                    accentColor = CyberCyan,
                                    modifier = Modifier.weight(1f)
                                )
                                MetricItem(
                                    label = "TAP LIMIT",
                                    value = "${profile.maxTapsPerSecond}",
                                    unit = "/s",
                                    accentColor = WarningAmber,
                                    modifier = Modifier.weight(1f)
                                )
                                MetricItem(
                                    label = "RULES",
                                    value = "${memories.size}",
                                    unit = "pola",
                                    accentColor = NeonGreen,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                // Attached Game Adapter & Capabilities
                item {
                    val adapter = com.example.adapter.registry.AdapterRegistry.getAdapterForProfile(profile)
                    FuturisticCard(testTag = "attached_game_adapter_card") {
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
                                        imageVector = Icons.Default.Gamepad,
                                        contentDescription = null,
                                        tint = CyberCyan,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = "GAME ADAPTER CORE",
                                        color = TextSecondary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(NeonGreen.copy(alpha = 0.15f))
                                        .border(1.dp, NeonGreen, RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "ACTIVE",
                                        color = NeonGreen,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(Modifier.height(10.dp))
                            Text(
                                text = adapter.adapterName,
                                color = TextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = adapter.description,
                                color = TextSecondary,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )

                            val adapterMacros = adapter.getPresetMacros()
                            if (adapterMacros.isNotEmpty()) {
                                Spacer(Modifier.height(12.dp))
                                Text(
                                    text = "ADAPTER PRESET MACROS (${adapterMacros.size})",
                                    color = CyberCyan,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(Modifier.height(6.dp))
                                adapterMacros.forEach { macro ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 3.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(CyberSurfaceVariant)
                                            .padding(horizontal = 8.dp, vertical = 6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = macro.name,
                                                color = TextPrimary,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "${macro.actions.size} actions: ${macro.description}",
                                                color = TextSecondary,
                                                fontSize = 10.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Strategy Memories List
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Psychology, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "STRATEGY MEMORY (COMBINED LEARNING)",
                                color = TextSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }

                if (memories.isEmpty()) {
                    item {
                        FuturisticCard(testTag = "empty_memory_card") {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.Memory, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(40.dp))
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = "No strategy patterns learned yet",
                                    color = TextSecondary,
                                    fontSize = 13.sp
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = "Patterns and coordinate triggers will populate automatically during automated sessions.",
                                    color = TextSecondary.copy(alpha = 0.7f),
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                } else {
                    items(memories, key = { it.id }) { memory ->
                        StrategyMemoryCard(memory = memory)
                    }
                }

                // Raw Config JSON Inspector
                item {
                    Text(
                        text = "ADAPTER CONFIGURATION JSON",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(CyberSurfaceVariant)
                            .border(1.dp, CyberSurfaceBorder, RoundedCornerShape(10.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = profile.configJson,
                            color = CyberCyan,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StrategyMemoryCard(memory: StrategyMemory) {
    FuturisticCard(
        borderColor = CyberSurfaceBorder,
        testTag = "strategy_memory_${memory.id}"
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = memory.stateSignature,
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )

                Text(
                    text = "WEIGHT: ${(memory.weight * 100).toInt()}%",
                    color = NeonGreen,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(6.dp))

            Text(
                text = "ACTION: ${memory.suggestedAction} at (${memory.targetCoordinates})",
                color = CyberCyan,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(CyberSurfaceVariant)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("SUCCESS: ${memory.successCount}", color = NeonGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Text("FAILURES: ${memory.failureCount}", color = if (memory.failureCount > 0) NeonRed else TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
