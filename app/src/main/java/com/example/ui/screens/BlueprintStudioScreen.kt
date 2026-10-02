package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.ReviewPacket
import com.example.core.math.AdmissionTestResult
import com.example.core.math.FinalizationCertificate
import com.example.data.local.ClaimType
import com.example.data.local.DecisionEntity
import com.example.data.local.DecisionEventEntity
import com.example.data.local.OutcomeDivergence
import com.example.services.ExportService
import com.example.ui.localization.LocalAppStrings
import com.example.ui.BlueprintLens
import com.example.ui.components.ApprovalGovernanceSheet
import com.example.ui.components.HapticFeedbackHelper
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
import com.nirzor.kingmaker.data.AppDatabase
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * KingMaker v7.0: Living Blueprint Studio & 5-Lens Projection (Section 16, 53.4, 57).
 * Lenses: Executive, Architecture, UX, Developer, Governance.
 * Replaces swipe slider with audited ApprovalGovernanceSheet.
 */
@Composable
fun BlueprintStudioScreen(
    decision: DecisionEntity,
    events: List<DecisionEventEntity>,
    activeLens: BlueprintLens,
    activeBranchId: String,
    signatureHash: String?,
    reviewPacket: ReviewPacket?,
    admissionResult: AdmissionTestResult?,
    certificate: FinalizationCertificate?,
    gateErrorMessage: String?,
    onSelectLens: (BlueprintLens) -> Unit,
    onSignApproved: () -> Unit = {},
    onApproveWithDetails: (selectedOption: String, rationale: String, preMortem: String?) -> Unit = { _, _, _ -> },
    onReject: (rationale: String) -> Unit = {},
    onDefer: (rationale: String) -> Unit = {},
    onRequestRevision: (instructions: String) -> Unit = {},
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val strings = LocalAppStrings.current
    val coroutineScope = rememberCoroutineScope()
    val isApproved = decision.status == "APPROVED" || signatureHash != null
    var showApprovalSheet by remember { mutableStateOf(false) }

    val exportService = remember {
        ExportService(context, AppDatabase.getInstance(context).decisionDao())
    }

    if (showApprovalSheet) {
        ApprovalGovernanceSheet(
            decision = decision,
            reviewPacket = reviewPacket,
            admissionResult = admissionResult,
            certificate = certificate,
            signatureHash = signatureHash,
            onDismiss = { showApprovalSheet = false },
            onApprove = { opt, rat, pm ->
                onApproveWithDetails(opt, rat, pm)
                onSignApproved()
            },
            onReject = onReject,
            onDefer = onDefer,
            onRequestRevision = onRequestRevision
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBg)
    ) {
        // Studio Top App Bar
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = strings.blueprintHeader,
                        color = TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .background(
                                if (isApproved) EmeraldGate.copy(alpha = 0.2f) else CyanTelemetry.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = decision.status,
                            color = if (isApproved) EmeraldGate else CyanTelemetry,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
                Text(
                    text = decision.title,
                    color = TextSecondary,
                    fontSize = 10.sp,
                    maxLines = 1
                )
            }
        }

        // Lens Selector Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            BlueprintLens.values().forEach { lens ->
                val isSelected = activeLens == lens
                Box(
                    modifier = Modifier
                        .background(
                            if (isSelected) IndigoNexus else SlateSurface,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .border(
                            1.dp,
                            if (isSelected) CyanTelemetry else BorderHairline,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable {
                            HapticFeedbackHelper.triggerSelectionClick(context)
                            onSelectLens(lens)
                        }
                        .padding(horizontal = 12.dp, vertical = 7.dp)
                ) {
                    val lensLabel = when (lens) {
                        BlueprintLens.EXECUTIVE -> strings.lensExecutive
                        BlueprintLens.ARCHITECTURE -> strings.lensArchitecture
                        BlueprintLens.UX -> strings.lensUx
                        BlueprintLens.DEVELOPER -> strings.lensDeveloper
                        BlueprintLens.GOVERNANCE -> strings.lensGovernance
                    }
                    Text(
                        text = lensLabel,
                        color = if (isSelected) Color.White else TextMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // Scrollable Body
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            if (gateErrorMessage != null) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(CrimsonAlert.copy(alpha = 0.15f), shape = RoundedCornerShape(8.dp))
                            .border(1.dp, CrimsonAlert, shape = RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = CrimsonAlert, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = gateErrorMessage, color = CrimsonAlert, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }

            // D6 Synthesis Review Packet Card
            item {
                ReviewPacketCard(packet = reviewPacket, decision = decision)
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Admission Test & Finalization Certificate Card
            item {
                AdmissionTestCard(
                    admissionResult = admissionResult,
                    certificate = certificate,
                    isApproved = isApproved
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // 5 Lenses Studio (Grounded in actual decision data)
            item {
                when (activeLens) {
                    BlueprintLens.EXECUTIVE -> ExecutiveLensView(decision, activeBranchId)
                    BlueprintLens.ARCHITECTURE -> ArchitectureLensView(decision, activeBranchId)
                    BlueprintLens.UX -> UxLensView(decision, activeBranchId)
                    BlueprintLens.DEVELOPER -> DeveloperLensView(decision, activeBranchId)
                    BlueprintLens.GOVERNANCE -> GovernanceLensView(
                        decision = decision,
                        events = events,
                        signatureHash = signatureHash,
                        branchId = activeBranchId,
                        onExportJson = {
                            HapticFeedbackHelper.triggerLightImpact(context)
                            coroutineScope.launch {
                                exportService.shareLivingBlueprintJson(decision.projectId, activeBranchId, decision.id)
                            }
                        },
                        onCopyJson = {
                            HapticFeedbackHelper.triggerLightImpact(context)
                            coroutineScope.launch {
                                exportService.copyJsonToClipboard(decision.projectId, activeBranchId, decision.id)
                            }
                        }
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Section 53.4: CEO Gate Governance Trigger
            item {
                if (isApproved) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(EmeraldGate.copy(alpha = 0.12f), shape = RoundedCornerShape(10.dp))
                            .border(1.dp, EmeraldGate, shape = RoundedCornerShape(10.dp))
                            .padding(14.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldGate, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "STATUS: APPROVED & ATTESTED BY HUMAN CEO",
                                    color = EmeraldGate,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            if (signatureHash != null) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "SHA-256 ATTESTATION HASH: $signatureHash",
                                    color = TextMuted,
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            if (decision.approvalRationale != null) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "RATIONALE: \"${decision.approvalRationale}\"",
                                    color = TextPrimary,
                                    fontSize = 10.sp
                                )
                            }
                            if (decision.preMortemRationale != null) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "PRE-MORTEM: \"${decision.preMortemRationale}\"",
                                    color = GoldWarning,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                } else {
                    Button(
                        onClick = { showApprovalSheet = true },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldGate),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Fingerprint, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${strings.btnSignApproveCeo} >>",
                            color = Color.Black,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun ReviewPacketCard(packet: ReviewPacket?, decision: DecisionEntity) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
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
                    text = "D6 SYNTHESIS REVIEW PACKET",
                    color = CyanTelemetry,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "MODE: [SIMULATED]",
                    color = AmberFlame,
                    fontSize = 8.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            if (packet != null) {
                Text(
                    text = packet.summary,
                    color = TextPrimary,
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "RECOMMENDED ACTION: ${packet.recommendation}",
                    color = EmeraldGate,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "STRONGEST ALTERNATIVE: ${packet.strongestAlternative}",
                    color = CyanTelemetry,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "WHAT COULD GO WRONG: ${packet.whatCouldGoWrong}",
                    color = AmberFlame,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            } else {
                Text(
                    text = "Decision problem statement: ${decision.problemStatement}. Complete D4-D5 War Room to synthesize full review packet.",
                    color = TextMuted,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
            }
        }
    }
}

@Composable
fun AdmissionTestCard(
    admissionResult: AdmissionTestResult?,
    certificate: FinalizationCertificate?,
    isApproved: Boolean
) {
    val passed = certificate != null || admissionResult?.allPassed == true
    val statusColor = if (passed) EmeraldGate else AmberFlame

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(SlateSurface, shape = RoundedCornerShape(10.dp))
            .border(1.dp, if (passed) EmeraldGate.copy(alpha = 0.5f) else BorderHairline, shape = RoundedCornerShape(10.dp))
            .padding(14.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (passed) Icons.Default.CheckCircle else Icons.Default.Shield,
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "ADMISSION TEST & CERTIFICATE (D7)",
                        color = statusColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Text(
                    text = if (passed) "ALL PASSED" else "PENDING",
                    color = statusColor,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Criteria: Specificity, Novelty, Actionability, Value. Must pass prior to human approval signature.",
                color = TextMuted,
                fontSize = 10.sp
            )

            if (certificate != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "CERTIFICATE ID: ${certificate.certificateId}",
                    color = CyanTelemetry,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "HASH: ${certificate.certificateHash}",
                    color = TextMuted,
                    fontSize = 8.sp,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun ExecutiveLensView(decision: DecisionEntity, branchId: String) {
    LensCard(title = "EXECUTIVE LENS: STRATEGIC IMPACT & ROI [$branchId]") {
        MetricRow("OBJECTIVE", decision.title)
        MetricRow("COMPLEXITY TIER", "${decision.complexityTier} (${String.format("%.2f", decision.complexityScore)})")
        MetricRow("RISK EXPOSURE", "${(decision.risk * 100).toInt()}%")
        MetricRow("FINANCIAL BUDGET", "${(decision.budget * 100).toInt()}% Envelope")
        MetricRow("CHANGEABILITY", if (decision.changeability >= 0.5) "Two-Way Door (Reversible)" else "One-Way Door (High Commitment)")
    }
}

@Composable
fun ArchitectureLensView(decision: DecisionEntity, branchId: String) {
    LensCard(title = "ARCHITECTURE LENS: COMPONENT TOPOLOGY [$branchId]") {
        MetricRow("PRIMARY PATTERN", "Append-Only Event Sourcing / Reactive State Machine")
        MetricRow("PERSISTENCE ENGINE", "Android Room SQLite (WAL Mode Active)")
        MetricRow("EVIDENCE GRADE", decision.evidenceType)
        MetricRow("GRAPH REVISION HASH", (decision.revisionHash ?: "HEAD-UNSEALED").take(18) + "...")
    }
}

@Composable
fun UxLensView(decision: DecisionEntity, branchId: String) {
    LensCard(title = "UX LENS: HUMAN COGNITION & USABILITY [$branchId]") {
        MetricRow("COGNITIVE LOAD", "Progressive Disclosure (Details Collapsed by Default)")
        MetricRow("ACCESSIBILITY", "48dp Touch Targets • TalkBack Semantics Enabled")
        MetricRow("LANGUAGE POSTURE", "Bangla + English Dual-Language Explanations")
        MetricRow("GOVERNANCE UI", "Modal Sheet Review with Explicit Rationale Validation")
    }
}

@Composable
fun DeveloperLensView(decision: DecisionEntity, branchId: String) {
    LensCard(title = "DEVELOPER LENS: CODE INTERFACES & SCHEMAS [$branchId]") {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0A0F1D), shape = RoundedCornerShape(6.dp))
                .padding(10.dp)
        ) {
            Text(
                text = """
// Immutable Decision Projection
data class Decision(
    val id: String = "${decision.id}",
    val status: String = "${decision.status}",
    val revHash: String = "${decision.revisionHash?.take(12) ?: "HEAD"}",
    val policy: String = "${decision.policyVersion}"
)
                """.trimIndent(),
                color = CyanTelemetry,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
fun GovernanceLensView(
    decision: DecisionEntity,
    events: List<DecisionEventEntity>,
    signatureHash: String?,
    branchId: String,
    onExportJson: () -> Unit,
    onCopyJson: () -> Unit
) {
    LensCard(title = "GOVERNANCE LENS: AUDIT TRAIL & ADR EXPORT [$branchId]") {
        MetricRow("APPEND-ONLY LOG", "${events.size} Immutable Events")
        MetricRow("EVIDENCE TYPE", decision.evidenceType)
        MetricRow("REVISION HASH", (decision.revisionHash ?: "HEAD-SEALED").take(20) + "...")
        MetricRow("ATTESTATION STATE", if (signatureHash != null) "ATTESTED BY HUMAN CEO" else "PENDING D7 AUTHORIZATION")

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onExportJson,
                colors = ButtonDefaults.buttonColors(containerColor = IndigoNexus),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.weight(1f).height(40.dp)
            ) {
                Icon(Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("SHARE ADR", color = Color.White, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
            }

            Button(
                onClick = onCopyJson,
                colors = ButtonDefaults.buttonColors(containerColor = SlateSurfaceElevated),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.weight(1f).height(40.dp)
            ) {
                Icon(Icons.Default.ContentCopy, contentDescription = null, tint = CyanTelemetry, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("COPY ADR", color = CyanTelemetry, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
            }
        }
    }
}

@Composable
private fun LensCard(title: String, content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(SlateSurface, shape = RoundedCornerShape(10.dp))
            .border(1.dp, BorderHairline, shape = RoundedCornerShape(10.dp))
            .padding(14.dp)
    ) {
        Column {
            Text(
                text = title,
                color = CyanTelemetry,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(10.dp))
            content()
        }
    }
}

@Composable
fun MetricRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = TextMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
        Text(text = value, color = TextPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
    }
}
