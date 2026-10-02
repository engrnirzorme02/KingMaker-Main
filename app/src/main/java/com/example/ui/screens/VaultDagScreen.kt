package com.example.ui.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CallSplit
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.graph.CycleDetectionResult
import com.example.data.local.DecisionBranchEntity
import com.example.data.local.DecisionEdgeEntity
import com.example.data.local.DecisionEntity
import com.example.data.local.ResolutionTaskEntity
import com.example.ui.localization.AppLanguage
import com.example.ui.localization.LocalAppStrings
import com.example.ui.components.ComplexityBadge
import com.example.ui.components.ForkTimelineDialog
import com.example.ui.components.HapticFeedbackHelper
import com.example.ui.components.HeaderCockpitBar
import com.example.ui.components.InteractiveDagView
import com.example.ui.components.VisualBranchTree
import com.example.ui.theme.AmberFlame
import com.example.ui.theme.BorderHairline
import com.example.ui.theme.CrimsonAlert
import com.example.ui.theme.CyanTelemetry
import com.example.ui.theme.EmeraldGate
import com.example.ui.theme.IndigoNexus
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.SlateSurface
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun VaultDagScreen(
    decisions: List<DecisionEntity>,
    branches: List<DecisionBranchEntity>,
    activeBranchId: String,
    edges: List<DecisionEdgeEntity>,
    selectedDecision: DecisionEntity?,
    rippleHighlightedNodes: Set<String>,
    cycleDetection: CycleDetectionResult?,
    pendingTasks: List<ResolutionTaskEntity> = emptyList(),
    onSelectBranch: (String) -> Unit,
    onForkTimeline: (sourceDecisionId: String, branchName: String, title: String) -> Unit,
    onSelectDecision: (DecisionEntity) -> Unit,
    onTestRipple: (String) -> Unit,
    onOpenWarRoom: (DecisionEntity) -> Unit,
    onOpenBlueprint: (DecisionEntity) -> Unit,
    onNewDecisionStream: () -> Unit,
    onTriggerDeadlockDemo: () -> Unit,
    onResolveCycleTask: (taskId: String, note: String, breakFromId: String?, breakToId: String?) -> Unit = { _, _, _, _ -> },
    userEmail: String = "engr.nirzor.me.02@gmail.com",
    cloudSyncStatus: String = "FIREBASE READY",
    isSyncing: Boolean = false,
    currentLanguage: AppLanguage = AppLanguage.BN,
    onSelectLanguage: (AppLanguage) -> Unit = {},
    onSyncToCloud: () -> Unit = {},
    onLoginAccount: (email: String, pass: String) -> Unit = { _, _ -> },
    onPurgeWorkspace: () -> Unit = {},
    apiSettings: com.example.services.ApiSettings? = null,
    onSaveApiSettings: (apiKey: String, endpoint: String, model: String, enabled: Boolean) -> Unit = { _, _, _, _ -> },
    onTestApiConnection: suspend (apiKey: String) -> Pair<Boolean, String> = { Pair(true, "") },
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val context = LocalContext.current
    var showForkDialog by remember { mutableStateOf(false) }
    var forkingSourceDecision by remember { mutableStateOf<DecisionEntity?>(null) }
    var forkErrorMessage by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBg)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            // Header Cockpit Bar with Firebase Account & Sync
            item {
                HeaderCockpitBar(
                    decisions = decisions,
                    userEmail = userEmail,
                    cloudSyncStatus = cloudSyncStatus,
                    isSyncing = isSyncing,
                    currentLanguage = currentLanguage,
                    onSelectLanguage = onSelectLanguage,
                    onTriggerDeadlockDemo = onTriggerDeadlockDemo,
                    onSyncToCloud = onSyncToCloud,
                    onLoginAccount = onLoginAccount,
                    onPurgeWorkspace = onPurgeWorkspace,
                    apiSettings = apiSettings,
                    onSaveApiSettings = onSaveApiSettings,
                    onTestApiConnection = onTestApiConnection
                )
            }

            // Directive 8: Resolution Task Alert Card for DAG Cycles
            if (pendingTasks.isNotEmpty()) {
                item {
                    val task = pendingTasks.first()
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                            .background(Color(0xFF2B1015), shape = RoundedCornerShape(10.dp))
                            .border(1.dp, CrimsonAlert, shape = RoundedCornerShape(10.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = CrimsonAlert, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "HUMAN RESOLUTION TASK: TARJAN CYCLE DEADLOCK",
                                    color = CrimsonAlert,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = task.reason,
                                color = TextPrimary,
                                fontSize = 10.sp,
                                lineHeight = 14.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Weakest Node Identified: '${task.weakestNodeId}' (marked as ASSUMPTION). AI will not auto-delete.",
                                color = AmberFlame,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    HapticFeedbackHelper.triggerMediumImpact(context)
                                    // Human breaks cycle edge from ADR-001 to ADR-003
                                    onResolveCycleTask(task.id, "Human resolution: severed cyclic feedback edge ADR-001 -> ADR-003", "ADR-001", "ADR-003")
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CrimsonAlert),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Icon(Icons.Default.Build, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("BREAK WEAKEST CYCLE EDGE (HUMAN RESOLUTION)", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Visual Branch Tree
            item {
                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                    VisualBranchTree(
                        branches = branches,
                        activeBranchId = activeBranchId,
                        onSelectBranch = onSelectBranch,
                        onOpenForkDialog = {
                            val candidate = selectedDecision ?: decisions.firstOrNull { it.status == "APPROVED" || it.status == "D4_DEBATE" }
                            if (candidate != null) {
                                val eligible = candidate.status in setOf("D4_DEBATE", "D5_CRITIQUE", "D6_SYNTHESIS", "D7_REVIEW", "APPROVED")
                                if (eligible) {
                                    forkErrorMessage = null
                                    forkingSourceDecision = candidate
                                    showForkDialog = true
                                } else {
                                    forkErrorMessage = "Forking Governance Guard: Decisions must reach D4+ maturity before timeline forking."
                                }
                            }
                        }
                    )
                }
            }

            // Interactive Topological DAG Visualizer
            item {
                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                    InteractiveDagView(
                        decisions = decisions,
                        edges = edges,
                        selectedDecisionId = selectedDecision?.id,
                        rippleHighlightedIds = rippleHighlightedNodes,
                        cycleResult = cycleDetection,
                        onSelectDecision = onSelectDecision,
                        onTestRipple = onTestRipple
                    )
                }
            }

            // Section Title
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "DECISION NODES (${decisions.size})",
                        color = TextMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "BRANCH: $activeBranchId",
                        color = CyanTelemetry,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Clean Workspace Empty State (when no decisions exist)
            if (decisions.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .background(SlateSurface, shape = RoundedCornerShape(12.dp))
                            .border(1.dp, BorderHairline, shape = RoundedCornerShape(12.dp))
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(IndigoNexus.copy(alpha = 0.2f), shape = RoundedCornerShape(10.dp))
                                    .border(1.dp, IndigoNexus.copy(alpha = 0.6f), shape = RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    tint = CyanTelemetry,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "CLEAN PERSONAL WORKSPACE",
                                color = TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "সব ডেমো ডেটা মুছে ফেলা হয়েছে। আপনার প্রথম আর্কিটেকচারাল সিদ্ধান্ত শুরু করতে নিচের বাটনে ট্যাপ করুন অথবা ক্লাউড থেকে ব্যাকআপ লোড করুন।",
                                color = TextMuted,
                                fontSize = 11.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                lineHeight = 16.sp
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(
                                onClick = onNewDecisionStream,
                                colors = ButtonDefaults.buttonColors(containerColor = IndigoNexus),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("+ START FIRST DECISION", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Decision Cards List
            items(decisions, key = { it.id }) { decision ->
                val isSelected = selectedDecision?.id == decision.id
                val isAffectedByRipple = rippleHighlightedNodes.contains(decision.id)

                DecisionCardItem(
                    decision = decision,
                    isSelected = isSelected,
                    isRippleAffected = isAffectedByRipple,
                    onClick = {
                        HapticFeedbackHelper.triggerSelectionClick(context)
                        onSelectDecision(decision)
                    },
                    onOpenWarRoom = { onOpenWarRoom(decision) },
                    onOpenBlueprint = { onOpenBlueprint(decision) }
                )
            }
        }

        // Floating Action Button: New Decision Stream
        FloatingActionButton(
            onClick = {
                HapticFeedbackHelper.triggerMediumImpact(context)
                onNewDecisionStream()
            },
            containerColor = IndigoNexus,
            contentColor = Color.White,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Add, contentDescription = strings.btnNewDecision)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = strings.btnNewDecision,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // Fork Dialog
        if (showForkDialog && forkingSourceDecision != null) {
            ForkTimelineDialog(
                sourceDecision = forkingSourceDecision!!,
                onDismiss = { showForkDialog = false },
                onConfirmFork = { sourceId, branchName, title ->
                    onForkTimeline(sourceId, branchName, title)
                    showForkDialog = false
                }
            )
        }
    }
}

@Composable
fun DecisionCardItem(
    decision: DecisionEntity,
    isSelected: Boolean,
    isRippleAffected: Boolean,
    onClick: () -> Unit,
    onOpenWarRoom: () -> Unit,
    onOpenBlueprint: () -> Unit
) {
    val strings = LocalAppStrings.current
    val borderColor = when {
        isRippleAffected -> CrimsonAlert
        isSelected -> CyanTelemetry
        decision.status == "APPROVED" -> EmeraldGate.copy(alpha = 0.5f)
        else -> BorderHairline
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp)
            .background(SlateSurface, shape = RoundedCornerShape(10.dp))
            .border(if (isSelected || isRippleAffected) 1.5.dp else 1.dp, borderColor, shape = RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(14.dp)
    ) {
        Column {
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
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .background(
                                if (decision.status == "APPROVED") EmeraldGate.copy(alpha = 0.15f) else Color(0xFF1E293B),
                                shape = RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = decision.status,
                            color = if (decision.status == "APPROVED") EmeraldGate else TextSecondary,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .background(if (decision.dqsScore >= 0.80) EmeraldGate.copy(alpha = 0.15f) else CyanTelemetry.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                        .border(1.dp, if (decision.dqsScore >= 0.80) EmeraldGate else CyanTelemetry, RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "${(decision.dqsScore * 100).toInt()}% Q-Score",
                        color = if (decision.dqsScore >= 0.80) EmeraldGate else CyanTelemetry,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = decision.title,
                color = TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = decision.problemStatement,
                color = TextSecondary,
                fontSize = 11.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 15.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "EVIDENCE: ${decision.evidenceType}",
                    color = TextMuted,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(
                        modifier = Modifier
                            .background(IndigoNexus.copy(alpha = 0.2f), shape = RoundedCornerShape(4.dp))
                            .border(1.dp, IndigoNexus.copy(alpha = 0.5f), shape = RoundedCornerShape(4.dp))
                            .clickable { onOpenWarRoom() }
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = strings.btnWarRoom,
                            color = CyanTelemetry,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Box(
                        modifier = Modifier
                            .background(EmeraldGate.copy(alpha = 0.15f), shape = RoundedCornerShape(4.dp))
                            .border(1.dp, EmeraldGate.copy(alpha = 0.5f), shape = RoundedCornerShape(4.dp))
                            .clickable { onOpenBlueprint() }
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = strings.btnBlueprint,
                            color = EmeraldGate,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}
