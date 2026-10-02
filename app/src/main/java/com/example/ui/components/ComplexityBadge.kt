package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AmberFlame
import com.example.ui.theme.CrimsonAlert
import com.example.ui.theme.CyanTelemetry
import com.example.ui.theme.EmeraldGate

/**
 * KingMaker v7.0 Proportionality Tier Badge (Section 8 & 56).
 * T1 (Lightweight) | T2 (Standard) | T3 (Rigorous / Pre-Mortem)
 */
@Composable
fun ComplexityBadge(
    tier: String,
    score: Double? = null,
    modifier: Modifier = Modifier
) {
    val (color, label) = when (tier.uppercase()) {
        "MAXIMUM" -> CrimsonAlert to "T3 MAXIMUM"
        "RIGOROUS" -> AmberFlame to "T3 RIGOROUS"
        "STANDARD" -> CyanTelemetry to "T2 STANDARD"
        else -> EmeraldGate to "T1 LIGHT"
    }

    Box(
        modifier = modifier
            .background(color.copy(alpha = 0.12f), shape = RoundedCornerShape(4.dp))
            .border(1.dp, color.copy(alpha = 0.5f), shape = RoundedCornerShape(4.dp))
            .padding(horizontal = 7.dp, vertical = 3.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(color, shape = RoundedCornerShape(3.dp))
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = if (score != null) "$label (${String.format("%.2f", score)})" else label,
                color = color,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
