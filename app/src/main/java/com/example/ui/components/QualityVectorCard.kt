package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.QualityVector
import com.example.ui.theme.BorderHairline
import com.example.ui.theme.CyanTelemetry
import com.example.ui.theme.EmeraldGate
import com.example.ui.theme.GoldWarning
import com.example.ui.theme.IndigoNexus
import com.example.ui.theme.SlateSurface
import com.example.ui.theme.SlateSurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary

/**
 * KingMaker v7.0: 9-Dimensional Quality Vector Card (Section 9 & 48).
 * Replaces the legacy scalar DQS gauge with a diagnostic multi-vector model.
 */
@Composable
fun QualityVectorCard(
    qualityVector: QualityVector,
    dqsScore: Double = qualityVector.compositeHeuristic,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    val compositeColor = when {
        dqsScore >= 0.80 -> EmeraldGate
        dqsScore >= 0.60 -> CyanTelemetry
        else -> GoldWarning
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(SlateSurface, shape = RoundedCornerShape(12.dp))
            .border(1.dp, BorderHairline, shape = RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        // Summary Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(compositeColor.copy(alpha = 0.15f))
                        .border(1.dp, compositeColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${(dqsScore * 100).toInt()}%",
                        color = compositeColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "DECISION QUALITY VECTOR",
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .background(IndigoNexus.copy(alpha = 0.2f), shape = RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("9-DIMENSIONS", color = IndigoNexus, fontSize = 8.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                        }
                    }
                    Text(
                        text = "ডায়াগনস্টিক গুণমান পরিমাপ (Quality ≠ Approval)",
                        color = TextMuted,
                        fontSize = 10.sp
                    )
                }
            }

            Icon(
                imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = "Expand",
                tint = TextMuted
            )
        }

        // Expanded 9 Dimensions
        AnimatedVisibility(visible = expanded) {
            Column(modifier = Modifier.padding(top = 14.dp)) {
                VectorDimensionRow("প্রমাণ ও ভিত্তি (Evidence Strength)", qualityVector.evidenceStrength, "Axiomatic/Empirical telemetry vs Assumptions")
                VectorDimensionRow("ফ্রেমের পূর্ণতা (Frame Completeness)", qualityVector.frameCompleteness, "Goals, non-goals, hard constraints identified")
                VectorDimensionRow("সীমাবদ্ধতা মানানসই (Constraint Fit)", qualityVector.constraintFit, "Adherence to non-negotiable boundaries")
                VectorDimensionRow("বিকল্পের কভারেজ (Option Coverage)", qualityVector.optionCoverage, "Realistic alternative paths evaluated")
                VectorDimensionRow("পরিবর্তনযোগ্যতা (Reversibility)", qualityVector.reversibility, "Two-way door vs Irreversible commit")
                VectorDimensionRow("ঝুঁকির স্থিতিস্থাপকতা (Risk Resilience)", qualityVector.riskExposure, "Downside protection under adverse scenarios")
                VectorDimensionRow("মতামতের বৈচিত্র্য (Disagreement Extent)", qualityVector.disagreement, "Constructive specialist challenge captured")
                VectorDimensionRow("পরীক্ষা প্রস্তুতি (Validation Readiness)", qualityVector.validationReadiness, "Executable tests and falsifiable hypotheses")
                VectorDimensionRow("জটিলতা জরিমানা (Complexity Penalty)", 1.0 - qualityVector.complexityPenalty, "Architectural bloat & cognitive overhead", isInverted = true)

                Spacer(modifier = Modifier.height(10.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SlateSurfaceElevated, shape = RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = CyanTelemetry, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "নীতিগত নিয়ম (Invariant I-19): কোনো নির্দিষ্ট সংখ্যা বা স্কোর স্বয়ংক্রিয় অনুমোদন নির্দেশ করে না। অনুমোদন সর্বদা মানবীয় দায়িত্ব।",
                            color = TextMuted,
                            fontSize = 10.sp,
                            lineHeight = 14.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun VectorDimensionRow(
    label: String,
    score: Double,
    description: String,
    isInverted: Boolean = false
) {
    val progress = score.toFloat().coerceIn(0f, 1f)
    val color = when {
        progress >= 0.75f -> EmeraldGate
        progress >= 0.50f -> CyanTelemetry
        else -> GoldWarning
    }

    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                color = TextPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "${(progress * 100).toInt()}%",
                color = color,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(3.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = color,
            trackColor = SlateSurfaceElevated
        )
    }
}
