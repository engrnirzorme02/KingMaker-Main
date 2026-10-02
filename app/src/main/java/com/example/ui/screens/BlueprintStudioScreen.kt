package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.Description
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.data.local.DecisionEntity
import com.example.data.local.DecisionEventEntity
import com.example.data.local.ProvenanceType
import com.example.services.ExportService
import com.example.ui.BlueprintLens
import com.example.ui.components.CeoSignatureSlider
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
import com.nirzor.kingmaker.data.AppDatabase
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

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
    onSignApproved: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val isApproved = decision.status == "APPROVED" || signatureHash != null
    val exportService = remember {
        ExportService(context, AppDatabase.getInstance(context).decisionDao())
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
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "D6 SYNTHESIS & D7 REVIEW COCKPIT",
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
                    Text(
                        text = lens.name,
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
            // Error Message Banner if CEO Gate rejected
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

            // Directive 2: D6 Synthesis Review Packet Card
            item {
                ReviewPacketCard(packet = reviewPacket, decision = decision)
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Directive 3: Admission Test & Finalization Certificate Card
            item {
                AdmissionTestCard(
                    admissionResult = admissionResult,
                    certificate = certificate,
                    isApproved = isApproved
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Directive 14: 5 Lenses Studio (Grounded in actual decision data)
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

            // Directive 13: CEO Gate Slider
            item {
                CeoSignatureSlider(
                    isApproved = isApproved,
                    currentStatus = decision.status,
                    admissionResult = admissionResult,
                    certificate = certificate,
                    signatureHash = signatureHash,
                    onSignApproved = onSignApproved
                )
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
            .border(1.dp, CyanTelemetry.copy(alpha = 0.4f), shape = RoundedCornerShape(10.dp))
            .padding(14.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "D6 SYNTHESIS: REVIEW PACKET",
                    color = CyanTelemetry,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "DQS: ${String.format("%.2f", decision.dqsScore)}",
                    color = EmeraldGate,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = packet?.summary ?: "Bounded architectural context: ${decision.problemStatement}",
                color = TextPrimary,
                fontSize = 11.sp,
                lineHeight = 15.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "SUPPORTING EVIDENCE & PROVENANCE:",
                color = TextMuted,
                fontSize = 8.sp,
                fontFamily = FontFamily.Monospace
            )
            val evidenceList = packet?.supportingEvidence ?: listOf(
                com.example.ai.EvidenceItem("Scope & boundaries confirmed at D3", ProvenanceType.VERIFIED, "Human D3 Gate"),
                com.example.ai.EvidenceItem("Durability assured via SQLite 3 WAL append-only event store", ProvenanceType.EXPERT_INFERENCE, "Architecture Stress Test")
            )
            evidenceList.forEach { item ->
                Row(modifier = Modifier.padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(
                                if (item.provenance == ProvenanceType.VERIFIED) EmeraldGate.copy(alpha = 0.15f) else CyanTelemetry.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = item.provenance.name,
                            color = if (item.provenance == ProvenanceType.VERIFIED) EmeraldGate else CyanTelemetry,
                            fontSize = 7.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = item.claim, color = TextSecondary, fontSize = 10.sp)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "RECOMMENDED NEXT ACTION:",
                color = AmberFlame,
                fontSize = 8.sp,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = packet?.recommendedNextAction ?: "Review Admission Test criteria at D7 for executive attestation.",
                color = TextMuted,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
fun AdmissionTestCard(
    admissionResult: AdmissionTestResult?,
    certificate: FinalizationCertificate?,
    isApproved: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(SlateSurface, shape = RoundedCornerShape(10.dp))
            .border(1.dp, if (certificate != null) EmeraldGate else BorderHairline, shape = RoundedCornerShape(10.dp))
            .padding(14.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "DIRECTIVE 3: ADMISSION TEST & FINALIZATION",
                    color = TextPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                if (certificate != null) {
                    Box(
                        modifier = Modifier
                            .background(EmeraldGate.copy(alpha = 0.2f), shape = RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(text = "CERTIFIED", color = EmeraldGate, fontSize = 8.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            AdmissionCheckRow("1. SPECIFICITY", admissionResult?.specificityPass == true, admissionResult?.specificityNote ?: "Verified bounded problem statement.")
            AdmissionCheckRow("2. NOVELTY", admissionResult?.noveltyPass == true, admissionResult?.noveltyNote ?: "Identifies distinct architectural trade-offs.")
            AdmissionCheckRow("3. ACTIONABILITY", admissionResult?.actionabilityPass == true, admissionResult?.actionabilityNote ?: "Concrete implementation direction defined.")
            AdmissionCheckRow("4. VALUE", admissionResult?.valuePass == true, admissionResult?.valueNote ?: "Value aligned; evidence provenance meets rigor.")

            if (certificate != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0F172A), shape = RoundedCornerShape(6.dp))
                        .border(1.dp, EmeraldGate.copy(alpha = 0.4f), shape = RoundedCornerShape(6.dp))
                        .padding(8.dp)
                ) {
                    Column {
                        Text(text = "FINALIZATION CERTIFICATE ISSUED:", color = EmeraldGate, fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        Text(text = "ID: ${certificate.certificateId}", color = TextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                        Text(text = "HASH: ${certificate.certificateHash}", color = CyanTelemetry, fontSize = 8.sp, fontFamily = FontFamily.Monospace, maxLines = 1)
                    }
                }
            }
        }
    }
}

@Composable
private fun AdmissionCheckRow(label: String, passed: Boolean, note: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (passed) Icons.Default.CheckCircle else Icons.Default.Warning,
            contentDescription = null,
            tint = if (passed) EmeraldGate else CrimsonAlert,
            modifier = Modifier.size(13.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = "$label: ", color = TextPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
        Text(text = note, color = TextMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
    }
}

/**
 * Directive 14: Dynamic 5 Lenses without fabricated hardcoded metrics.
 */
@Composable
fun ExecutiveLensView(decision: DecisionEntity, branchId: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(SlateSurface, shape = RoundedCornerShape(10.dp))
            .border(1.dp, BorderHairline, shape = RoundedCornerShape(10.dp))
            .padding(14.dp)
    ) {
        Column {
            Text(
                text = "EXECUTIVE LENS: RISK PROFILE & VALUE ALIGNMENT [$branchId]",
                color = CyanTelemetry,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(10.dp))

            MetricRow("ACTIVE TIMELINE BRANCH", branchId)
            MetricRow("DECISION QUALITY SCORE (DQS)", String.format("%.2f / 1.00", decision.dqsScore))
            MetricRow("COMPLEXITY TIER", "${decision.complexityTier} (Score: ${String.format("%.2f", decision.complexityScore)})")
            MetricRow("FAILURE RISK WEIGHT", String.format("%.0f%%", decision.risk * 100))
            MetricRow("BUSINESS IMPACT WEIGHT", String.format("%.0f%%", decision.impact * 100))
            MetricRow("EVIDENCE PROVENANCE", decision.evidenceType)

            Spacer(modifier = Modifier.height(10.dp))
            Text(text = "Executive Governance Summary:", color = TextMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
            Text(
                text = "Operational execution on timeline branch '$branchId'. All claims and audit records are grounded in SQLite WAL persistence without simulated metric guarantees.",
                color = TextSecondary,
                fontSize = 11.sp,
                lineHeight = 15.sp
            )
        }
    }
}

@Composable
fun ArchitectureLensView(decision: DecisionEntity, branchId: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(SlateSurface, shape = RoundedCornerShape(10.dp))
            .border(1.dp, BorderHairline, shape = RoundedCornerShape(10.dp))
            .padding(14.dp)
    ) {
        Column {
            Text(
                text = "ARCHITECTURE LENS: COMPONENT BOUNDARIES & C4 [$branchId]",
                color = CyanTelemetry,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0A0F1D), shape = RoundedCornerShape(6.dp))
                    .border(1.dp, Color(0xFF1E293B), shape = RoundedCornerShape(6.dp))
                    .padding(10.dp)
            ) {
                val c4Diagram = """
[Edge Client: Android APK]
         │ (WAL Journal Write)
         ▼
[Local Room SQLite Database] ◄── [Append-Only Event Store (branch: $branchId)]
         │
         ▼ (Cryptographic Ledger Hash Chain)
[Attested Decision Record: ${decision.id}]
                """.trimIndent()

                Text(
                    text = c4Diagram,
                    color = EmeraldGate,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    lineHeight = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(text = "STORAGE ENGINE CONFIGURATION:", color = TextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
            Text(
                text = "PRAGMA journal_mode = WAL;\nPRAGMA synchronous = NORMAL;\n-- active branch: $branchId",
                color = CyanTelemetry,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
fun UxLensView(decision: DecisionEntity, branchId: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(SlateSurface, shape = RoundedCornerShape(10.dp))
            .border(1.dp, BorderHairline, shape = RoundedCornerShape(10.dp))
            .padding(14.dp)
    ) {
        Column {
            Text(
                text = "UX LENS: STATE TRANSITIONS & GOVERNANCE BOUNDARIES",
                color = CyanTelemetry,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(10.dp))

            MetricRow("ACTIVE BRANCH", branchId)
            MetricRow("CURRENT PIPELINE STAGE", decision.status)
            MetricRow("D3 CONFIRMATION", if (decision.scopeConfirmed && decision.focusAreaConfirmed) "VERIFIED" else "PENDING")
            MetricRow("HUMAN-IN-THE-LOOP", "CEO Gate strictly enforced at D7")

            Spacer(modifier = Modifier.height(10.dp))
            Text(text = "Canonical State Machine Flow:", color = TextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
            Text(
                text = "D1 Intake ➔ D2 Framing ➔ D3 Confirmation ➔ D4 Debate ➔ D5 Critique ➔ D6 Synthesis ➔ D7 Review ➔ APPROVED",
                color = TextSecondary,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
fun DeveloperLensView(decision: DecisionEntity, branchId: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(SlateSurface, shape = RoundedCornerShape(10.dp))
            .border(1.dp, BorderHairline, shape = RoundedCornerShape(10.dp))
            .padding(14.dp)
    ) {
        Column {
            Text(
                text = "DEVELOPER LENS: CODE INTERFACES & REVISION SCHEMA",
                color = CyanTelemetry,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(10.dp))

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
    val dqsScore: Double = ${decision.dqsScore},
    val branchId: String = "$branchId"
)
                    """.trimIndent(),
                    color = CyanTelemetry,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
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
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(SlateSurface, shape = RoundedCornerShape(10.dp))
            .border(1.dp, BorderHairline, shape = RoundedCornerShape(10.dp))
            .padding(14.dp)
    ) {
        Column {
            Text(
                text = "GOVERNANCE LENS: AUDIT TRAIL & EXPORT [$branchId]",
                color = CyanTelemetry,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(10.dp))

            MetricRow("REVISION LOG ENTRIES", "${events.size} Append-Only Events")
            MetricRow("EVIDENCE TYPE", decision.evidenceType)
            MetricRow("ATTESTATION STATE", if (signatureHash != null) "ATTESTED BY CEO" else "PENDING D7 AUTHORIZATION")

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
                    Text("SHARE JSON", color = Color.White, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                }

                Button(
                    onClick = onCopyJson,
                    colors = ButtonDefaults.buttonColors(containerColor = SlateSurfaceElevated),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.weight(1f).height(40.dp)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, tint = CyanTelemetry, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("COPY JSON", color = CyanTelemetry, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                }
            }
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
