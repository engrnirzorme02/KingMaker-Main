package com.example.ui.components

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
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Architecture
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Language
import com.example.ui.localization.AppLanguage
import com.example.ui.localization.LocalAppStrings
import com.example.services.AiEngineMode
import com.example.services.ApiSettings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
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
import com.example.data.local.DecisionEntity
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

@Composable
fun HeaderCockpitBar(
    projectName: String = "KINGMAKER-V7-WORKSPACE",
    decisions: List<DecisionEntity>,
    userEmail: String = "engr.nirzor.me.02@gmail.com",
    cloudSyncStatus: String = "FIREBASE READY",
    isSyncing: Boolean = false,
    currentLanguage: AppLanguage = AppLanguage.BN,
    onSelectLanguage: (AppLanguage) -> Unit = {},
    onTriggerDeadlockDemo: () -> Unit,
    onSyncToCloud: () -> Unit = {},
    onLoginAccount: (email: String, pass: String) -> Unit = { _, _ -> },
    onPurgeWorkspace: () -> Unit = {},
    apiSettings: ApiSettings? = null,
    onSaveApiSettings: (apiKey: String, endpoint: String, model: String, enabled: Boolean) -> Unit = { _, _, _, _ -> },
    onTestApiConnection: suspend (apiKey: String) -> Pair<Boolean, String> = { Pair(true, "") },
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    var showAccountDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var inputEmail by remember { mutableStateOf(userEmail) }
    var inputPassword by remember { mutableStateOf("kingmaker2026") }

    val avgDqs = if (decisions.isNotEmpty()) {
        decisions.map { it.dqsScore }.average()
    } else 0.0

    if (showSettingsDialog && apiSettings != null) {
        SettingsApiDialog(
            currentSettings = apiSettings,
            onSaveSettings = onSaveApiSettings,
            onTestConnection = onTestApiConnection,
            currentLanguage = currentLanguage,
            onSelectLanguage = onSelectLanguage,
            onDismiss = { showSettingsDialog = false }
        )
    }

    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = null,
                        tint = CyanTelemetry,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = strings.languageDialogTitle,
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            },
            text = {
                Column {
                    Text(
                        text = strings.languageDialogDesc,
                        color = TextMuted,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    // Bengali Option (Default)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                if (currentLanguage == AppLanguage.BN) IndigoNexus.copy(alpha = 0.25f) else SlateSurface,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .border(
                                1.dp,
                                if (currentLanguage == AppLanguage.BN) IndigoNexus else BorderHairline,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable {
                                onSelectLanguage(AppLanguage.BN)
                                showLanguageDialog = false
                            }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = strings.languageBengali,
                                    color = TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .background(EmeraldGate.copy(alpha = 0.2f), shape = RoundedCornerShape(4.dp))
                                        .padding(horizontal = 5.dp, vertical = 1.dp)
                                ) {
                                    Text("DEFAULT", color = EmeraldGate, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Text(
                                text = "ডিফল্ট সিস্টেম ভাষা (বাংলা ইন্টারফেস)",
                                color = TextMuted,
                                fontSize = 10.sp
                            )
                        }
                        if (currentLanguage == AppLanguage.BN) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = EmeraldGate,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // English Option
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                if (currentLanguage == AppLanguage.EN) IndigoNexus.copy(alpha = 0.25f) else SlateSurface,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .border(
                                1.dp,
                                if (currentLanguage == AppLanguage.EN) IndigoNexus else BorderHairline,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable {
                                onSelectLanguage(AppLanguage.EN)
                                showLanguageDialog = false
                            }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = strings.languageEnglish,
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "English UI (Standard Technical)",
                                color = TextMuted,
                                fontSize = 10.sp
                            )
                        }
                        if (currentLanguage == AppLanguage.EN) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = EmeraldGate,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                OutlinedButton(onClick = { showLanguageDialog = false }) {
                    Text(strings.close, color = TextMuted, fontSize = 10.sp)
                }
            },
            containerColor = SlateSurfaceElevated,
            shape = RoundedCornerShape(12.dp)
        )
    }

    if (showAccountDialog) {
        AlertDialog(
            onDismissRequest = { showAccountDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CloudSync,
                        contentDescription = null,
                        tint = CyanTelemetry,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = strings.accountDialogTitle,
                        color = TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            },
            text = {
                Column {
                    Text(
                        text = strings.accountDialogDesc,
                        color = TextMuted,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = inputEmail,
                        onValueChange = { inputEmail = it },
                        label = { Text(strings.emailLabel, color = TextMuted, fontSize = 10.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanTelemetry,
                            unfocusedBorderColor = BorderHairline,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = inputPassword,
                        onValueChange = { inputPassword = it },
                        label = { Text(strings.passwordLabel, color = TextMuted, fontSize = 10.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanTelemetry,
                            unfocusedBorderColor = BorderHairline,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "${strings.statusLabel} $cloudSyncStatus",
                        color = EmeraldGate,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onLoginAccount(inputEmail.trim(), inputPassword.trim())
                        showAccountDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = IndigoNexus)
                ) {
                    Text(strings.btnLoginRestore, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Button(
                        onClick = {
                            onSyncToCloud()
                            showAccountDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldGate)
                    ) {
                        Text(strings.btnSyncNow, color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    OutlinedButton(
                        onClick = {
                            onPurgeWorkspace()
                            showAccountDialog = false
                        },
                        colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(contentColor = CrimsonAlert)
                    ) {
                        Text(strings.btnPurgeDemo, color = CrimsonAlert, fontSize = 9.sp)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    OutlinedButton(onClick = { showAccountDialog = false }) {
                        Text(strings.close, color = TextMuted, fontSize = 9.sp)
                    }
                }
            },
            containerColor = SlateSurfaceElevated,
            shape = RoundedCornerShape(12.dp)
        )
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(ObsidianBg)
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        // App brand & Title
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .background(IndigoNexus.copy(alpha = 0.2f), shape = RoundedCornerShape(8.dp))
                        .border(1.dp, IndigoNexus, shape = RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Architecture,
                        contentDescription = "KingMaker Logo",
                        tint = IndigoNexus,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = strings.appName,
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .background(IndigoNexus, shape = RoundedCornerShape(3.dp))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = strings.versionTag,
                                color = Color.White,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                    Text(
                        text = strings.leadArchitect,
                        color = CyanTelemetry,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            // Global Confidence Score (Avg DQS)
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = strings.globalDqs,
                    color = TextMuted,
                    fontSize = 8.sp,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = if (decisions.isNotEmpty()) String.format("%.2f", avgDqs) else "--",
                    color = if (avgDqs >= 0.85 && decisions.isNotEmpty()) EmeraldGate else CyanTelemetry,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Firebase User Account & API Control Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Firebase User Account & Cloud Sync Pill
            Row(
                modifier = Modifier
                    .weight(1f)
                    .background(Color(0xFF0F172A), shape = RoundedCornerShape(6.dp))
                    .border(1.dp, Color(0xFF1E293B), shape = RoundedCornerShape(6.dp))
                    .clickable { showAccountDialog = true }
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f, fill = false)) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = null,
                        tint = EmeraldGate,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = userEmail,
                        color = TextPrimary,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isSyncing) {
                        CircularProgressIndicator(
                            color = CyanTelemetry,
                            strokeWidth = 1.5.dp,
                            modifier = Modifier.size(10.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Icon(
                        imageVector = if (isSyncing) Icons.Default.Sync else Icons.Default.CloudDone,
                        contentDescription = null,
                        tint = if (isSyncing) CyanTelemetry else EmeraldGate,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isSyncing) strings.syncStatusSyncing else strings.syncStatusCloud,
                        color = if (isSyncing) CyanTelemetry else EmeraldGate,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Language Selector Pill
            Row(
                modifier = Modifier
                    .background(Color(0xFF0F172A), shape = RoundedCornerShape(6.dp))
                    .border(1.dp, IndigoNexus.copy(alpha = 0.7f), shape = RoundedCornerShape(6.dp))
                    .clickable { showLanguageDialog = true }
                    .padding(horizontal = 7.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Language,
                    contentDescription = "Language",
                    tint = CyanTelemetry,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = if (currentLanguage == AppLanguage.BN) "বাংলা" else "EN",
                    color = CyanTelemetry,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            // AI Engine & API Key Settings Pill
            Row(
                modifier = Modifier
                    .background(Color(0xFF0F172A), shape = RoundedCornerShape(6.dp))
                    .border(
                        1.dp,
                        if (apiSettings?.activeEngineMode == AiEngineMode.EXTERNAL_KEY) EmeraldGate else Color(0xFF1E293B),
                        shape = RoundedCornerShape(6.dp)
                    )
                    .clickable { showSettingsDialog = true }
                    .padding(horizontal = 7.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "API Settings",
                    tint = if (apiSettings?.activeEngineMode == AiEngineMode.EXTERNAL_KEY) EmeraldGate else CyanTelemetry,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = when (apiSettings?.activeEngineMode) {
                        AiEngineMode.EXTERNAL_KEY -> strings.apiModeCustom
                        AiEngineMode.OFFLINE_DETERMINISTIC -> strings.apiModeOffline
                        else -> strings.apiModeConfig
                    },
                    color = if (apiSettings?.activeEngineMode == AiEngineMode.EXTERNAL_KEY) EmeraldGate else CyanTelemetry,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            // Baseline Badge
            Row(
                modifier = Modifier
                    .background(Color(0xFF0F172A), shape = RoundedCornerShape(6.dp))
                    .border(1.dp, IndigoNexus.copy(alpha = 0.5f), shape = RoundedCornerShape(6.dp))
                    .padding(horizontal = 7.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.size(5.dp).background(EmeraldGate, shape = CircleShape))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = strings.baselineBadge,
                    color = EmeraldGate,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // System Resource Status Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SlateSurface, shape = RoundedCornerShape(6.dp))
                .border(1.dp, BorderHairline, shape = RoundedCornerShape(6.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Storage,
                    contentDescription = null,
                    tint = EmeraldGate,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = strings.sqliteWalActive,
                    color = EmeraldGate,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(CyanTelemetry, shape = CircleShape)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = String.format(strings.nodesCount, decisions.size),
                    color = TextMuted,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            // Quick trigger button to simulate cycle deadlock
            Box(
                modifier = Modifier
                    .clickable { onTriggerDeadlockDemo() }
                    .background(Color(0xFF1E293B), shape = RoundedCornerShape(3.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = strings.testCycleBtn,
                    color = CyanTelemetry,
                    fontSize = 8.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}
