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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.DecisionBranchEntity
import com.example.data.local.DecisionEntity
import com.example.data.local.DecisionStatus
import com.example.ui.components.ComplexityBadge
import com.example.ui.localization.LocalAppStrings
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.BorderHairline
import com.example.ui.theme.CyanTelemetry
import com.example.ui.theme.EmeraldGate
import com.example.ui.theme.IndigoNexus
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.SlateSurface
import com.example.ui.theme.SlateSurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

/**
 * KingMaker v7.0 Decisions List & Portfolio Screen (Directive 30).
 * Imports superior search, state filtering, and lifecycle visibility from new-7.
 */
@Composable
fun DecisionsListScreen(
    decisions: List<DecisionEntity>,
    branches: List<DecisionBranchEntity>,
    activeBranchId: String,
    onSelectBranch: (String) -> Unit,
    onSelectDecision: (DecisionEntity) -> Unit,
    onNewDecision: () -> Unit,
    onOpenWarRoom: (DecisionEntity) -> Unit,
    onOpenReview: (DecisionEntity) -> Unit,
    onOpenBlueprint: (DecisionEntity) -> Unit,
    onOpenOutcome: (DecisionEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedStatusFilter by remember { mutableStateOf<String?>(null) }

    val statusFilters = listOf(
        null to "ALL",
        DecisionStatus.DRAFT to "DRAFT",
        DecisionStatus.CONTEXT_REQUIRED to "CONTEXT",
        DecisionStatus.FRAMING to "FRAMING",
        DecisionStatus.READY_FOR_DEBATE to "READY",
        DecisionStatus.DEBATING to "DEBATING",
        DecisionStatus.SYNTHESIS to "SYNTHESIS",
        DecisionStatus.HUMAN_REVIEW to "REVIEW",
        DecisionStatus.APPROVED to "APPROVED",
        DecisionStatus.OUTCOME_TRACKING to "OUTCOME"
    )

    val filteredDecisions = decisions.filter { d ->
        val matchesQuery = searchQuery.isBlank() ||
                d.title.contains(searchQuery, ignoreCase = true) ||
                d.problemStatement.contains(searchQuery, ignoreCase = true) ||
                d.id.contains(searchQuery, ignoreCase = true)
        val matchesStatus = selectedStatusFilter == null || d.status == selectedStatusFilter
        matchesQuery && matchesStatus
    }.sortedByDescending { it.updatedTimestamp }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            // Header Bar & Branch Selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = strings.workspaceDecisionsTitle,
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "পোর্টফোলিও এবং গভর্নেন্স পাইপলাইন • শাখা: $activeBranchId",
                        color = CyanTelemetry,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Branch Tag
                Box(
                    modifier = Modifier
                        .background(IndigoNexus.copy(alpha = 0.2f), shape = RoundedCornerShape(6.dp))
                        .border(1.dp, IndigoNexus, shape = RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "BRANCH: $activeBranchId",
                        color = CyanTelemetry,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text(strings.searchDecisionsPlaceholder, color = TextMuted, fontSize = 11.sp)
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = CyanTelemetry,
                        modifier = Modifier.size(18.dp)
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CyanTelemetry,
                    unfocusedBorderColor = BorderHairline,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("search_decisions_input"),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Filter Chips Row
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(statusFilters) { (status, label) ->
                    val isSelected = selectedStatusFilter == status
                    Box(
                        modifier = Modifier
                            .background(
                                if (isSelected) IndigoNexus else SlateSurface,
                                shape = RoundedCornerShape(6.dp)
                            )
                            .border(
                                1.dp,
                                if (isSelected) CyanTelemetry else BorderHairline,
                                shape = RoundedCornerShape(6.dp)
                            )
                            .clickable { selectedStatusFilter = status }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = label,
                            color = if (isSelected) Color.White else TextMuted,
                            fontSize = 9.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Decision Count
            Text(
                text = "${filteredDecisions.size} টি সিদ্ধান্ত পাওয়া গেছে",
                color = TextMuted,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Decisions List
            if (filteredDecisions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(SlateSurface, shape = RoundedCornerShape(10.dp))
                        .border(1.dp, BorderHairline, shape = RoundedCornerShape(10.dp))
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = strings.emptyDecisionsPrompt,
                            color = TextSecondary,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "নতুন সিদ্ধান্ত তৈরি করতে নিচের '+' বাটনে ট্যাপ করুন।",
                            color = TextMuted,
                            fontSize = 10.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    items(filteredDecisions, key = { it.id }) { decision ->
                        DecisionListItemCard(
                            decision = decision,
                            onClick = { onSelectDecision(decision) },
                            onOpenWarRoom = { onOpenWarRoom(decision) },
                            onOpenReview = { onOpenReview(decision) },
                            onOpenBlueprint = { onOpenBlueprint(decision) },
                            onOpenOutcome = { onOpenOutcome(decision) }
                        )
                    }
                }
            }
        }

        // Floating Action Button for New Decision
        FloatingActionButton(
            onClick = onNewDecision,
            containerColor = IndigoNexus,
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("new_decision_fab")
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = strings.btnNewDecision,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun DecisionListItemCard(
    decision: DecisionEntity,
    onClick: () -> Unit,
    onOpenWarRoom: () -> Unit,
    onOpenReview: () -> Unit,
    onOpenBlueprint: () -> Unit,
    onOpenOutcome: () -> Unit,
    modifier: Modifier = Modifier
) {
    val statusColor = when (decision.status) {
        DecisionStatus.APPROVED -> EmeraldGate
        DecisionStatus.HUMAN_REVIEW -> CyanTelemetry
        DecisionStatus.DEBATING, DecisionStatus.CRITIQUE -> IndigoNexus
        DecisionStatus.OUTCOME_TRACKING -> Color(0xFF38BDF8)
        DecisionStatus.REJECTED -> Color(0xFFEF4444)
        else -> AmberWarning
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(SlateSurfaceElevated, shape = RoundedCornerShape(10.dp))
            .border(1.dp, BorderHairline, shape = RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(12.dp)
    ) {
        // Top row: ID, Status chip, Complexity badge
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = decision.id,
                    color = CyanTelemetry,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .background(statusColor.copy(alpha = 0.15f), shape = RoundedCornerShape(4.dp))
                        .border(1.dp, statusColor, shape = RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = decision.status,
                        color = statusColor,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                ComplexityBadge(tier = decision.complexityTier)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "DQS: ${String.format("%.2f", decision.dqsScore)}",
                    color = if (decision.dqsScore >= 0.80) EmeraldGate else TextMuted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Title
        Text(
            text = decision.title,
            color = TextPrimary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Problem statement preview
        Text(
            text = decision.problemStatement,
            color = TextSecondary,
            fontSize = 11.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            lineHeight = 14.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Revision & Provenance meta
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "REV #${decision.revisionNumber}",
                    color = TextMuted,
                    fontSize = 8.sp,
                    fontFamily = FontFamily.Monospace
                )
                decision.revisionHash?.let { hash ->
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "HASH: ${hash.take(8)}...",
                        color = CyanTelemetry.copy(alpha = 0.7f),
                        fontSize = 8.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Quick navigation icon buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // War Room
                Box(
                    modifier = Modifier
                        .clickable { onOpenWarRoom() }
                        .background(SlateSurface, shape = RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = "War Room",
                            tint = CyanTelemetry,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("ডিবেট", color = CyanTelemetry, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                    }
                }

                // CEO Review Gate
                Box(
                    modifier = Modifier
                        .clickable { onOpenReview() }
                        .background(SlateSurface, shape = RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Gavel,
                            contentDescription = "Review",
                            tint = EmeraldGate,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("রিভিউ", color = EmeraldGate, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                    }
                }

                // Blueprint DAG
                Box(
                    modifier = Modifier
                        .clickable { onOpenBlueprint() }
                        .background(SlateSurface, shape = RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Hub,
                            contentDescription = "Blueprint",
                            tint = IndigoNexus,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("DAG", color = Color(0xFFA5B4FC), fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                    }
                }

                // Outcome
                Box(
                    modifier = Modifier
                        .clickable { onOpenOutcome() }
                        .background(SlateSurface, shape = RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Timeline,
                            contentDescription = "Outcome",
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("আউটকাম", color = Color(0xFF38BDF8), fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }
    }
}
