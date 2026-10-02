package com.example.ui.components

import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.CallSplit
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.DecisionBranchEntity
import com.example.ui.theme.AmberFlame
import com.example.ui.theme.BorderHairline
import com.example.ui.theme.CyanTelemetry
import com.example.ui.theme.EmeraldGate
import com.example.ui.theme.IndigoNexus
import com.example.ui.theme.SlateSurface
import com.example.ui.theme.SlateSurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun VisualBranchTree(
    branches: List<DecisionBranchEntity>,
    activeBranchId: String,
    onSelectBranch: (String) -> Unit,
    onOpenForkDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(SlateSurface, shape = RoundedCornerShape(12.dp))
            .border(1.dp, BorderHairline, shape = RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AccountTree,
                    contentDescription = null,
                    tint = IndigoNexus,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "GIT-STYLE DECISION BRANCH TREE",
                    color = TextPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 0.5.sp
                )
            }

            // Fork Timeline Quick Trigger
            Box(
                modifier = Modifier
                    .background(IndigoNexus.copy(alpha = 0.15f), shape = RoundedCornerShape(4.dp))
                    .border(1.dp, IndigoNexus, shape = RoundedCornerShape(4.dp))
                    .clickable { onOpenForkDialog() }
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CallSplit,
                        contentDescription = null,
                        tint = IndigoNexus,
                        modifier = Modifier.size(11.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "FORK TIMELINE",
                        color = IndigoNexus,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Active Timeline Branches (${branches.size}). Tap a branch to switch the entire cockpit and living blueprint view.",
            color = TextMuted,
            fontSize = 10.sp,
            lineHeight = 14.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Visual Tree List
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            branches.forEach { branch ->
                val isActive = branch.branchId == activeBranchId
                val isMain = branch.branchId == "main"
                val branchColor = if (isMain) EmeraldGate else AmberFlame

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (isActive) SlateSurfaceElevated else Color(0xFF0F172A),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .border(
                            width = if (isActive) 1.5.dp else 1.dp,
                            color = if (isActive) branchColor else BorderHairline,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable { onSelectBranch(branch.branchId) }
                        .padding(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            // Tree connector dot
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(branchColor, shape = CircleShape)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = branch.name,
                                        color = if (isActive) TextPrimary else TextSecondary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    if (isMain) {
                                        Box(
                                            modifier = Modifier
                                                .background(EmeraldGate.copy(alpha = 0.2f), RoundedCornerShape(3.dp))
                                                .padding(horizontal = 4.dp, vertical = 1.dp)
                                        ) {
                                            Text(
                                                text = "PRODUCTION",
                                                color = EmeraldGate,
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .background(AmberFlame.copy(alpha = 0.2f), RoundedCornerShape(3.dp))
                                                .padding(horizontal = 4.dp, vertical = 1.dp)
                                        ) {
                                            Text(
                                                text = "EXPLORATORY",
                                                color = AmberFlame,
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }
                                    }
                                }

                                if (branch.parentBranchId != null) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "forked from ${branch.parentBranchId} @ ${branch.forkedAtDecisionId ?: "ROOT"}",
                                        color = TextMuted,
                                        fontSize = 9.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }

                        // Right: Branch DQS badge + Active indicator
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "DQS",
                                    color = TextMuted,
                                    fontSize = 8.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = String.format("%.2f", branch.dqsScore),
                                    color = if (branch.dqsScore >= 0.85) EmeraldGate else AmberFlame,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            if (isActive) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Active Branch",
                                    tint = branchColor,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
