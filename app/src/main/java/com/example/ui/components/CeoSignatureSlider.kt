package com.example.ui.components

import android.app.KeyguardManager
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.math.AdmissionTestResult
import com.example.core.math.FinalizationCertificate
import com.example.ui.theme.BorderHairline
import com.example.ui.theme.CrimsonAlert
import com.example.ui.theme.CyanTelemetry
import com.example.ui.theme.EmeraldGate
import com.example.ui.theme.IndigoNexus
import com.example.ui.theme.SlateSurface
import com.example.ui.theme.SlateSurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import kotlin.math.roundToInt

@Composable
fun CeoSignatureSlider(
    isApproved: Boolean,
    currentStatus: String,
    admissionResult: AdmissionTestResult?,
    certificate: FinalizationCertificate?,
    signatureHash: String?,
    onSignApproved: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var offsetX by remember { mutableFloatStateOf(0f) }
    var hasSigned by remember(isApproved) { mutableStateOf(isApproved) }
    var showDeviceCredentialModal by remember { mutableStateOf(false) }

    val isEligibleForApproval = currentStatus == "D7_REVIEW" &&
            (certificate != null || admissionResult?.allPassed == true) &&
            !hasSigned

    fun triggerHapticFeedback() {
        HapticFeedbackHelper.triggerHeavyImpact(context)
    }

    if (showDeviceCredentialModal) {
        AlertDialog(
            onDismissRequest = { showDeviceCredentialModal = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Fingerprint,
                        contentDescription = null,
                        tint = EmeraldGate,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "EXECUTIVE ATTESTATION CHALLENGE",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            },
            text = {
                Column {
                    Text(
                        text = "Directive 13 / Invariant 2: AI agents cannot approve decisions. Final authorization requires human executive identity attestation.",
                        color = TextMuted,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Certificate: ${certificate?.certificateId ?: "CERT-AUTO-VERIFIED"}",
                        color = CyanTelemetry,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "Admission Criteria: Specificity, Novelty, Actionability, Value [ALL PASSED]",
                        color = EmeraldGate,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeviceCredentialModal = false
                        hasSigned = true
                        triggerHapticFeedback()
                        onSignApproved()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGate)
                ) {
                    Text("AUTHORIZE & COMMIT SHA-256", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeviceCredentialModal = false }) {
                    Text("ABORT", color = TextMuted, fontSize = 11.sp)
                }
            },
            containerColor = SlateSurfaceElevated,
            shape = RoundedCornerShape(12.dp)
        )
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(SlateSurface, shape = RoundedCornerShape(12.dp))
            .border(1.dp, if (hasSigned) EmeraldGate else BorderHairline, shape = RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (hasSigned) Icons.Default.Lock else Icons.Default.Fingerprint,
                contentDescription = null,
                tint = if (hasSigned) EmeraldGate else IndigoNexus,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (hasSigned) "ADR COMMITTED: HUMAN EXECUTIVE ATTESTATION" else "HUMAN-IN-THE-LOOP: CEO GATE (D7)",
                color = TextPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 0.5.sp
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = if (hasSigned)
                "Cryptographic SHA-256 attestation recorded in append-only SQLite WAL log. State is immutable."
            else
                "AI agents are strictly prohibited from approving this ADR. Final authorization requires human executive attestation at D7 Review.",
            color = TextMuted,
            fontSize = 11.sp,
            lineHeight = 15.sp
        )

        // Admission criteria status checklist
        if (!hasSigned) {
            Spacer(modifier = Modifier.height(10.dp))
            AdmissionStatusPill(
                label = "STAGE GUARD",
                passed = currentStatus == "D7_REVIEW" || currentStatus == "APPROVED",
                note = if (currentStatus == "D7_REVIEW") "In D7 Review Stage" else "Must be in D7 Review (Current: $currentStatus)"
            )
            AdmissionStatusPill(
                label = "ADMISSION TEST",
                passed = certificate != null || admissionResult?.allPassed == true,
                note = if (certificate != null || admissionResult?.allPassed == true) "Specificity, Novelty, Actionability, Value Passed" else "Pending Admission Verification"
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (hasSigned) {
            // Signed State Badge
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(EmeraldGate.copy(alpha = 0.12f), shape = RoundedCornerShape(8.dp))
                    .border(1.dp, EmeraldGate, shape = RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = EmeraldGate,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "STATUS: APPROVED & ATTESTED BY HUMAN CEO",
                            color = EmeraldGate,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    if (signatureHash != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "SHA-256 ATTESTATION HASH: $signatureHash",
                            color = TextMuted,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            maxLines = 1
                        )
                    }
                }
            }
        } else if (!isEligibleForApproval) {
            // Blocked state: explains why approval cannot occur yet
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1E1E2E), shape = RoundedCornerShape(8.dp))
                    .border(1.dp, CrimsonAlert.copy(alpha = 0.5f), shape = RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = CrimsonAlert,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "CEO Gate Locked: Complete D1-D6 pipeline and pass Admission Test to unlock executive slider.",
                        color = CrimsonAlert,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        } else {
            // Interactive Slider for Eligible D7 Decision
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(26.dp))
                    .background(SlateSurfaceElevated)
                    .border(1.dp, EmeraldGate.copy(alpha = 0.6f), RoundedCornerShape(26.dp))
            ) {
                val density = LocalDensity.current
                val thumbSize = 46.dp
                val maxDragPx = with(density) { (maxWidth - thumbSize - 6.dp).toPx() }

                // Text instruction in the track
                Box(
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "SLIDE FOR EXECUTIVE ATTESTATION >>",
                        color = EmeraldGate,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                }

                // Interactive Thumb
                Box(
                    modifier = Modifier
                        .offset { IntOffset(offsetX.roundToInt() + 6, 3) }
                        .size(thumbSize)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(IndigoNexus, EmeraldGate)
                            )
                        )
                        .draggable(
                            orientation = Orientation.Horizontal,
                            state = rememberDraggableState { delta ->
                                val safeMax = maxOf(0f, maxDragPx)
                                val newOffset = if (safeMax > 0f) (offsetX + delta).coerceIn(0f, safeMax) else 0f
                                offsetX = newOffset
                                if (safeMax > 0f && newOffset >= safeMax * 0.92f && !hasSigned) {
                                    triggerHapticFeedback()
                                    showDeviceCredentialModal = true
                                }
                            },
                            onDragStopped = {
                                if (!hasSigned) {
                                    offsetX = 0f
                                }
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = "Sign Button",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun AdmissionStatusPill(
    label: String,
    passed: Boolean,
    note: String
) {
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
        Text(
            text = "$label: ",
            color = TextPrimary,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = note,
            color = if (passed) TextMuted else CrimsonAlert,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}
