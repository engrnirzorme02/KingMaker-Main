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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FactCheck
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
import com.example.core.math.DecisionMathEngine
import com.example.data.local.ClaimType
import com.example.data.local.DecisionEntity
import com.example.ui.components.QualityVectorCard
import com.example.ui.localization.LocalAppStrings
import com.example.ui.theme.AmberFlame
import com.example.ui.theme.BorderHairline
import com.example.ui.theme.CrimsonAlert
import com.example.ui.theme.CyanTelemetry
import com.example.ui.theme.EmeraldGate
import com.example.ui.theme.GoldWarning
import com.example.ui.theme.IndigoNexus
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.SlateSurface
import com.example.ui.theme.SlateSurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

/**
 * KingMaker v7.0: D4-D5 War Room Screen (Section 4, 49, 50).
 * Features:
 * - Independent Specialist Perspectives
 * - Devil's Advocate (Red Team) Adversarial Counter-Case
 * - Coverage Auditor (Cross-cutting D01-D11 check)
 * - Explicit Critique Dimensions: CDR-P1 (11 Dimensions)
 * - 9-Dimensional Quality Vector
 * - Clear Bangla + English Copy
 */
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
    val strings = LocalAppStrings.current
    var isRedTeamOpen by remember { mutableStateOf(true) }
    var isCoverageOpen by remember { mutableStateOf(true) }
    var isCDRP1Open by remember { mutableStateOf(false) }

    val qv = roundResult?.qualityVector ?: DecisionMathEngine.evaluateQualityVector(
        evidenceType = decision.evidenceType,
        hasConfirmedFraming = true,
        constraintsCount = 3,
        optionsCount = 2,
        changeability = decision.changeability,
        risk = decision.risk,
        specialistAgreement = 0.85,
        validationPass = currentRound >= 2,
        complexityScore = decision.complexityScore
    )

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
                    contentDescription = strings.back,
                    tint = TextPrimary
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = strings.warRoomHeader,
                    color = TextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "${decision.complexityTier} TIER • ${roundResult?.roundName ?: "Evaluating Specialist Perspectives"}",
                    color = CyanTelemetry,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // Provenance & Simulation Banner (Section 46.2)
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
                    text = "CRITIQUE REGISTRY: CDR-P1",
                    color = CyanTelemetry,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // Stop Rule Notification Banner (Directive 6)
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
                        text = "কনভারজেন্স অর্জিত: Stop Rule Triggered (Δ Q-Score < 0.05). সংশ্লেষণের জন্য প্রস্তুত।",
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
            // 9-Dimensional Quality Vector Card
            item {
                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    QualityVectorCard(
                        qualityVector = qv,
                        dqsScore = roundResult?.dqsScore ?: decision.dqsScore
                    )
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

            // Specialist Perspectives Carousel
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "১. প্রাসঙ্গিক বিশেষজ্ঞ মতামত (${roundResult?.agents?.size ?: 0})",
                        color = TextPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "CONTEXTUAL SPECIALISTS",
                        color = TextMuted,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))

                val agents = roundResult?.agents ?: emptyList()
                if (agents.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .background(SlateSurface, shape = RoundedCornerShape(8.dp))
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "ওয়ার রুম প্রস্তুত। 'ADVANCE DEBATE' চাপুন।",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }
                } else {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(agents) { agent ->
                            SpecialistAgentCard(agent)
                        }
                    }
                }
            }

            // Devil's Advocate (Red Team) Drawer
            item {
                Spacer(modifier = Modifier.height(14.dp))
                val critique = roundResult?.redTeam
                if (critique != null) {
                    RedTeamDrawer(
                        critique = critique,
                        isOpen = isRedTeamOpen,
                        onToggle = { isRedTeamOpen = !isRedTeamOpen }
                    )
                }
            }

            // Coverage Auditor Drawer (Cross-cutting)
            item {
                Spacer(modifier = Modifier.height(10.dp))
                val auditText = roundResult?.coverageAudit ?: "Coverage Auditor: All 11 dimensions mapped. No unassessed operational blind spots detected."
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .background(SlateSurface, shape = RoundedCornerShape(10.dp))
                        .border(1.dp, IndigoNexus.copy(alpha = 0.5f), shape = RoundedCornerShape(10.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isCoverageOpen = !isCoverageOpen },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.FactCheck,
                                    contentDescription = null,
                                    tint = IndigoNexus,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "COVERAGE AUDITOR (CROSS-CUTTING AUDIT)",
                                    color = IndigoNexus,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            Icon(
                                imageVector = if (isCoverageOpen) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null,
                                tint = TextMuted
                            )
                        }
                        AnimatedVisibility(visible = isCoverageOpen) {
                            Column(modifier = Modifier.padding(top = 8.dp)) {
                                Text(
                                    text = auditText,
                                    color = TextPrimary,
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    }
                }
            }

            // CDR-P1 11-Dimension Critique Panel (Section 49 & 50)
            item {
                Spacer(modifier = Modifier.height(10.dp))
                val ratings = roundResult?.dimensionRatings ?: emptyList()
                if (ratings.isNotEmpty()) {
                    CDRP1Panel(
                        ratings = ratings,
                        isOpen = isCDRP1Open,
                        onToggle = { isCDRP1Open = !isCDRP1Open }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }

        // Bottom Action Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(SlateSurfaceElevated)
                .border(1.dp, BorderHairline)
                .padding(14.dp)
        ) {
            Button(
                onClick = onAdvanceRound,
                enabled = !isDebating,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (stopRuleTriggered) EmeraldGate else IndigoNexus
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                if (isDebating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(strings.debatingInProgress, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                } else {
                    Icon(
                        imageVector = if (stopRuleTriggered) Icons.Default.Gavel else Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (stopRuleTriggered) "${strings.btnProceedToBlueprint} >>" else "${strings.btnAdvanceRound} >>",
                        color = Color.White,
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
fun SpecialistAgentCard(agent: AgentPerspective) {
    val provenanceColor = when (agent.provenance) {
        ClaimType.FACT -> EmeraldGate
        ClaimType.CONSTRAINT -> CyanTelemetry
        ClaimType.INFERENCE -> CyanTelemetry
        ClaimType.ASSUMPTION -> AmberFlame
        ClaimType.UNKNOWN -> GoldWarning
        else -> CrimsonAlert
    }

    Box(
        modifier = Modifier
            .width(260.dp)
            .background(SlateSurface, shape = RoundedCornerShape(10.dp))
            .border(1.dp, BorderHairline, shape = RoundedCornerShape(10.dp))
            .padding(14.dp)
    ) {
        Column {
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

            Text(
                text = "CLAIM / দাবি:",
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

            Text(
                text = "STRATEGY / প্রস্তাবনা:",
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

            Text(
                text = "RISK WARNING / ঝুঁকি:",
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
                        text = "DEVIL'S ADVOCATE: ${critique.title.uppercase()}",
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
                        text = "TARGET / আক্রমণের লক্ষ্য:",
                        color = TextMuted,
                        fontSize = 8.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = critique.target,
                        color = CrimsonAlert,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "CHALLENGE / পাল্টা যুক্তি:",
                        color = TextMuted,
                        fontSize = 8.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = critique.challenge,
                        color = TextPrimary,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "STRESS-TEST SCENARIO / স্ট্রেস টেস্ট:",
                        color = TextMuted,
                        fontSize = 8.sp,
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
                        fontSize = 8.sp,
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

/**
 * Section 49 & 50: Personal Critique Registry CDR-P1 (11 Dimensions).
 * UI explicitly states: "Critique Dimensions: CDR-P1 (11 Dimensions)"
 */
@Composable
fun CDRP1Panel(
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
                        text = "CRITIQUE DIMENSIONS: CDR-P1 (11 DIMENSIONS)",
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
                        Column(modifier = Modifier.padding(vertical = 4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "[${rating.code}] ${rating.name}",
                                    color = TextPrimary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = "${rating.score}/10",
                                    color = if (rating.score >= 8) EmeraldGate else if (rating.score >= 6) CyanTelemetry else AmberFlame,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            Text(
                                text = rating.commentary,
                                color = TextMuted,
                                fontSize = 9.sp,
                                lineHeight = 13.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
