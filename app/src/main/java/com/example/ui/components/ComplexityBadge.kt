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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AmberFlame
import com.example.ui.theme.CrimsonAlert
import com.example.ui.theme.CyanTelemetry
import com.example.ui.theme.EmeraldGate

@Composable
fun ComplexityBadge(
    tier: String,
    score: Double? = null,
    modifier: Modifier = Modifier
) {
    val (color, label) = when (tier.uppercase()) {
        "MAXIMUM" -> CrimsonAlert to "MAXIMUM"
        "RIGOROUS" -> AmberFlame to "RIGOROUS"
        "STANDARD" -> CyanTelemetry to "STANDARD"
        else -> EmeraldGate to "LIGHT"
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
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
