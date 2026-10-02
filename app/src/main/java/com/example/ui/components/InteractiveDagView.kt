package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.graph.CycleDetectionResult
import com.example.core.graph.DependencyGraphEngine
import com.example.core.graph.GraphEdge
import com.example.data.local.DecisionEdgeEntity
import com.example.data.local.DecisionEntity
import com.example.ui.theme.AmberFlame
import com.example.ui.theme.BorderHairline
import com.example.ui.theme.CrimsonAlert
import com.example.ui.theme.CyanTelemetry
import com.example.ui.theme.EmeraldGate
import com.example.ui.theme.IndigoNexus
import com.example.ui.theme.SlateSurface
import com.example.ui.theme.SlateSurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

enum class DagDisplayMode {
    VISUAL_CANVAS,
    ACCESSIBLE_LIST
}

/**
 * KingMaker v7.0: Living Dependency DAG View (Section 12, 15, 27).
 * Supports:
 * 1. Visual Bézier DAG with Topological Node layout
 * 2. Accessible Text/List Alternative (mandatory for accessibility & narrow phone widths)
 * 3. Typed edge representation (DEPENDS_ON, CONSTRAINS, SUPPORTS, SUPERSEDES, etc.)
 * 4. Tarjan SCC cycle detection indicator
 */
