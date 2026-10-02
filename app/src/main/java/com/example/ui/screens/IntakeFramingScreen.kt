package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.math.ComplexityResult
import com.example.ui.TagType
import com.example.ui.TaxonomyTag
import com.example.ui.components.ComplexityBadge
import com.example.ui.theme.AmberFlame
import com.example.ui.theme.BorderHairline
import com.example.ui.theme.CyanTelemetry
import com.example.ui.theme.EmeraldGate
import com.example.ui.theme.IndigoNexus
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.PurpleConstraint
import com.example.ui.theme.SlateSurface
import com.example.ui.theme.SlateSurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WishStrikethrough

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun IntakeFramingScreen(
    title: String,
    problemStatement: String,
    risk: Double,
    impact: Double,
    changeability: Double,
    budget: Double,
    complexity: ComplexityResult,
    tags: List<TaxonomyTag>,
    scopeConfirmed: Boolean,
    focusAreaConfirmed: Boolean,
    constraintsConfirmed: Boolean,
    goalsConfirmed: Boolean,
    isFramingCompleted: Boolean = false,
    onTitleChange: (String) -> Unit,
    onProblemChange: (String) -> Unit,
    onSliderChange: (Double, Double, Double, Double) -> Unit,
    onAddTag: (String, TagType) -> Unit,
    onRemoveTag: (String) -> Unit,
    onCompleteFraming: () -> Unit = {},
    onScopeChange: (Boolean) -> Unit,
    onFocusAreaChange: (Boolean) -> Unit,
    onConstraintsChange: (Boolean) -> Unit,
    onGoalsChange: (Boolean) -> Unit,
    onLaunchProtocol: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var newTagText by remember { mutableStateOf("") }
    var selectedTagType by remember { mutableStateOf(TagType.TECHNICAL_CONSTRAINT) }

    val allD3Confirmed = scopeConfirmed && focusAreaConfirmed && constraintsConfirmed && goalsConfirmed

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBg)
    ) {
        // Top Bar
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
            Text(
                text = "D1 INTAKE ➔ D2 FRAMING ➔ D3 CONFIRMATION",
                color = TextPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 0.5.sp
            )
        }

        // Scrollable Body
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Live Complexity Prediction Widget
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SlateSurface, shape = RoundedCornerShape(10.dp))
                    .border(1.dp, BorderHairline, shape = RoundedCornerShape(10.dp))
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "COMPLEXITY ENGINE (PROPORTIONALITY)",
                            color = TextMuted,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Score: ${String.format("%.2f", complexity.score)} / 1.00",
                            color = CyanTelemetry,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "Formula: (Risk×0.35)+(Impact×0.30)+((1-Chg)×0.20)+(Budget×0.15)",
                            color = TextMuted,
                            fontSize = 8.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    ComplexityBadge(tier = complexity.tier.name, score = complexity.score)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // D1 Intake Inputs
            Text(
                text = "1. ARCHITECTURAL DECISION TITLE (D1)",
                color = TextMuted,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = title,
                onValueChange = onTitleChange,
                placeholder = { Text("e.g. Distributed Consensus Quorum Topology", color = TextMuted, fontSize = 13.sp) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CyanTelemetry,
                    unfocusedBorderColor = BorderHairline,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "2. PROBLEM STATEMENT & TENSION (D1)",
                color = TextMuted,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = problemStatement,
                onValueChange = onProblemChange,
                placeholder = { Text("Describe system bottleneck, SLA violation, or tradeoff...", color = TextMuted, fontSize = 12.sp) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CyanTelemetry,
                    unfocusedBorderColor = BorderHairline,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp),
                maxLines = 4
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Complexity Sliders (D2 Framing)
            Text(
                text = "3. COMPLEXITY DIMENSIONS & PROPORTIONALITY (D2)",
                color = TextMuted,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SlateSurface, shape = RoundedCornerShape(10.dp))
                    .border(1.dp, BorderHairline, shape = RoundedCornerShape(10.dp))
                    .padding(12.dp)
            ) {
                Column {
                    ComplexitySliderItem(
                        label = "FAILURE RISK (35%)",
                        value = risk,
                        onValueChange = { onSliderChange(it, impact, changeability, budget) }
                    )
                    ComplexitySliderItem(
                        label = "SYSTEM IMPACT (30%)",
                        value = impact,
                        onValueChange = { onSliderChange(risk, it, changeability, budget) }
                    )
                    ComplexitySliderItem(
                        label = "IRREVERSIBILITY / 1-CHANGEABILITY (20%)",
                        value = 1.0 - changeability,
                        onValueChange = { onSliderChange(risk, impact, (1.0 - it).coerceIn(0.1, 1.0), budget) }
                    )
                    ComplexitySliderItem(
                        label = "CAPITAL / BUDGET IMPACT (15%)",
                        value = budget,
                        onValueChange = { onSliderChange(risk, impact, changeability, it) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Taxonomy & Constraints (D2 Framing)
            Text(
                text = "4. TAXONOMY TAGS & CONSTRAINTS (D2)",
                color = TextMuted,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(4.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                tags.forEach { tag ->
                    TaxonomyChip(tag = tag, onDelete = { onRemoveTag(tag.id) })
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Add new tag input
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = newTagText,
                    onValueChange = { newTagText = it },
                    placeholder = { Text("Add constraint or wish...", color = TextMuted, fontSize = 11.sp) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanTelemetry,
                        unfocusedBorderColor = BorderHairline,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                Spacer(modifier = Modifier.width(6.dp))

                IconButton(
                    onClick = {
                        if (newTagText.isNotBlank()) {
                            onAddTag(newTagText.trim(), selectedTagType)
                            newTagText = ""
                        }
                    },
                    modifier = Modifier
                        .background(IndigoNexus, shape = RoundedCornerShape(8.dp))
                        .size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Chip",
                        tint = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // D2 Framing Status & Action Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (isFramingCompleted) EmeraldGate.copy(alpha = 0.10f) else SlateSurface, shape = RoundedCornerShape(10.dp))
                    .border(1.dp, if (isFramingCompleted) EmeraldGate.copy(alpha = 0.5f) else BorderHairline, shape = RoundedCornerShape(10.dp))
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isFramingCompleted) Icons.Default.CheckCircle else Icons.Default.Psychology,
                                contentDescription = null,
                                tint = if (isFramingCompleted) EmeraldGate else AmberFlame,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isFramingCompleted) "D2 FRAMING: COMPLETED" else "D2 FRAMING: REQUIRED",
                                color = if (isFramingCompleted) EmeraldGate else AmberFlame,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isFramingCompleted)
                                "ট্যাক্সোনমি ও জটিলতা মানদণ্ড স্টেট মেশিনে সুরক্ষিত।"
                            else
                                "D3 কনফার্মেশন আনলক করতে Framing সম্পন্ন করুন।",
                            color = TextMuted,
                            fontSize = 9.sp
                        )
                    }
                    Button(
                        onClick = onCompleteFraming,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isFramingCompleted) SlateSurfaceElevated else IndigoNexus
                        ),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = if (isFramingCompleted) "RE-FRAME D2" else "COMPLETE D2",
                            color = if (isFramingCompleted) CyanTelemetry else Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Directive 2: D3 Human Confirmation Gate Section
            Text(
                text = "5. D3 HUMAN CONFIRMATION GATE (MANDATORY GUARDS)",
                color = if (isFramingCompleted) CyanTelemetry else TextMuted,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SlateSurface, shape = RoundedCornerShape(10.dp))
                    .border(
                        1.dp,
                        if (!isFramingCompleted) BorderHairline else if (allD3Confirmed) EmeraldGate else CyanTelemetry.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(10.dp)
                    )
                    .padding(12.dp)
            ) {
                Column {
                    Text(
                        text = if (isFramingCompleted)
                            "Directive 2 Invariant: Before launching Multi-Agent War Room, the human operator must explicitly attest to the decision boundaries:"
                        else
                            "LOCKED: Complete D2 Framing above to unlock D3 Confirmation Gate.",
                        color = if (isFramingCompleted) TextMuted else AmberFlame,
                        fontSize = 10.sp,
                        lineHeight = 14.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    D3CheckboxRow(
                        label = "SCOPE: Architectural perimeter & bounded contexts verified",
                        checked = scopeConfirmed,
                        onCheckedChange = { if (isFramingCompleted) onScopeChange(it) }
                    )
                    D3CheckboxRow(
                        label = "FOCUS AREA: Core problem domain & trade-off focus defined",
                        checked = focusAreaConfirmed,
                        onCheckedChange = { if (isFramingCompleted) onFocusAreaChange(it) }
                    )
                    D3CheckboxRow(
                        label = "CONSTRAINTS: Technical & budgetary limits acknowledged",
                        checked = constraintsConfirmed,
                        onCheckedChange = { if (isFramingCompleted) onConstraintsChange(it) }
                    )
                    D3CheckboxRow(
                        label = "GOALS: Success criteria & value alignment metrics set",
                        checked = goalsConfirmed,
                        onCheckedChange = { if (isFramingCompleted) onGoalsChange(it) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Bottom Action Bar
        val canLaunch = isFramingCompleted && allD3Confirmed
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(SlateSurface)
                .border(1.dp, BorderHairline)
                .padding(16.dp)
        ) {
            Button(
                onClick = onLaunchProtocol,
                enabled = canLaunch,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (canLaunch) IndigoNexus else Color(0xFF1E293B),
                    disabledContainerColor = Color(0xFF1E293B)
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (canLaunch) Icons.Default.PlayArrow else Icons.Default.Shield,
                        contentDescription = null,
                        tint = if (canLaunch) Color.White else Color(0xFF64748B),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = when {
                            !isFramingCompleted -> "STEP 1: COMPLETE D2 FRAMING FIRST"
                            !allD3Confirmed -> "STEP 2: CONFIRM D3 GATE TO UNLOCK"
                            else -> "LAUNCH MULTI-AGENT PROTOCOL (D4-D5)"
                        },
                        color = if (canLaunch) Color.White else Color(0xFF64748B),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun D3CheckboxRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(
                checkedColor = EmeraldGate,
                uncheckedColor = TextMuted,
                checkmarkColor = Color.Black
            )
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            color = if (checked) TextPrimary else TextSecondary,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
fun ComplexitySliderItem(
    label: String,
    value: Double,
    onValueChange: (Double) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 3.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                color = TextSecondary,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = String.format("%.2f", value),
                color = CyanTelemetry,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.toDouble()) },
            valueRange = 0.0f..1.0f,
            colors = SliderDefaults.colors(
                thumbColor = CyanTelemetry,
                activeTrackColor = CyanTelemetry,
                inactiveTrackColor = SlateSurfaceElevated
            ),
            modifier = Modifier.height(28.dp)
        )
    }
}

@Composable
fun TaxonomyChip(
    tag: TaxonomyTag,
    onDelete: () -> Unit
) {
    val (color, prefix, isWish) = when (tag.type) {
        TagType.FACT -> Triple(CyanTelemetry, "[Fact]", false)
        TagType.BUSINESS_CONSTRAINT -> Triple(PurpleConstraint, "[Business]", false)
        TagType.TECHNICAL_CONSTRAINT -> Triple(Color(0xFF2DD4BF), "[Tech]", false)
        TagType.WISH -> Triple(WishStrikethrough, "[Wish]", true)
    }

    Box(
        modifier = Modifier
            .background(color.copy(alpha = 0.12f), shape = RoundedCornerShape(6.dp))
            .border(1.dp, color.copy(alpha = 0.5f), shape = RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = prefix,
                color = color,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = tag.text,
                color = if (isWish) Color(0xFF64748B) else TextPrimary,
                fontSize = 11.sp,
                textDecoration = if (isWish) TextDecoration.LineThrough else TextDecoration.None
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Remove",
                tint = color,
                modifier = Modifier
                    .size(12.dp)
                    .clickable { onDelete() }
            )
        }
    }
}
