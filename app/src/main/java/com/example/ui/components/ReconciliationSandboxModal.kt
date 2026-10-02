package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallMerge
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.graph.SemanticGraphDiff
import com.example.data.local.ConflictClass
import com.example.ui.theme.BorderHairline
import com.example.ui.theme.CrimsonAlert
import com.example.ui.theme.CyanTelemetry
import com.example.ui.theme.EmeraldGate
import com.example.ui.theme.GoldWarning
import com.example.ui.theme.IndigoNexus
import com.example.ui.theme.SlateSurface
import com.example.ui.theme.SlateSurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary

/**
 * KingMaker v7.0: Section 52 Conflict Reconciliation Sandbox.
 * Displays semantic diff between offline local outbox operations and live server state.
 */
@Composable
fun ReconciliationSandboxModal(
    diff: SemanticGraphDiff,
    onKeepServer: () -> Unit,
    onKeepLocalAsDraft: () -> Unit,
    onMergeAndReview: () -> Unit,
    onDiscardLocal: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val conflictLabel = when (diff.conflictClass) {
        ConflictClass.NON_OVERLAPPING -> "A: NON-OVERLAPPING GRAPH CHANGES"
        ConflictClass.SAME_NODE_FIELD_CONFLICT -> "B: SAME-NODE FIELD CONFLICT"
        ConflictClass.SAME_EDGE_CONFLICT -> "C: SAME-EDGE RELATIONSHIP CONFLICT"
        ConflictClass.TOPOLOGY_CONFLICT -> "D: TOPOLOGY STRUCTURAL CONFLICT"
        ConflictClass.CYCLE_CONFLICT -> "E: DEPENDENCY-CYCLE CONFLICT"
        ConflictClass.POLICY_APPROVAL_INVALIDATION -> "F: POLICY / APPROVAL INVALIDATION"
        ConflictClass.HISTORICAL_SUPERSESSION -> "G: HISTORICAL SUPERSESSION"
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CompareArrows,
                    contentDescription = null,
                    tint = if (diff.requiresManualReconciliation) GoldWarning else EmeraldGate,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "GRAPH RECONCILIATION SANDBOX",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "অফলাইন বনাম সার্ভার গ্রাফ রিকনসিলিয়েশন (Section 52)",
                        color = TextMuted,
                        fontSize = 10.sp
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Conflict Class Badge
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (diff.requiresManualReconciliation) GoldWarning.copy(alpha = 0.15f) else EmeraldGate.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .border(
                            1.dp,
                            if (diff.requiresManualReconciliation) GoldWarning else EmeraldGate,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(10.dp)
                ) {
                    Column {
                        Text(
                            text = conflictLabel,
                            color = if (diff.requiresManualReconciliation) GoldWarning else EmeraldGate,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = diff.reconciliationExplanation,
                            color = TextPrimary,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Affected Subgraph
                Text(
                    text = "প্রভাবিত নোডসমূহ (${diff.affectedNodeIds.size}):",
                    color = TextPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (diff.affectedNodeIds.isEmpty()) "কোনো নোড দ্বন্দ্ব নেই।" else diff.affectedNodeIds.joinToString(", "),
                    color = CyanTelemetry,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )

                if (diff.conflictingEdges.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "দ্বন্দ্বযুক্ত এজসমূহ (Conflicting Edges):",
                        color = CrimsonAlert,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    diff.conflictingEdges.forEach { (edge, reason) ->
                        Text(
                            text = "• ${edge.from} -> ${edge.to}: $reason",
                            color = TextMuted,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SlateSurfaceElevated, shape = RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Text(
                        text = "নীতিগত নিয়ম (Invariant I-26): কোনো অবস্থাতেই সার্ভারের গ্রাফ জোরপূর্বক ওভাররাইট করা হয় না। সফল মার্জও প্রথমে খসড়া হিসেবে থাকে।",
                        color = TextMuted,
                        fontSize = 10.sp,
                        lineHeight = 14.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onMergeAndReview,
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGate),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.CallMerge, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("MERGE & REVIEW DRAFT", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedButton(
                    onClick = onKeepServer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("KEEP SERVER", fontSize = 10.sp, color = TextMuted)
                }
                OutlinedButton(
                    onClick = onKeepLocalAsDraft,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("LOCAL AS DRAFT", fontSize = 10.sp, color = CyanTelemetry)
                }
            }
        },
        containerColor = Color(0xFF0F141C),
        shape = RoundedCornerShape(14.dp),
        modifier = modifier
    )
}
