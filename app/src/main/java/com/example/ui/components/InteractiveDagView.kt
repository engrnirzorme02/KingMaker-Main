package com.example.ui.components

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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlin.math.roundToInt

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

    // Topological Layout calculation (assigns (x, y) coordinates based on hierarchical rank)
    val graphEdges = remember(edges) {
        edges.map { GraphEdge(it.fromDecisionId, it.toDecisionId) }
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
                        text = if (hasCycle) "DEADLOCK: CYCLE DETECTED" else "TARJAN: ACYCLIC",
                        color = if (hasCycle) CrimsonAlert else EmeraldGate,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Visual Graph Layout Canvas with Bézier Curves & Topological Nodes
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(190.dp)
                .background(Color(0xFF090E1A), shape = RoundedCornerShape(8.dp))
                .border(1.dp, Color(0xFF1E293B), shape = RoundedCornerShape(8.dp))
        ) {
            val canvasWidthPx = constraints.maxWidth.toFloat()
            val canvasHeightPx = constraints.maxHeight.toFloat()
            if (canvasWidthPx <= 20f || canvasHeightPx <= 20f) {
                return@BoxWithConstraints
            }
            val density = context.resources.displayMetrics.density

            // 1. Draw Background Cyber Grid & Cubic Bézier Curves
            Canvas(modifier = Modifier.fillMaxSize()) {
                // Tactical grid lines
                val step = 20.dp.toPx()
                for (x in 0..(size.width / step).toInt()) {
                    drawLine(
                        color = Color(0xFF131C2E),
                        start = Offset(x * step, 0f),
                        end = Offset(x * step, size.height),
                        strokeWidth = 0.5f
                    )
                }
                for (y in 0..(size.height / step).toInt()) {
                    drawLine(
                        color = Color(0xFF131C2E),
                        start = Offset(0f, y * step),
                        end = Offset(size.width, y * step),
                        strokeWidth = 0.5f
                    )
                }

                // Render Bézier Edges between topologically assigned coordinates
                edges.forEach { edge ->
                    val fromCoord = topologicalCoordinates[edge.fromDecisionId]
                    val toCoord = topologicalCoordinates[edge.toDecisionId]

                    if (fromCoord != null && toCoord != null) {
                        val startX = fromCoord.normalizedX * size.width
                        val startY = fromCoord.normalizedY * size.height
                        val endX = toCoord.normalizedX * size.width
                        val endY = toCoord.normalizedY * size.height

                        val isHighlighted = rippleHighlightedIds.contains(edge.toDecisionId)

                        // Smooth Cubic Bézier Curve
                        val path = Path().apply {
                            moveTo(startX, startY)
                            val midX = (startX + endX) / 2f
                            cubicTo(
                                x1 = midX, y1 = startY,
                                x2 = midX, y2 = endY,
                                x3 = endX, y3 = endY
                            )
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

                        // Directional Arrow Head at the destination
                        drawCircle(
                            color = strokeColor,
                            radius = if (isHighlighted) 4.5.dp.toPx() else 3.dp.toPx(),
                            center = Offset(endX, endY)
                        )
                    }
                }
            }

            // Standby empty state overlay when no decisions exist in current branch
            if (decisions.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.ElectricBolt,
                            contentDescription = null,
                            tint = CyanTelemetry.copy(alpha = 0.5f),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "DAG STANDBY: NO ACTIVE NODES",
                            color = TextMuted,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "Workspace is clean. Launch a new decision stream below.",
                            color = TextSecondary,
                            fontSize = 8.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            // 2. Render Topological Decision Nodes
            decisions.forEach { decision ->
                val coord = topologicalCoordinates[decision.id]
                val isSelected = decision.id == selectedDecisionId
                val isRippleAffected = rippleHighlightedIds.contains(decision.id)
                val isDeadlocked = cyclicNodeIds.contains(decision.id)

                // Directive 1: State colors: Green (Approved), Yellow (Debating), Red (Deadlock/Needs User)
                val nodeColor = when {
                    isDeadlocked -> CrimsonAlert
                    decision.status == "APPROVED" -> EmeraldGate
                    decision.status.startsWith("D4") || decision.status.startsWith("D5") -> AmberFlame
                    decision.status == "D6_ROUTING" -> CrimsonAlert
                    else -> CyanTelemetry
                }

                // Calculate pixel offset (clamped inside bounds)
                val normX = coord?.normalizedX ?: 0.5f
                val normY = coord?.normalizedY ?: 0.5f
                val nodeWidthDp = 70.dp
                val nodeHeightDp = 44.dp
                val nodeWidthPx = nodeWidthDp.value * density
                val nodeHeightPx = nodeHeightDp.value * density

                val maxLeft = maxOf(8f, canvasWidthPx - nodeWidthPx - 8f)
                val leftPx = if (maxLeft > 8f) {
                    ((normX * canvasWidthPx) - (nodeWidthPx / 2f)).coerceIn(8f, maxLeft)
                } else 8f

                val maxTop = maxOf(8f, canvasHeightPx - nodeHeightPx - 8f)
                val topPx = if (maxTop > 8f) {
                    ((normY * canvasHeightPx) - (nodeHeightPx / 2f)).coerceIn(8f, maxTop)
                } else 8f

                Box(
                    modifier = Modifier
                        .offset(x = (leftPx / density).dp, y = (topPx / density).dp)
                        .size(width = nodeWidthDp, height = nodeHeightDp)
                        .background(
                            if (isSelected) nodeColor.copy(alpha = 0.22f) else Color(0xFF111827),
                            shape = RoundedCornerShape(8.dp)
                        )
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            // Node Pin indicator
                            Box(
                                modifier = Modifier
                                    .size(5.dp)
                                    .background(nodeColor, shape = CircleShape)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = decision.id,
                                color = if (isSelected) TextPrimary else TextMuted,
                                fontSize = 8.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = String.format("%.2f DQS", decision.dqsScore),
                            color = nodeColor,
                            fontSize = 9.sp,
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
                Icon(
                    imageVector = Icons.Default.ElectricBolt,
                    contentDescription = null,
                    tint = AmberFlame,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Ripple Cascade: ${rippleHighlightedIds.size} downstream decision nodes affected",
                    color = AmberFlame,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}
