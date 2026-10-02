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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
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
import com.example.core.graph.WhatIfSimulationResult
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
 * KingMaker v7.0: Section 51.3 What-if Simulation Dialog.
 * Shows preview of graph mutation without altering authoritative state.
 */
@Composable
fun WhatIfSimulationDialog(
    simulationResult: WhatIfSimulationResult,
    onConfirmMutation: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val statusColor = when {
        simulationResult.introducesCycle -> CrimsonAlert
        simulationResult.resolvesCycle -> EmeraldGate
        else -> CyanTelemetry
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = statusColor,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "WHAT-IF GRAPH SIMULATION",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "প্রস্তাবিত গ্রাফ পরিবর্তনের প্রভাব বিশ্লেষণ (Section 51.3)",
                        color = TextMuted,
                        fontSize = 10.sp
                    )
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Summary Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(statusColor.copy(alpha = 0.12f), shape = RoundedCornerShape(8.dp))
                        .border(1.dp, statusColor, shape = RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (simulationResult.introducesCycle) Icons.Default.Warning else Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = statusColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (simulationResult.introducesCycle) "CYCLE DANGER DETECTED" else "SAFE PROPOSAL",
                                color = statusColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = simulationResult.summary,
                            color = TextPrimary,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Detail Fields
                DetailRow("Candidate Edge", "${simulationResult.candidateEdge.from} -> ${simulationResult.candidateEdge.to}")
                DetailRow("Relationship", simulationResult.candidateEdge.relationship)
                DetailRow("Affected Nodes", "${simulationResult.affectedNodes.size} node(s): ${simulationResult.affectedNodes.joinToString(", ")}")
                DetailRow("Projected Graph Hash", simulationResult.projectedGraphHash.take(18) + "...")

                Spacer(modifier = Modifier.height(10.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SlateSurfaceElevated, shape = RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Text(
                        text = "ইনভেরিয়েন্ট I-20: সিমুলেশন কখনোই মূল ডাটাবেজ পরিবর্তন করে না। মানবীয় অনুমোদনের পরেই কেবল পরিবর্তন কার্যকর হবে।",
                        color = TextMuted,
                        fontSize = 10.sp,
                        lineHeight = 14.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirmMutation,
                enabled = !simulationResult.introducesCycle,
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGate),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("CONFIRM MUTATION", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("CANCEL", color = TextMuted, fontSize = 11.sp)
            }
        },
        containerColor = Color(0xFF0F141C),
        shape = RoundedCornerShape(14.dp),
        modifier = modifier
    )
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = TextMuted, fontSize = 10.sp)
        Text(
            text = value,
            color = CyanTelemetry,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Medium
        )
    }
}
