package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AmberFlame
import com.example.ui.theme.CrimsonAlert
import com.example.ui.theme.EmeraldGate
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary

@Composable
fun DqsGauge(
    score: Double,
    size: Dp = 80.dp,
    strokeWidth: Dp = 7.dp,
    showThreshold: Boolean = true
) {
    val animatedScore by animateFloatAsState(
        targetValue = score.toFloat().coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 800),
        label = "DqsAnimation"
    )

    val gaugeColor = when {
        score >= 0.85 -> EmeraldGate
        score >= 0.65 -> AmberFlame
        else -> CrimsonAlert
    }

    Box(
        modifier = Modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val strokePx = strokeWidth.toPx()
            // Background track
            drawArc(
                color = Color(0xFF1F2937),
                startAngle = 135f,
                sweepAngle = 270f,
                useCenter = false,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )

            // Progress Arc
            drawArc(
                color = gaugeColor,
                startAngle = 135f,
                sweepAngle = 270f * animatedScore,
                useCenter = false,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )

            if (showThreshold) {
                // Target 0.85 marker (135 + 270 * 0.85 = 364.5 deg)
                val targetAngle = 135f + (270f * 0.85f)
                drawArc(
                    color = Color.White.copy(alpha = 0.8f),
                    startAngle = targetAngle - 1f,
                    sweepAngle = 2f,
                    useCenter = false,
                    style = Stroke(width = strokePx + 4f, cap = StrokeCap.Square)
                )
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = String.format("%.2f", score),
                color = TextPrimary,
                fontSize = if (size > 100.dp) 22.sp else 15.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = "DQS",
                color = TextMuted,
                fontSize = 9.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
