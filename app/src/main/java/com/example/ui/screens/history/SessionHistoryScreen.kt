package com.example.ui.screens.history

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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.GameSession
import com.example.ui.components.CyberTopAppBar
import com.example.ui.components.FuturisticCard
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberDark
import com.example.ui.theme.CyberSurfaceBorder
import com.example.ui.theme.CyberSurfaceVariant
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonRed
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningAmber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SessionHistoryScreen(
    sessions: List<GameSession>,
    onBackClick: () -> Unit
) {
    Scaffold(
        containerColor = CyberDark,
        topBar = {
            CyberTopAppBar(
                title = "SESSION HISTORY",
                onBackClick = onBackClick
            )
        }
    ) { innerPadding ->
        if (sessions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.History,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(Modifier.height(14.dp))
                    Text(
                        text = "No Recorded Sessions Yet",
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Automated sessions will automatically log action telemetry here.",
                        color = TextSecondary,
                        fontSize = 12.sp
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
                items(sessions, key = { it.id }) { session ->
                    SessionItemCard(session = session)
                }
            }
        }
    }
}

@Composable
fun SessionItemCard(session: GameSession) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.getDefault())
    val dateString = dateFormat.format(Date(session.startTime))

    val successRate = if (session.totalActions > 0) {
        (session.successfulActions.toFloat() / session.totalActions.toFloat() * 100).toInt()
    } else {
        100
    }

    FuturisticCard(
        borderColor = if (session.status == "COMPLETED") CyberSurfaceBorder else NeonRed.copy(alpha = 0.5f),
        testTag = "session_item_${session.id}"
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(
                                if (session.status == "COMPLETED") NeonGreen else NeonRed
                            )
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = session.profileName,
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = session.status,
                    color = if (session.status == "COMPLETED") NeonGreen else NeonRed,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(4.dp))

            Text(
                text = dateString,
                color = TextSecondary,
                fontSize = 11.sp
            )

            Spacer(Modifier.height(12.dp))

            // Metrics row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(CyberSurfaceVariant)
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("TOTAL ACTIONS", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Text("${session.totalActions}", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }

                Column {
                    Text("SUCCESS RATE", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Text("$successRate%", color = if (successRate >= 90) NeonGreen else WarningAmber, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }

                Column {
                    Text("AVG LATENCY", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Text("${session.averageLatencyMs}ms", color = CyberCyan, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }

            if (session.summaryNotes.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = session.summaryNotes,
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }
        }
    }
}
