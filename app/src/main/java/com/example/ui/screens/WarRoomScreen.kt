package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.AgentPerspective
import com.example.ai.DimensionRating
import com.example.ai.RedTeamCritique
import com.example.ai.WarRoomRoundResult
import com.example.data.local.DecisionEntity
import com.example.data.local.ProvenanceType
import com.example.ui.components.DqsGauge
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
fun WarRoomScreen(
    decision: DecisionEntity,
    currentRound: Int,
    roundResult: WarRoomRoundResult?,
    isDebating: Boolean,
    stopRuleTriggered: Boolean = false,
    onAdvanceRound: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isRedTeamOpen by remember { mutableStateOf(true) }
    var isRadarOpen by remember { mutableStateOf(false) }

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
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "D4-D5 MULTI-AGENT WAR ROOM",
                    color = TextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "${decision.complexityTier} TIER: ${roundResult?.roundName ?: "Evaluating Specialist Perspectives"}",
                    color = CyanTelemetry,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // Directive 7: Honest Simulation Mode Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0F172A))
                .border(1.dp, Color(0xFF1E293B))
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "MODE: [SIMULATED / OFFLINE EVALUATION]",
                    color = AmberFlame,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "AI: RECOMMEND ONLY (CANNOT APPROVE)",
                    color = TextMuted,
                    fontSize = 8.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // Directive 6: Stop Rule Notification Banner
        if (stopRuleTriggered) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(EmeraldGate.copy(alpha = 0.12f))
                    .border(1.dp, EmeraldGate.copy(alpha = 0.5f))
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = EmeraldGate,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "CONVERGENCE REACHED: Stop Rule Triggered (Δ DQS < 0.05). Ready for D6 Synthesis.",
                        color = EmeraldGate,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // Body Content
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            // Live DQS Cockpit Widget
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .background(SlateSurface, shape = RoundedCornerShape(10.dp))
                        .border(1.dp, BorderHairline, shape = RoundedCornerShape(10.dp))
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "DECISION QUALITY SCORE (DQS)",
                                color = TextMuted,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            val displayDqs = roundResult?.dqsScore ?: decision.dqsScore
                            Text(
                                text = String.format("%.2f / 1.00", displayDqs),
                                color = if (displayDqs >= 0.85) EmeraldGate else AmberFlame,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "7-Parameter Canonical Model (Divisor = 0.85). No artificial boosts.",
                                color = TextMuted,
                                fontSize = 8.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        val displayDqs = roundResult?.dqsScore ?: decision.dqsScore
                        DqsGauge(score = displayDqs, size = 60.dp)
                    }
                }
            }

            // Consensus Summary Box
            item {
                val summary = roundResult?.consensusSummary
                if (summary != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                            .background(Color(0xFF0F172A), shape = RoundedCornerShape(8.dp))
                            .border(1.dp, CyanTelemetry.copy(alpha = 0.3f), shape = RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Column {
                            Text(
                                text = "MULTI-AGENT SYNTHESIS SUMMARY:",
                                color = CyanTelemetry,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = summary,
                                color = TextPrimary,
                                fontSize = 11.sp,
                                lineHeight = 15.sp
                            )
                        }
                    }
                }
            }

            // Specialist Perspective Cards Carousel
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SPECIALIST PERSPECTIVES (${roundResult?.agents?.size ?: 0} AGENTS):",
                        color = TextMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "SWIPE >>",
                        color = CyanTelemetry,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))

                val agents = roundResult?.agents ?: emptyList()
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(agents) { agent ->
                        SpecialistAgentCard(agent = agent)
                    }
                }
            }

            // The Devil's Advocate Red-Team Drawer
            item {
                Spacer(modifier = Modifier.height(14.dp))
                val redTeam = roundResult?.redTeam
                if (redTeam != null) {
                    RedTeamDrawer(
                        critique = redTeam,
                        isOpen = isRedTeamOpen,
                        onToggle = { isRedTeamOpen = !isRedTeamOpen }
                    )
                }
            }

            // 14-Dimension Radar / Inspection Panel
            item {
                Spacer(modifier = Modifier.height(10.dp))
                val ratings = roundResult?.dimensionRatings ?: emptyList()
                DimensionsPanel(
                    ratings = ratings,
                    isOpen = isRadarOpen,
                    onToggle = { isRadarOpen = !isRadarOpen }
                )
            }
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
                onClick = onAdvanceRound,
                enabled = !isDebating,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (stopRuleTriggered || currentRound >= 2) EmeraldGate else IndigoNexus
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                if (isDebating) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "EVALUATING MULTI-AGENT ROUND...",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (stopRuleTriggered || currentRound >= 2) {
                                "GENERATE D6 SYNTHESIS & REVIEW PACKET >>"
                            } else {
                                "ADVANCE TO D5 ADVERSARIAL CRITIQUE >>"
                            },
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SpecialistAgentCard(agent: AgentPerspective) {
    val provenanceColor = when (agent.provenance) {
        ProvenanceType.VERIFIED -> EmeraldGate
        ProvenanceType.EXPERT_INFERENCE -> CyanTelemetry
        ProvenanceType.ASSUMPTION -> AmberFlame
        ProvenanceType.UNVERIFIED -> CrimsonAlert
    }

    Box(
        modifier = Modifier
            .width(260.dp)
            .background(SlateSurface, shape = RoundedCornerShape(10.dp))
            .border(1.dp, BorderHairline, shape = RoundedCornerShape(10.dp))
            .padding(14.dp)
    ) {
        Column {
            // Specialist Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = agent.role.uppercase(),
                    color = CyanTelemetry,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                // Honest Provenance Tag
                Box(
                    modifier = Modifier
                        .background(provenanceColor.copy(alpha = 0.15f), shape = RoundedCornerShape(4.dp))
                        .border(1.dp, provenanceColor, shape = RoundedCornerShape(4.dp))
                        .padding(horizontal = 5.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = agent.provenance.name,
                        color = provenanceColor,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = agent.specialistName,
                color = TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Claim
            Text(
                text = "CLAIM:",
                color = TextMuted,
                fontSize = 8.sp,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = agent.claim,
                color = TextSecondary,
                fontSize = 11.sp,
                lineHeight = 15.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Proposed Strategy
            Text(
                text = "PROPOSED STRATEGY:",
                color = TextMuted,
                fontSize = 8.sp,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = agent.proposedStrategy,
                color = TextPrimary,
                fontSize = 11.sp,
                lineHeight = 15.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Risk Warning
            Text(
                text = "RISK WARNING:",
                color = AmberFlame,
                fontSize = 8.sp,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = agent.riskWarning,
                color = TextMuted,
                fontSize = 10.sp,
                lineHeight = 14.sp
            )
        }
    }
}

@Composable
fun RedTeamDrawer(
    critique: RedTeamCritique,
    isOpen: Boolean,
    onToggle: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .background(SlateSurface, shape = RoundedCornerShape(10.dp))
            .border(1.dp, CrimsonAlert.copy(alpha = 0.6f), shape = RoundedCornerShape(10.dp))
            .padding(12.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggle() },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = CrimsonAlert,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "DEVIL'S ADVOCATE (RED TEAM): ${critique.title.uppercase()}",
                        color = CrimsonAlert,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Icon(
                    imageVector = if (isOpen) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = TextMuted
                )
            }

            AnimatedVisibility(visible = isOpen) {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    Text(
                        text = "COUNTER-ARGUMENT:",
                        color = TextMuted,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = critique.counterArgument,
                        color = TextPrimary,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "STRESS-TEST SCENARIO:",
                        color = TextMuted,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = critique.stressTestScenario,
                        color = AmberFlame,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "UNHANDLED RESIDUAL RISK:",
                        color = TextMuted,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = critique.unhandledRisk,
                        color = TextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                }
            }
        }
    }
}

@Composable
fun DimensionsPanel(
    ratings: List<DimensionRating>,
    isOpen: Boolean,
    onToggle: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .background(SlateSurface, shape = RoundedCornerShape(10.dp))
            .border(1.dp, BorderHairline, shape = RoundedCornerShape(10.dp))
            .padding(12.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggle() },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = CyanTelemetry,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "14-DIMENSION ARCHITECTURE RADAR (${ratings.size})",
                        color = CyanTelemetry,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Icon(
                    imageVector = if (isOpen) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = TextMuted
                )
            }

            AnimatedVisibility(visible = isOpen) {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    ratings.forEach { rating ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = rating.name,
                                color = TextSecondary,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${rating.score}/10",
                                    color = if (rating.score >= 8) EmeraldGate else if (rating.score >= 6) CyanTelemetry else AmberFlame,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
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
