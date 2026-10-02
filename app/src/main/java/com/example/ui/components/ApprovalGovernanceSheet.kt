package com.example.ui.components

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.ReviewPacket
import com.example.core.math.AdmissionTestResult
import com.example.core.math.FinalizationCertificate
import com.example.data.local.DecisionEntity
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
 * KingMaker v7.0: Canonical Human Approval Governance Sheet (Section 53.4).
 * Replaces legacy swipe slider with a structured, audited governance modal sheet.
 *
 * Requirements:
 * 1. Selected Option picker
 * 2. Rationale field >= 30 characters
 * 3. Pre-mortem for T3 decisions ("It's 6 months later and this failed...")
 * 4. Current revision hash display & policy eligibility check
 * 5. Review Packet binding
 * 6. Biometric / Executive Step-up confirmation
 * 7. Server confirmation
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApprovalGovernanceSheet(
    decision: DecisionEntity,
    reviewPacket: ReviewPacket?,
    admissionResult: AdmissionTestResult?,
    certificate: FinalizationCertificate?,
    signatureHash: String?,
    onDismiss: () -> Unit,
    onApprove: (selectedOption: String, rationale: String, preMortem: String?) -> Unit,
    onReject: (rationale: String) -> Unit,
    onDefer: (rationale: String) -> Unit,
    onRequestRevision: (instructions: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val options = reviewPacket?.alternatives ?: listOf(
        decision.selectedOption ?: "Primary Option",
        "Alternative Option B",
        "Maintain Status Quo"
    )

    var selectedOption by remember { mutableStateOf(decision.selectedOption ?: options.firstOrNull() ?: "") }
    var rationale by remember { mutableStateOf("") }
    var preMortem by remember { mutableStateOf("") }
    var showBiometricChallenge by remember { mutableStateOf(false) }
    var governanceAction by remember { mutableStateOf<String?>(null) }
    var validationError by remember { mutableStateOf<String?>(null) }

    val isT3 = decision.complexityTier.contains("RIGOROUS") || decision.complexityTier.contains("MAXIMUM")
    val hasValidRationale = rationale.trim().length >= 30
    val hasValidPreMortem = !isT3 || preMortem.trim().length >= 15
    val isEligible = (certificate != null || admissionResult?.allPassed == true) &&
            (decision.status == "D7_REVIEW" || decision.status == "HUMAN_REVIEW" || decision.status == "APPROVED")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF0F141C),
        dragHandle = null,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (decision.status == "APPROVED") Icons.Default.Lock else Icons.Default.Shield,
                        contentDescription = null,
                        tint = if (decision.status == "APPROVED") EmeraldGate else IndigoNexus,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (decision.status == "APPROVED") "GOVERNANCE RECORD COMMITTED" else "HUMAN CEO GOVERNANCE SHEET",
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "ইনভেরিয়েন্ট I-01/I-02: মানবীয় কর্তৃত্ব চূড়ান্ত; AI কখনো সিদ্ধান্ত অনুমোদন করে না।",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Revision Hash & Policy Pill
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SlateSurface, shape = RoundedCornerShape(8.dp))
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "REVISION HASH (RFC 8785 SHA-256)",
                        color = TextMuted,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = (decision.revisionHash ?: signatureHash ?: "HEAD-REV-UNSEALED").take(24) + "...",
                        color = CyanTelemetry,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Box(
                    modifier = Modifier
                        .background(IndigoNexus.copy(alpha = 0.2f), shape = RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = decision.policyVersion,
                        color = IndigoNexus,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Option Selection
            Text(
                text = "১. অনুমোদনের জন্য নির্বাচিত অপশন (Selected Decision Option):",
                color = TextPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                options.forEach { opt ->
                    FilterChip(
                        selected = selectedOption == opt,
                        onClick = { selectedOption = opt },
                        label = {
                            Text(
                                text = opt,
                                fontSize = 11.sp,
                                maxLines = 2
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = EmeraldGate.copy(alpha = 0.2f),
                            selectedLabelColor = EmeraldGate,
                            containerColor = SlateSurface,
                            labelColor = TextMuted
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selectedOption == opt,
                            borderColor = if (selectedOption == opt) EmeraldGate else BorderHairline,
                            selectedBorderColor = EmeraldGate
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Rationale Input (Minimum 30 characters)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "২. সিদ্ধান্তের পক্ষে যৌক্তিক ব্যাখ্যা (Rationale >= 30 chars):",
                    color = TextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${rationale.trim().length}/30",
                    color = if (hasValidRationale) EmeraldGate else GoldWarning,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = rationale,
                onValueChange = {
                    rationale = it
                    validationError = null
                },
                placeholder = {
                    Text(
                        text = "এই সিদ্ধান্ত নেওয়ার পেছনের প্রধান কারণ এবং কীভাবে ঝুঁকি মূল্যায়ন করা হয়েছে লিখুন...",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = if (hasValidRationale) EmeraldGate else GoldWarning,
                    unfocusedBorderColor = BorderHairline,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedContainerColor = SlateSurface,
                    unfocusedContainerColor = SlateSurface
                ),
                shape = RoundedCornerShape(8.dp)
            )

            // T3 Pre-Mortem (Mandatory for high-impact)
            if (isT3) {
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "৩. প্রি-মর্টেম বিশ্লেষণ (T3 Mandatory Pre-Mortem):",
                        color = GoldWarning,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${preMortem.trim().length}/15",
                        color = if (hasValidPreMortem) EmeraldGate else GoldWarning,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Text(
                    text = "“ধরুন ৬ মাস পর এই সিদ্ধান্তটি ব্যর্থ হলো। এর প্রধান কারণ কী হতে পারে?”",
                    color = TextMuted,
                    fontSize = 11.sp,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = preMortem,
                    onValueChange = {
                        preMortem = it
                        validationError = null
                    },
                    placeholder = {
                        Text(
                            text = "সবচেয়ে সম্ভাব্য ব্যর্থতার কারণ ও পূর্বপ্রস্তুতি লিখুন...",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (hasValidPreMortem) EmeraldGate else GoldWarning,
                        unfocusedBorderColor = BorderHairline,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedContainerColor = SlateSurface,
                        unfocusedContainerColor = SlateSurface
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
            }

            if (validationError != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = validationError!!,
                    color = CrimsonAlert,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons
            if (decision.status == "APPROVED") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(EmeraldGate.copy(alpha = 0.15f), shape = RoundedCornerShape(8.dp))
                        .border(1.dp, EmeraldGate, shape = RoundedCornerShape(8.dp))
                        .padding(14.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldGate, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "ADR APPROVED & LOCKED BY HUMAN CEO",
                                color = EmeraldGate,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        if (decision.approvalRationale != null) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Rationale: \"${decision.approvalRationale}\"",
                                color = TextPrimary,
                                fontSize = 11.sp
                            )
                        }
                        if (decision.digitalSignatureHash != null) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Hash: ${decision.digitalSignatureHash}",
                                color = CyanTelemetry,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Approve Button
                    Button(
                        onClick = {
                            if (!isEligible) {
                                validationError = "পূর্বশর্ত অপূর্ণ: D6 সংশ্লেষণ ও অ্যাডমিশন টেস্ট সম্পন্ন হওয়া আবশ্যক।"
                                return@Button
                            }
                            if (!hasValidRationale) {
                                validationError = "যৌক্তিক ব্যাখ্যা অন্তত ৩০ অক্ষর হতে হবে।"
                                return@Button
                            }
                            if (!hasValidPreMortem) {
                                validationError = "T3 সিদ্ধান্তের জন্য প্রি-মর্টেম অন্তত ১৫ অক্ষর হতে হবে।"
                                return@Button
                            }
                            HapticFeedbackHelper.triggerHeavyImpact(context)
                            onApprove(selectedOption, rationale.trim(), preMortem.takeIf { it.isNotBlank() })
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldGate),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Fingerprint, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "APPROVE & ATTEST IMMUTABLE ADR",
                            color = Color.Black,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    // Alternate Actions Row (Reject, Defer, Request Revision)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                if (rationale.trim().length < 10) {
                                    validationError = "বাতিলের কারণ ব্যাখ্যা করুন (কমপক্ষে ১০ অক্ষর)।"
                                    return@OutlinedButton
                                }
                                onReject(rationale)
                                onDismiss()
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = CrimsonAlert),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("REJECT", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = {
                                onDefer(rationale.ifBlank { "Deferred for further research." })
                                onDismiss()
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldWarning),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("DEFER", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = {
                                onRequestRevision(rationale.ifBlank { "Revising decision constraints." })
                                onDismiss()
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = CyanTelemetry),
                            modifier = Modifier.weight(1.2f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("REVISION", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
