package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ContextQuestion
import com.example.data.local.DecisionEntity
import com.example.data.local.EpistemicType
import com.example.data.local.QuestionStatus
import com.example.ui.localization.LocalAppStrings
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.BorderHairline
import com.example.ui.theme.CrimsonAlert
import com.example.ui.theme.CyanTelemetry
import com.example.ui.theme.EmeraldGate
import com.example.ui.theme.IndigoNexus
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.SlateSurface
import com.example.ui.theme.SlateSurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

/**
 * Consequential Context Questions Screen (Directive 6).
 * Surfaces non-negotiable questions where answers materially alter decision pathways.
 * Explicitly supports UNKNOWN ("I don't know") and prevents silently bypassing unresolved blockers.
 */
@Composable
fun ContextQuestionsScreen(
    decision: DecisionEntity,
    questions: List<ContextQuestion>,
    onSaveQuestionAnswer: (questionId: String, answer: String?, status: QuestionStatus, epistemicType: EpistemicType) -> Unit,
    onProceedToFraming: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current

    val unresolvedCount = questions.count {
        it.status == QuestionStatus.UNANSWERED
    }
    val unknownCount = questions.count {
        it.status == QuestionStatus.UNKNOWN
    }
    val answeredCount = questions.count {
        it.status == QuestionStatus.ANSWERED
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBg)
            .padding(14.dp)
    ) {
        // Top navigation bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = CyanTelemetry
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Column {
                    Text(
                        text = "কনসিকোয়েন্সিয়াল কনটেক্সট প্রশ্নাবলী",
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "সিদ্ধান্ত: ${decision.title.take(30)}...",
                        color = CyanTelemetry,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Status Badge
            Box(
                modifier = Modifier
                    .background(
                        if (unresolvedCount == 0) EmeraldGate.copy(alpha = 0.2f) else AmberWarning.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(6.dp)
                    )
                    .border(
                        1.dp,
                        if (unresolvedCount == 0) EmeraldGate else AmberWarning,
                        shape = RoundedCornerShape(6.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (unresolvedCount == 0) "সব প্রশ্ন পর্যালোচিত" else "$unresolvedCount টি অনুত্তর প্রশ্ন বাকি",
                    color = if (unresolvedCount == 0) EmeraldGate else AmberWarning,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Explanatory Guard Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(SlateSurfaceElevated, shape = RoundedCornerShape(8.dp))
                .border(1.dp, BorderHairline, shape = RoundedCornerShape(8.dp))
                .padding(10.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.HelpOutline,
                        contentDescription = null,
                        tint = CyanTelemetry,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "কেন এই প্রশ্নের উত্তর গুরুত্বপূর্ণ?",
                        color = CyanTelemetry,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "প্রতিটি প্রশ্নের উত্তর আপনার সিদ্ধান্তের ঝুঁকি, খরচ বা স্থাপত্যের পথকে মৌলিকভাবে প্রভাবিত করে। উত্তর নিশ্চিত না হলে সত্য চেপে না রেখে নির্দ্বিধায় 'UNKNOWN' (আমি জানি না) চিহ্নিত করুন। ভুয়া উত্তর দেওয়া সম্পূর্ণ নিষিদ্ধ।",
                    color = TextMuted,
                    fontSize = 10.sp,
                    lineHeight = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Questions List
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            items(questions, key = { it.id }) { question ->
                QuestionItemCard(
                    question = question,
                    onSaveAnswer = { ans, status, epistemic ->
                        onSaveQuestionAnswer(question.id, ans, status, epistemic)
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Bottom Action Bar: Proceed to Framing
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "উত্তর দেওয়া: $answeredCount | অজানা: $unknownCount | অনিষ্পন্ন: $unresolvedCount",
                    color = TextMuted,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace
                )
                if (unresolvedCount > 0) {
                    Text(
                        text = "সতর্কতা: অনিষ্পন্ন প্রশ্ন রেখে গেলে ফ্রেমিং অসম্পূর্ণ থাকবে।",
                        color = CrimsonAlert,
                        fontSize = 8.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Button(
                onClick = onProceedToFraming,
                enabled = unresolvedCount == 0,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (unresolvedCount == 0) EmeraldGate else SlateSurface,
                    contentColor = if (unresolvedCount == 0) Color.Black else TextMuted
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("proceed_to_framing_btn")
            ) {
                Text(
                    text = "ফ্রেমিং এ যান →",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun QuestionItemCard(
    question: ContextQuestion,
    onSaveAnswer: (answer: String?, status: QuestionStatus, epistemic: EpistemicType) -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    var currentAnswer by remember(question.answer) { mutableStateOf(question.answer ?: "") }

    val statusColor = when (question.status) {
        QuestionStatus.ANSWERED -> EmeraldGate
        QuestionStatus.UNKNOWN -> AmberWarning
        QuestionStatus.DEFERRED -> CyanTelemetry
        QuestionStatus.NOT_APPLICABLE -> TextMuted
        QuestionStatus.UNANSWERED -> CrimsonAlert
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(SlateSurface, shape = RoundedCornerShape(8.dp))
            .border(1.dp, if (question.status == QuestionStatus.UNANSWERED) CrimsonAlert.copy(alpha = 0.5f) else BorderHairline, shape = RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        // Top row: Category, Status chip
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "[${question.category}]",
                color = CyanTelemetry,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )

            Box(
                modifier = Modifier
                    .background(statusColor.copy(alpha = 0.15f), shape = RoundedCornerShape(4.dp))
                    .border(1.dp, statusColor, shape = RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = question.status.name,
                    color = statusColor,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Question text
        Text(
            text = question.question,
            color = TextPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 16.sp
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Consequence / Why it matters
        Row(verticalAlignment = Alignment.Top) {
            Text(
                text = "কনসিকোয়েন্স: ",
                color = AmberWarning,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = question.consequence,
                color = TextSecondary,
                fontSize = 10.sp,
                lineHeight = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Current Answer display or Input field
        OutlinedTextField(
            value = currentAnswer,
            onValueChange = { currentAnswer = it },
            placeholder = { Text("আপনার উত্তর লিখুন...", color = TextMuted, fontSize = 10.sp) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CyanTelemetry,
                unfocusedBorderColor = BorderHairline,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            ),
            shape = RoundedCornerShape(6.dp),
            modifier = Modifier.fillMaxWidth(),
            maxLines = 3
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Action options: Answered, Unknown ("I don't know"), Deferred, Not Applicable
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Save as Answered
            Button(
                onClick = {
                    if (currentAnswer.isNotBlank()) {
                        onSaveAnswer(currentAnswer.trim(), QuestionStatus.ANSWERED, EpistemicType.FACT)
                    }
                },
                enabled = currentAnswer.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = IndigoNexus),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text("উত্তর সেভ", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            }

            // Mark as UNKNOWN / "I don't know" (Directive 6: First-class citizen)
            OutlinedButton(
                onClick = {
                    currentAnswer = "অজানা (UNKNOWN) - এখনো যাচাই করা হয়নি"
                    onSaveAnswer("অজানা (UNKNOWN)", QuestionStatus.UNKNOWN, EpistemicType.UNKNOWN)
                },
                colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(contentColor = AmberWarning),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text("অজানা (UNKNOWN)", color = AmberWarning, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            }

            // Defer
            OutlinedButton(
                onClick = {
                    onSaveAnswer(null, QuestionStatus.DEFERRED, EpistemicType.UNVERIFIED)
                },
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.weight(0.7f)
            ) {
                Text("পরে", color = TextMuted, fontSize = 9.sp)
            }
        }
    }
}
