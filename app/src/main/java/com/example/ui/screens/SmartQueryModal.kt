package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.DecisionEntity
import com.example.ui.components.HapticFeedbackHelper
import com.example.ui.theme.AmberFlame
import com.example.ui.theme.BorderHairline
import com.example.ui.theme.CrimsonAlert
import com.example.ui.theme.CyanTelemetry
import com.example.ui.theme.EmeraldGate
import com.example.ui.theme.IndigoNexus
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.SlateSurface
import com.example.ui.theme.SlateSurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun SmartQueryModal(
    decision: DecisionEntity,
    onResolve: (optionSelected: String, note: String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedOption by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        HapticFeedbackHelper.triggerWarningPattern(context)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBg)
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TextPrimary
                )
            }
            Text(
                text = "D6 SMART ROUTING & IMPASSE RESOLUTION",
                color = TextPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 0.5.sp
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Impasse Trigger Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CrimsonAlert.copy(alpha = 0.12f), shape = RoundedCornerShape(10.dp))
                    .border(1.dp, CrimsonAlert.copy(alpha = 0.6f), shape = RoundedCornerShape(10.dp))
                    .padding(14.dp)
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = CrimsonAlert,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "[TYPE B: ARCHITECTURAL TRADEOFF IMPASSE]",
                            color = CrimsonAlert,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Specialist agents are divided on write-throughput vs ACID strict serializability. Automated debate halted at DQS ${String.format("%.2f", decision.dqsScore)} (< 0.85). Human executive resolution required.",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Context Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SlateSurface, shape = RoundedCornerShape(10.dp))
                    .border(1.dp, BorderHairline, shape = RoundedCornerShape(10.dp))
                    .padding(14.dp)
            ) {
                Column {
                    Text(
                        text = "CORE CONFLICT CONTEXT",
                        color = TextMuted,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${decision.title}: Should we prioritize sub-millisecond local SQLite batching or distributed multi-region consensus?",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "SELECT EXECUTIVE RESOLUTION PATH",
                color = TextMuted,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Option 1
            TradeoffChoiceCard(
                optionId = "OPTION_1",
                title = "[Option 1]: Write-Heavy Micro-Transactions",
                subtitle = "Optimized for 500k events/sec ingestion. Accepts eventual consistency with monotonic conflict resolution.",
                isSelected = selectedOption == "OPTION_1",
                onSelect = { selectedOption = "OPTION_1" }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Option 2
            TradeoffChoiceCard(
                optionId = "OPTION_2",
                title = "[Option 2]: Read-Heavy Catalog Queries (Strict ACID)",
                subtitle = "Prioritizes immediate linearizability. Throttles peak write bursts to protect database serialization locks.",
                isSelected = selectedOption == "OPTION_2",
                onSelect = { selectedOption = "OPTION_2" }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Option 3: AI Auto-Resolve
            TradeoffChoiceCard(
                optionId = "AI_AUTO",
                title = "[AI Auto-Resolve]: Cost & Latency Pareto Optimum",
                subtitle = "Synthesizes hybrid tiered cache: memory ring buffer + SQLite WAL log.",
                isSelected = selectedOption == "AI_AUTO",
                isAi = true,
                onSelect = { selectedOption = "AI_AUTO" }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Option 4: Defer
            TradeoffChoiceCard(
                optionId = "DEFER",
                title = "[Defer Decision]: Log as Type C Deferred Risk",
                subtitle = "Records minority dissent in SQLite WAL and unblocks downstream streams.",
                isSelected = selectedOption == "DEFER",
                onSelect = { selectedOption = "DEFER" }
            )
        }

        // Bottom Action Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(SlateSurface)
                .border(1.dp, BorderHairline)
                .padding(16.dp)
        ) {
            Button(
                onClick = {
                    val choice = selectedOption ?: "OPTION_1"
                    val label = when (choice) {
                        "OPTION_1" -> "Write-Heavy Micro-Transactions"
                        "OPTION_2" -> "Read-Heavy Catalog Queries"
                        "AI_AUTO" -> "Hybrid Tiered Cache Pareto Optimum"
                        else -> "Deferred Type C Risk"
                    }
                    onResolve(label, "Executive trade-off selected by Human-in-the-Loop.")
                },
                enabled = selectedOption != null,
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGate),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "CONFIRM RESOLUTION & ADVANCE TO D7",
                        color = Color.Black,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

@Composable
fun TradeoffChoiceCard(
    optionId: String,
    title: String,
    subtitle: String,
    isSelected: Boolean,
    isAi: Boolean = false,
    onSelect: () -> Unit
) {
    val borderColor = when {
        isSelected -> EmeraldGate
        isAi -> IndigoNexus
        else -> BorderHairline
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isSelected) SlateSurfaceElevated else SlateSurface, shape = RoundedCornerShape(10.dp))
            .border(1.dp, borderColor, shape = RoundedCornerShape(10.dp))
            .clickable { onSelect() }
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier
                    .padding(top = 2.dp)
                    .size(16.dp)
                    .background(
                        if (isSelected) EmeraldGate else Color.Transparent,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .border(1.dp, if (isSelected) EmeraldGate else TextMuted, shape = RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(Color.Black, shape = RoundedCornerShape(3.dp))
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        color = if (isSelected) EmeraldGate else TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    if (isAi) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = IndigoNexus,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = subtitle,
                    color = TextSecondary,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
            }
        }
    }
}
