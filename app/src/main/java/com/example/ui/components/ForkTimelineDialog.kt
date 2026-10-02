package com.example.ui.components

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallSplit
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.DecisionEntity
import com.example.ui.theme.AmberFlame
import com.example.ui.theme.BorderHairline
import com.example.ui.theme.CyanTelemetry
import com.example.ui.theme.IndigoNexus
import com.example.ui.theme.SlateSurface
import com.example.ui.theme.SlateSurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun ForkTimelineDialog(
    sourceDecision: DecisionEntity?,
    onDismiss: () -> Unit,
    onConfirmFork: (sourceDecisionId: String, branchName: String, exploratoryTitle: String) -> Unit
) {
    var branchName by remember { mutableStateOf("exp/multi-master-raft") }
    var exploratoryTitle by remember {
        mutableStateOf(
            if (sourceDecision != null) "Exploratory Paxos consensus alternative for ${sourceDecision.title}"
            else "Alternative exploratory timeline hypothesis"
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(SlateSurface, shape = RoundedCornerShape(14.dp))
                .border(1.dp, IndigoNexus, shape = RoundedCornerShape(14.dp))
                .padding(18.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CallSplit,
                            contentDescription = null,
                            tint = AmberFlame,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "FORK DECISION TIMELINE",
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Forking creates an immutable alternate branch in SQLite WAL. The original 'main' decision remains locked and unimpacted.",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Source node display
                if (sourceDecision != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SlateSurfaceElevated, shape = RoundedCornerShape(6.dp))
                            .border(1.dp, BorderHairline, shape = RoundedCornerShape(6.dp))
                            .padding(8.dp)
                    ) {
                        Column {
                            Text(
                                text = "FORKING POINT (SOURCE NODE):",
                                color = CyanTelemetry,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${sourceDecision.id} • ${sourceDecision.title}",
                                color = TextPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                Text(
                    text = "NEW BRANCH NAME",
                    color = TextMuted,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = branchName,
                    onValueChange = { branchName = it },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = IndigoNexus,
                        unfocusedBorderColor = BorderHairline,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedContainerColor = SlateSurfaceElevated,
                        unfocusedContainerColor = SlateSurfaceElevated
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "EXPLORATORY HYPOTHESIS",
                    color = TextMuted,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = exploratoryTitle,
                    onValueChange = { exploratoryTitle = it },
                    minLines = 2,
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = IndigoNexus,
                        unfocusedBorderColor = BorderHairline,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedContainerColor = SlateSurfaceElevated,
                        unfocusedContainerColor = SlateSurfaceElevated
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = {
                        val srcId = sourceDecision?.id ?: "DEC-001"
                        if (branchName.isNotBlank()) {
                            onConfirmFork(srcId, branchName.trim(), exploratoryTitle.trim())
                            onDismiss()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = IndigoNexus),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().height(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CallSplit,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "EXECUTE FORK IN WAL LOG",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}