@Composable
fun InteractiveDagView(
    decisions: List<DecisionEntity>,
    edges: List<DecisionEdgeEntity>,
    selectedDecisionId: String?,
    rippleHighlightedIds: Set<String>,
    cycleResult: CycleDetectionResult?,
    onSelectDecision: (DecisionEntity) -> Unit,
    onTestRipple: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val hasCycle = cycleResult?.hasCycle == true
    val cyclicNodeIds = cycleResult?.cyclicNodeIds ?: emptySet()
    var displayMode by remember { mutableStateOf(DagDisplayMode.VISUAL_CANVAS) }

    val graphEdges = remember(edges) {
        edges.map { GraphEdge(it.fromDecisionId, it.toDecisionId, it.relationship) }
    }
    val nodeIds = remember(decisions) { decisions.map { it.id } }
    val topologicalCoordinates = remember(nodeIds, graphEdges) {
        DependencyGraphEngine.calculateTopologicalCoordinates(nodeIds, graphEdges)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(SlateSurface, shape = RoundedCornerShape(12.dp))
            .border(1.dp, if (hasCycle) CrimsonAlert else BorderHairline, shape = RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        // DAG Header Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(if (hasCycle) CrimsonAlert else EmeraldGate, shape = CircleShape)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "LIVING DEPENDENCY DAG",
                    color = TextPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // View Mode Toggle (Visual vs Accessible List)
                IconButton(
                    onClick = {
                        displayMode = if (displayMode == DagDisplayMode.VISUAL_CANVAS) DagDisplayMode.ACCESSIBLE_LIST else DagDisplayMode.VISUAL_CANVAS
                    },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = if (displayMode == DagDisplayMode.VISUAL_CANVAS) Icons.Default.List else Icons.Default.AccountTree,
                        contentDescription = "Toggle Accessible View",
                        tint = CyanTelemetry,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Tarjan SCC Health Badge
                Box(
                    modifier = Modifier
                        .background(
                            if (hasCycle) CrimsonAlert.copy(alpha = 0.2f) else EmeraldGate.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(4.dp)
                        )
                        .border(
                            1.dp,
                            if (hasCycle) CrimsonAlert else EmeraldGate.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (hasCycle) Icons.Default.Warning else Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = if (hasCycle) CrimsonAlert else EmeraldGate,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (hasCycle) "CYCLE DETECTED" else "TARJAN: ACYCLIC",
                            color = if (hasCycle) CrimsonAlert else EmeraldGate,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (displayMode == DagDisplayMode.VISUAL_CANVAS) {
            // Visual Graph Canvas with Bézier Curves & Topological Nodes
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(190.dp)
                    .background(Color(0xFF090E1A), shape = RoundedCornerShape(8.dp))
                    .border(1.dp, Color(0xFF1E293B), shape = RoundedCornerShape(8.dp))
            ) {
                val canvasWidthPx = constraints.maxWidth.toFloat()
                val canvasHeightPx = constraints.maxHeight.toFloat()
                if (canvasWidthPx <= 20f || canvasHeightPx <= 20f) return@BoxWithConstraints

                val density = context.resources.displayMetrics.density

                Canvas(modifier = Modifier.fillMaxSize()) {
                    val step = 20.dp.toPx()
                    for (x in 0..(size.width / step).toInt()) {
                        drawLine(color = Color(0xFF131C2E), start = Offset(x * step, 0f), end = Offset(x * step, size.height), strokeWidth = 0.5f)
                    }
                    for (y in 0..(size.height / step).toInt()) {
                        drawLine(color = Color(0xFF131C2E), start = Offset(0f, y * step), end = Offset(size.width, y * step), strokeWidth = 0.5f)
                    }

                    // Render Bézier Edges
                    edges.forEach { edge ->
                        val fromCoord = topologicalCoordinates[edge.fromDecisionId]
                        val toCoord = topologicalCoordinates[edge.toDecisionId]

                        if (fromCoord != null && toCoord != null) {
                            val startX = fromCoord.normalizedX * size.width
                            val startY = fromCoord.normalizedY * size.height
                            val endX = toCoord.normalizedX * size.width
                            val endY = toCoord.normalizedY * size.height

                            val isHighlighted = rippleHighlightedIds.contains(edge.toDecisionId)

                            val path = Path().apply {
                                moveTo(startX, startY)
                                val midX = (startX + endX) / 2f
                                cubicTo(x1 = midX, y1 = startY, x2 = midX, y2 = endY, x3 = endX, y3 = endY)
                            }

                            val strokeColor = if (isHighlighted) AmberFlame else CyanTelemetry.copy(alpha = 0.7f)
                            val strokeWidth = if (isHighlighted) 3.dp.toPx() else 1.8.dp.toPx()

                            drawPath(
                                path = path,
                                color = strokeColor,
                                style = Stroke(
                                    width = strokeWidth,
                                    cap = StrokeCap.Round,
                                    pathEffect = if (isHighlighted) PathEffect.dashPathEffect(floatArrayOf(12f, 10f), 0f) else null
                                )
                            )

                            drawCircle(color = strokeColor, radius = if (isHighlighted) 4.5.dp.toPx() else 3.dp.toPx(), center = Offset(endX, endY))
                        }
                    }
                }

                if (decisions.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(imageVector = Icons.Default.ElectricBolt, contentDescription = null, tint = CyanTelemetry.copy(alpha = 0.5f), modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "DAG STANDBY: NO ACTIVE NODES", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                    }
                }

                // Render Topological Nodes
                decisions.forEach { decision ->
                    val coord = topologicalCoordinates[decision.id]
                    val isSelected = decision.id == selectedDecisionId
                    val isRippleAffected = rippleHighlightedIds.contains(decision.id)
                    val isDeadlocked = cyclicNodeIds.contains(decision.id)

                    val nodeColor = when {
                        isDeadlocked -> CrimsonAlert
                        decision.status == "APPROVED" -> EmeraldGate
                        decision.status.startsWith("D4") || decision.status.startsWith("D5") -> AmberFlame
                        else -> CyanTelemetry
                    }

                    val normX = coord?.normalizedX ?: 0.5f
                    val normY = coord?.normalizedY ?: 0.5f
                    val nodeWidthDp = 76.dp
                    val nodeHeightDp = 44.dp
                    val nodeWidthPx = nodeWidthDp.value * density
                    val nodeHeightPx = nodeHeightDp.value * density

                    val maxLeft = maxOf(8f, canvasWidthPx - nodeWidthPx - 8f)
                    val leftPx = if (maxLeft > 8f) ((normX * canvasWidthPx) - (nodeWidthPx / 2f)).coerceIn(8f, maxLeft) else 8f

                    val maxTop = maxOf(8f, canvasHeightPx - nodeHeightPx - 8f)
                    val topPx = if (maxTop > 8f) ((normY * canvasHeightPx) - (nodeHeightPx / 2f)).coerceIn(8f, maxTop) else 8f

                    Box(
                        modifier = Modifier
                            .offset(x = (leftPx / density).dp, y = (topPx / density).dp)
                            .size(width = nodeWidthDp, height = nodeHeightDp)
                            .background(if (isSelected) nodeColor.copy(alpha = 0.22f) else Color(0xFF111827), shape = RoundedCornerShape(8.dp))
                            .border(
                                width = if (isSelected || isRippleAffected) 2.dp else 1.dp,
                                color = if (isRippleAffected) AmberFlame else if (isSelected) nodeColor else Color(0xFF2A374E),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable {
                                HapticFeedbackHelper.triggerLightImpact(context)
                                onSelectDecision(decision)
                                onTestRipple(decision.id)
                            }
                            .padding(horizontal = 4.dp, vertical = 3.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                                Box(modifier = Modifier.size(5.dp).background(nodeColor, shape = CircleShape))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = decision.id.take(8),
                                    color = if (isSelected) TextPrimary else TextMuted,
                                    fontSize = 8.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = "${(decision.dqsScore * 100).toInt()}% Q-Score",
                                color = nodeColor,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = decision.title,
                                color = if (isSelected) TextPrimary else TextSecondary,
                                fontSize = 7.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        } else {
            // Accessible Text / List Alternative (Section 15 & 27)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF090E1A), shape = RoundedCornerShape(8.dp))
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "অ্যাক্সেসিবল টেক্সট অল্টারনেটিভ (Accessible Graph Alternative):",
                    color = CyanTelemetry,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                if (decisions.isEmpty()) {
                    Text("কোনো সিদ্ধান্ত নোড নেই।", color = TextMuted, fontSize = 11.sp)
                } else {
                    decisions.forEach { dec ->
                        val isSelected = dec.id == selectedDecisionId
                        val outgoingEdges = edges.filter { it.fromDecisionId == dec.id }
                        val incomingEdges = edges.filter { it.toDecisionId == dec.id }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(if (isSelected) SlateSurfaceElevated else Color(0xFF111827), shape = RoundedCornerShape(6.dp))
                                .border(1.dp, if (isSelected) IndigoNexus else BorderHairline, shape = RoundedCornerShape(6.dp))
                                .clickable {
                                    HapticFeedbackHelper.triggerLightImpact(context)
                                    onSelectDecision(dec)
                                    onTestRipple(dec.id)
                                }
                                .padding(10.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${dec.id}: ${dec.title}",
                                        color = TextPrimary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = dec.status,
                                        color = if (dec.status == "APPROVED") EmeraldGate else CyanTelemetry,
                                        fontSize = 9.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                if (outgoingEdges.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "নির্ভরশীলতা (Dependencies): " + outgoingEdges.joinToString(", ") { "${it.relationship} -> ${it.toDecisionId}" },
                                        color = TextMuted,
                                        fontSize = 9.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                                if (incomingEdges.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "প্রয়োজনীয়তা (Prerequisites): " + incomingEdges.joinToString(", ") { "${it.fromDecisionId} (${it.relationship})" },
                                        color = TextMuted,
                                        fontSize = 9.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Ripple cascade feedback note
        if (rippleHighlightedIds.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AmberFlame.copy(alpha = 0.1f), shape = RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Icon(imageVector = Icons.Default.ElectricBolt, contentDescription = null, tint = AmberFlame, modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "রিপল ক্যাসকেড: ${rippleHighlightedIds.size}টি নিম্নমুখী নোড এই পরিবর্তনে প্রভাবিত হবে",
                    color = AmberFlame,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}
